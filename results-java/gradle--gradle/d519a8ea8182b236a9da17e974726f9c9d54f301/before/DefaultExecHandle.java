/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.process.internal;

import org.apache.commons.lang.StringUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.listener.AsyncListenerBroadcast;
import org.gradle.listener.ListenerBroadcast;
import org.gradle.messaging.concurrent.DefaultExecutorFactory;
import org.gradle.messaging.concurrent.StoppableExecutor;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.shutdown.ShutdownHookActionRegister;
import org.gradle.util.UncheckedException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default implementation for the ExecHandle interface.
 *
 * <h3>State flows</h3>
 *
 * <p>The ExecHandle has very strict state control.
 * The following state flows are allowed:</p>
 *
 * Normal state flow:
 * <ul><li>INIT -> STARTED -> SUCCEEDED</li></ul>
 * Failure state flows:
 * <ul>
 * <li>INIT -> FAILED</li>
 * <li>INIT -> STARTED -> FAILED</li>
 * </ul>
 * Aborted state flow:
 * <ul><li>INIT -> STARTED -> ABORTED</li></ul>
 *
 * State is controlled on all control methods:
 * <ul>
 * <li>{@link #start()} can only be called when the state is NOT {@link ExecHandleState#STARTED}</li>
 * <li>{@link #abort()} can only be called when the state is {@link ExecHandleState#STARTED}</li>
 * </ul>
 *
 * @author Tom Eyckmans
 */
public class DefaultExecHandle implements ExecHandle {
    private static final Logger LOGGER = Logging.getLogger(DefaultExecHandle.class);
    private final String displayName;
    /**
     * The working directory of the process.
     */
    private final File directory;
    /**
     * The executable to run.
     */
    private final String command;
    /**
     * Arguments to pass to the executable.
     */
    private final List<String> arguments;

    /**
     * The variables to set in the environment the executable is run in.
     */
    private final Map<String, String> environment;

    private final OutputStream standardOutput;
    private final OutputStream errorOutput;
    private final InputStream standardInput;

    /**
     * Lock to guard all mutable state
     */
    private final Lock lock;

    private final Condition stateChange;

    private final StoppableExecutor executor;

    /**
     * State of this ExecHandle.
     */
    private ExecHandleState state;

    /**
     * When not null, the runnable that is waiting
     */
    private ExecHandleRunner execHandleRunner;

    private int exitCode;
    private RuntimeException failureCause;

    private final ListenerBroadcast<ExecHandleListener> broadcast;

    private final ExecHandleShutdownHookAction shutdownHookAction;

    DefaultExecHandle(String displayName, File directory, String command, List<String> arguments,
                      Map<String, String> environment, OutputStream standardOutput, OutputStream errorOutput,
                      InputStream standardInput, List<ExecHandleListener> listeners) {
        this.displayName = displayName;
        this.directory = directory;
        this.command = command;
        this.arguments = arguments;
        this.environment = environment;
        this.standardOutput = standardOutput;
        this.errorOutput = errorOutput;
        this.standardInput = standardInput;
        this.lock = new ReentrantLock();
        this.stateChange = lock.newCondition();
        this.state = ExecHandleState.INIT;
        executor = new DefaultExecutorFactory().create(String.format("Run %s", displayName));
        shutdownHookAction = new ExecHandleShutdownHookAction(this);
        broadcast = new AsyncListenerBroadcast<ExecHandleListener>(ExecHandleListener.class, executor);
        broadcast.addAll(listeners);
    }

    public File getDirectory() {
        return directory;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public List<String> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public Map<String, String> getEnvironment() {
        return Collections.unmodifiableMap(environment);
    }

    public OutputStream getStandardOutput() {
        return standardOutput;
    }

    public OutputStream getErrorOutput() {
        return errorOutput;
    }

    public InputStream getStandardInput() {
        return standardInput;
    }

    public ExecHandleState getState() {
        lock.lock();
        try {
            return state;
        } finally {
            lock.unlock();
        }
    }

    private void setState(ExecHandleState state) {
        lock.lock();
        try {
            this.state = state;
            stateChange.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean stateIn(ExecHandleState... states) {
        lock.lock();
        try {
            return Arrays.asList(states).contains(this.state);
        } finally {
            lock.unlock();
        }
    }

    private void setEndStateInfo(ExecHandleState state, int exitCode, Throwable failureCause) {
        ShutdownHookActionRegister.removeAction(shutdownHookAction);

        lock.lock();
        try {
            if (failureCause != null) {
                if (this.state == ExecHandleState.STARTING) {
                    this.failureCause = new ExecException(String.format("A problem occurred starting %s.",
                            displayName), failureCause);
                } else {
                    this.failureCause = new ExecException(String.format(
                            "A problem occurred waiting for %s to complete.", displayName), failureCause);
                }
            }
            setState(state);
            this.exitCode = exitCode;
        } finally {
            lock.unlock();
        }

        LOGGER.debug("Process finished for {}.", displayName);

        broadcast.getSource().executionFinished(this);
        broadcast.stop();
        executor.requestStop();
    }

    public ExecHandle start() {
        lock.lock();
        try {
            if (!stateIn(ExecHandleState.INIT)) {
                throw new IllegalStateException("already started!");
            }
            setState(ExecHandleState.STARTING);

            exitCode = -1;
            failureCause = null;

            execHandleRunner = new ExecHandleRunner(this, executor);

            executor.execute(execHandleRunner);

            while (getState() == ExecHandleState.STARTING) {
                try {
                    stateChange.await();
                } catch (InterruptedException e) {
                    throw new UncheckedException(e);
                }
            }
            if (failureCause != null) {
                throw failureCause;
            }

            LOGGER.debug("Started {}.", displayName);
        } finally {
            lock.unlock();
        }
        return this;
    }

    public void abort() {
        lock.lock();
        try {
            if (state == ExecHandleState.SUCCEEDED) {
                return;
            }
            if (!stateIn(ExecHandleState.STARTED)) {
                throw new IllegalStateException("not in started state!");
            }
            this.execHandleRunner.stopWaiting();
        } finally {
            lock.unlock();
        }
    }

    public ExecResult waitForFinish() {
        executor.stop();

        final int exitValue;
        lock.lock();
        try {
            exitValue = exitCode;
            if (failureCause != null) {
                throw failureCause;
            }
        } finally {
            lock.unlock();
        }

        return new ExecResult() {
            public int getExitValue() {
                return exitValue;
            }

            public ExecResult assertNormalExitValue() throws ExecException {
                if (exitValue != 0) {
                    throw new ExecException(String.format("%s finished with non-zero exit value.", StringUtils.capitalize(displayName)));
                }
                return this;
            }
        };
    }

    void started() {
        ShutdownHookActionRegister.addAction(shutdownHookAction);

        setState(ExecHandleState.STARTED);
        broadcast.getSource().executionStarted(this);
    }

    void finished(int exitCode) {
        if (exitCode != 0) {
            setEndStateInfo(ExecHandleState.FAILED, exitCode, null);
        } else {
            setEndStateInfo(ExecHandleState.SUCCEEDED, 0, null);
        }
    }

    void aborted(int exitCode) {
        if (exitCode == 0) {
            // This can happen on windows
            exitCode = -1;
        }
        setEndStateInfo(ExecHandleState.ABORTED, exitCode, null);
    }

    void failed(Throwable failureCause) {
        setEndStateInfo(ExecHandleState.FAILED, -1, failureCause);
    }

    public void addListener(ExecHandleListener listener) {
        broadcast.add(listener);
    }

    public void removeListener(ExecHandleListener listener) {
        broadcast.remove(listener);
    }
}
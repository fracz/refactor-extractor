/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.launcher.daemon.client;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.specs.Spec;
import org.gradle.initialization.BuildClientMetaData;
import org.gradle.initialization.GradleLauncherAction;
import org.gradle.internal.UncheckedException;
import org.gradle.launcher.daemon.context.DaemonContext;
import org.gradle.launcher.daemon.diagnostics.DaemonDiagnostics;
import org.gradle.launcher.daemon.logging.DaemonMessages;
import org.gradle.launcher.daemon.protocol.*;
import org.gradle.launcher.exec.BuildActionParameters;
import org.gradle.launcher.exec.GradleLauncherActionExecuter;
import org.gradle.logging.internal.OutputEvent;
import org.gradle.logging.internal.OutputEventListener;
import org.gradle.messaging.remote.internal.Connection;
import org.gradle.util.GFileUtils;

import java.io.InputStream;

/**
 * The client piece of the build daemon.
 * <p>
 * Immediately upon forming a connection, the daemon may send {@link OutputEvent} messages back to the client and may do so
 * for as long as the connection is open.
 * <p>
 * The client is expected to send exactly one {@link Build} message as the first message it sends to the daemon. The daemon
 * may either return {@link DaemonBusy} or {@link BuildStarted}. If the former is received, the client should not send any more
 * messages to this daemon. If the latter is received, the client can assume the daemon is performing the build. The client may then
 * send zero to many {@link ForwardInput} messages. If the client's stdin stream is closed before the connection to the
 * daemon is terminated, the client must send a {@link CloseInput} command to instruct the daemon that no more input is to be
 * expected.
 * <p>
 * After receiving the {@link Result} message (after a {@link BuildStarted} mesage), the client must send a {@link CloseInput}
 * command if it has not already done so due to the stdin stream being closed. At this point the client is expected to
 * terminate the connection with the daemon.
 * <p>
 * If the daemon returns a {@code null} message before returning a {@link Result} object, it has terminated unexpectedly for some reason.
 */
public class DaemonClient implements GradleLauncherActionExecuter<BuildActionParameters> {
    private static final Logger LOGGER = Logging.getLogger(DaemonClient.class);
    protected final DaemonConnector connector;
    protected final BuildClientMetaData clientMetaData;
    private final OutputEventListener outputEventListener;
    private final Spec<DaemonContext> compatibilitySpec;
    private final InputStream buildStandardInput;

    //TODO SF - outputEventListener and buildStandardInput are per-build settings
    //so down the road we should refactor the code accordingly and potentially attach them to BuildActionParameters
    public DaemonClient(DaemonConnector connector, BuildClientMetaData clientMetaData, OutputEventListener outputEventListener,
                        Spec<DaemonContext> compatibilitySpec, InputStream buildStandardInput) {
        this.connector = connector;
        this.clientMetaData = clientMetaData;
        this.outputEventListener = outputEventListener;
        this.compatibilitySpec = compatibilitySpec;
        this.buildStandardInput = buildStandardInput;
    }

    /**
     * Stops all daemons, if any is running.
     */
    public void stop() {
        DaemonConnection connection = connector.maybeConnect(compatibilitySpec);
        if (connection == null) {
            LOGGER.lifecycle(DaemonMessages.NO_DAEMONS_RUNNING);
            return;
        }

        LOGGER.lifecycle("Stopping daemon(s).");
        //iterate and stop all daemons
        while (connection != null) {
            new StopDispatcher().dispatch(clientMetaData, connection.getConnection());
            LOGGER.lifecycle("Gradle daemon stopped.");
            connection = connector.maybeConnect(compatibilitySpec);
        }
    }

    /**
     * Executes the given action in the daemon. The action and parameters must be serializable.
     *
     * @param action The action
     * @throws org.gradle.launcher.exec.ReportedException On failure, when the failure has already been logged/reported.
     */
    public <T> T execute(GradleLauncherAction<T> action, BuildActionParameters parameters) {
        LOGGER.warn("Note: the Gradle build daemon is an experimental feature.");
        LOGGER.warn("As such, you may experience unexpected build failures. You may need to occasionally stop the daemon.");
        Build build = new Build(action, parameters);
        int saneNumberOfAttempts = 100; //is it sane enough?
        for(int i=1; i<saneNumberOfAttempts; i++) {
            DaemonConnection daemonConnection = connector.connect(compatibilitySpec);
            Connection<Object> connection = daemonConnection.getConnection();

            Object firstResult;
            try {
                LOGGER.info("Connected to the daemon. Dispatching {} request.", build);
                connection.dispatch(build);
                firstResult = connection.receive();
            } catch (Exception e) {
                LOGGER.info("Exception when attempted to send and receive first result from the daemon. I will try a different daemon... The exception was:", e);
                continue;
            }

            if (firstResult instanceof BuildStarted) {
                DaemonDiagnostics diagnostics = ((BuildStarted) firstResult).getDiagnostics();
                return (T) monitorBuild(build, diagnostics, connection).getValue();
            } else if (firstResult instanceof DaemonBusy) {
                LOGGER.info("The daemon we connected to was busy. Trying a different daemon...");
            } else if (firstResult instanceof Failure) {
                // Could potentially distinguish between CommandFailure and DaemonFailure here.
                throw UncheckedException.throwAsUncheckedException(((Failure) firstResult).getValue());
            } else if (firstResult == null) {
                LOGGER.info("The first result from the daemon was empty. Most likely the daemon has died. Trying a different daemon...");
            } else {
                throw new IllegalStateException(String.format(
                    "The first result from the Daemon: %s is a Result of a type we don't have a strategy to handle."
                    + "Earlier, %s request was sent to the daemon.", firstResult, build));
            }
        }
        throw new NoUsableDaemonFoundException("Unable to find a usable idle daemon. I have connected to "
                + saneNumberOfAttempts + " different daemons but I could not use any of them to run build: " + build + ".");
    }

    protected Result monitorBuild(Build build, DaemonDiagnostics diagnostics, Connection<Object> connection) {
        DaemonClientInputForwarder inputForwarder = new DaemonClientInputForwarder(buildStandardInput, build.getClientMetaData(), connection);
        try {
            inputForwarder.start();
            int objectsReceived = 0;

            while (true) {
                Object object = connection.receive();
                LOGGER.trace("Received object #{}, type: {}", objectsReceived++, object == null ? null : object.getClass().getName());

                if (object == null) {
                    return handleDaemonDisappearance(build, diagnostics);
                } else if (object instanceof Failure) {
                    // Could potentially distinguish between CommandFailure and DaemonFailure here.
                    throw UncheckedException.throwAsUncheckedException(((Failure) object).getValue());
                } else if (object instanceof OutputEvent) {
                    outputEventListener.onOutput((OutputEvent) object);
                } else if (object instanceof Result) {
                    return (Result) object;
                } else {
                    throw new IllegalStateException(String.format("Daemon returned %s (type: %s) as for which there is no strategy to handle", object, object.getClass()));
                }
            }
        } finally {
            inputForwarder.stop();
            connection.stop();
        }
    }

    private Result handleDaemonDisappearance(Build build, DaemonDiagnostics diagnostics) {
        //we can try sending something to the daemon and try out if he is really dead or use jps
        //if he's really dead we should deregister it if it is not already deregistered.
        //if the daemon is not dead we might continue receiving from him (and try to find the bug in messaging infrastructure)
        int daemonLogLines = 20;
        LOGGER.error("The message received from the daemon indicates that the daemon has disappeared."
                + "\nDaemon pid: " + diagnostics.getPid()
                + "\nDaemon log: " + diagnostics.getDaemonLog()
                + "\nBuild request sent: " + build
                + "\nAttempting to read last " + daemonLogLines + " lines from the daemon log...");

        try {
            String tail = GFileUtils.tail(diagnostics.getDaemonLog(), daemonLogLines);
            LOGGER.error("Last " + daemonLogLines + " lines from " + diagnostics.getDaemonLog().getName() + ":"
                    + "\n----------\n" + tail + "----------\n");
        } catch (GFileUtils.TailReadingException e) {
            LOGGER.error("Unable to read from the daemon log file because of: " + e);
            LOGGER.debug("Problem reading the daemon log file.", e);
        }

        throw new DaemonDisappearedException();
    }
}
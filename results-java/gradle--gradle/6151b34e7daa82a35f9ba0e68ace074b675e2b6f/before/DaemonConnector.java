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
package org.gradle.launcher;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.internal.DefaultClassPathRegistry;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.initialization.DefaultCommandLineConverter;
import org.gradle.messaging.concurrent.CompositeStoppable;
import org.gradle.messaging.concurrent.DefaultExecutorFactory;
import org.gradle.messaging.concurrent.Stoppable;
import org.gradle.messaging.remote.Address;
import org.gradle.messaging.remote.ConnectEvent;
import org.gradle.messaging.remote.internal.ConnectException;
import org.gradle.messaging.remote.internal.Connection;
import org.gradle.messaging.remote.internal.DefaultMessageSerializer;
import org.gradle.messaging.remote.internal.inet.InetAddressFactory;
import org.gradle.messaging.remote.internal.inet.TcpIncomingConnector;
import org.gradle.messaging.remote.internal.inet.TcpOutgoingConnector;
import org.gradle.util.GUtil;
import org.gradle.util.Jvm;
import org.gradle.util.UUIDGenerator;
import org.gradle.util.UncheckedException;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DaemonConnector {
    private static final Logger LOGGER = Logging.getLogger(DaemonConnector.class);
    private final File userHomeDir;
    private DaemonRegistry daemonRegistry;
    private final int idleDaemonTimeout;

    public DaemonConnector(File userHomeDir) {
        this(userHomeDir, 3 * 60 * 60 * 1000);
    }

    DaemonConnector(File userHomeDir, int idleDaemonTimeout) {
        this.userHomeDir = userHomeDir;
        this.daemonRegistry = new DaemonRegistry(userHomeDir);
        this.idleDaemonTimeout = idleDaemonTimeout;
    }

    /**
     * Attempts to connect to the daemon, if it is running.
     *
     * @param idleOnly - get connections only to idle daemons. TODO SF - this is ugly. Refactor.
     * @return The connection, or null if not running.
     */
    public Connection<Object> maybeConnect(boolean idleOnly) {
        List<DaemonStatus> statuses = idleOnly? daemonRegistry.getIdle() : daemonRegistry.getAll();
        for (DaemonStatus status : statuses) {
            try {
                return new TcpOutgoingConnector<Object>(new DefaultMessageSerializer<Object>(getClass().getClassLoader())).connect(status.getAddress());
            } catch (ConnectException e) {
                // Ignore
            }
        }
        return null;
    }

    /**
     * Connects to the daemon, starting it if required.
     *
     * @return The connection. Never returns null.
     */
    public Connection<Object> connect() {
        Connection<Object> connection = maybeConnect(true);
        if (connection != null) {
            return connection;
        }

        LOGGER.info("Starting Gradle daemon");
        startDaemon();
        Date expiry = new Date(System.currentTimeMillis() + 30000L);
        do {
            connection = maybeConnect(true);
            if (connection != null) {
                return connection;
            }
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                throw UncheckedException.asUncheckedException(e);
            }
        } while (System.currentTimeMillis() < expiry.getTime());

        throw new GradleException("Timeout waiting to connect to Gradle daemon.");
    }

    private void startDaemon() {
        List<String> daemonArgs = new ArrayList<String>();
        daemonArgs.add(Jvm.current().getJavaExecutable().getAbsolutePath());
        daemonArgs.add("-Xmx1024m");
        daemonArgs.add("-XX:MaxPermSize=256m");
        daemonArgs.add("-cp");
        daemonArgs.add(GUtil.join(new DefaultClassPathRegistry().getClassPathFiles("GRADLE_RUNTIME"),
                File.pathSeparator));
        daemonArgs.add(GradleDaemon.class.getName());
        daemonArgs.add(String.format("-%s", DefaultCommandLineConverter.GRADLE_USER_HOME));
        daemonArgs.add(userHomeDir.getAbsolutePath());
        DaemonStartAction daemon = new DaemonStartAction();
        daemon.args(daemonArgs);
        daemon.workingDir(userHomeDir);
        daemon.start();
    }

    /**
     * Starts accepting connections.
     *
     * @param handler The handler for connections.
     */
    void accept(final IncomingConnectionHandler handler) {
        DefaultExecutorFactory executorFactory = new DefaultExecutorFactory();
        TcpIncomingConnector<Object> incomingConnector = new TcpIncomingConnector<Object>(executorFactory, new DefaultMessageSerializer<Object>(getClass().getClassLoader()), new InetAddressFactory(), new UUIDGenerator());

        final DaemonRegistry.Registry registry = daemonRegistry.newRegistry();

        final CompletionHandler finished = new CompletionHandler(idleDaemonTimeout, registry);

        LOGGER.lifecycle("Awaiting requests.");


        Action<ConnectEvent<Connection<Object>>> connectEvent = new Action<ConnectEvent<Connection<Object>>>() {
            public void execute(ConnectEvent<Connection<Object>> connectionConnectEvent) {
                handler.handle(connectionConnectEvent.getConnection(), finished);
            }
        };
        Address address = incomingConnector.accept(connectEvent, false);

        registry.store(address);

        boolean stopped = finished.awaitStop();
        if (!stopped) {
            LOGGER.lifecycle("Time-out waiting for requests. Stopping.");
        }
        registry.remove();
        new CompositeStoppable(incomingConnector, executorFactory).stop();
    }

    DaemonRegistry getDaemonRegistry() {
        return daemonRegistry;
    }

    public static class CompletionHandler implements Stoppable {

        private final Lock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();
        private boolean running;
        private boolean stopped;
        private long expiry;
        private final int idleDaemonTimeout;
        private final DaemonRegistry.Registry registryFile;

        CompletionHandler(int idleDaemonTimeout, DaemonRegistry.Registry registryFile) {
            this.idleDaemonTimeout = idleDaemonTimeout;
            this.registryFile = registryFile;
            resetTimer();
        }

        /**
         * Waits until stopped, or timeout.
         *
         * @return true if stopped, false if timeout
         */
        public boolean awaitStop() {
            lock.lock();
            try {
                while (running || (!stopped && System.currentTimeMillis() < expiry)) {
                    try {
                        if (running) {
                            condition.await();
                        } else {
                            condition.awaitUntil(new Date(expiry));
                        }
                    } catch (InterruptedException e) {
                        throw UncheckedException.asUncheckedException(e);
                    }
                }
                assert !running;
                return stopped;
            } finally {
                lock.unlock();
            }
        }

        public void stop() {
            lock.lock();
            try {
                stopped = true;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public void onStartActivity() {
            lock.lock();
            try {
                assert !running;
                running = true;
                condition.signalAll();
                registryFile.markBusy();
            } finally {
                lock.unlock();
            }
        }

        public void onActivityComplete() {
            lock.lock();
            try {
                assert running;
                running = false;
                resetTimer();
                condition.signalAll();
                registryFile.markIdle();
            } finally {
                lock.unlock();
            }
        }

        private void resetTimer() {
            expiry = System.currentTimeMillis() + idleDaemonTimeout;
        }
    }
}
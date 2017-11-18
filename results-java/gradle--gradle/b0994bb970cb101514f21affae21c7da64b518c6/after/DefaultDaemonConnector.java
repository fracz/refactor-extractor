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

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.specs.Spec;
import org.gradle.internal.UncheckedException;
import org.gradle.launcher.daemon.context.DaemonContext;
import org.gradle.launcher.daemon.diagnostics.DaemonProcessInfo;
import org.gradle.launcher.daemon.registry.DaemonInfo;
import org.gradle.launcher.daemon.registry.DaemonRegistry;
import org.gradle.messaging.remote.internal.ConnectException;
import org.gradle.messaging.remote.internal.OutgoingConnector;

import java.util.List;

/**
 * Provides the mechanics of connecting to a daemon, starting one via a given runnable if no suitable daemons are already available.
 */
public class DefaultDaemonConnector implements DaemonConnector {
    private static final Logger LOGGER = Logging.getLogger(DefaultDaemonConnector.class);
    public static final int DEFAULT_CONNECT_TIMEOUT = 30000;
    private final DaemonRegistry daemonRegistry;
    private final OutgoingConnector<Object> connector;
    private final DaemonStarter daemonStarter;
    private long connectTimeout = DefaultDaemonConnector.DEFAULT_CONNECT_TIMEOUT;

    public DefaultDaemonConnector(DaemonRegistry daemonRegistry, OutgoingConnector<Object> connector, DaemonStarter daemonStarter) {
        this.daemonRegistry = daemonRegistry;
        this.connector = connector;
        this.daemonStarter = daemonStarter;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public DaemonRegistry getDaemonRegistry() {
        return daemonRegistry;
    }

    public DaemonConnection maybeConnect(Spec<? super DaemonContext> constraint) {
        return findConnection(daemonRegistry.getAll(), constraint);
    }

    public DaemonConnection connect(Spec<? super DaemonContext> constraint) {
        DaemonConnection connection = findConnection(daemonRegistry.getIdle(), constraint);
        if (connection != null) {
            return connection;
        }

        return createConnection();
    }

    private DaemonConnection findConnection(List<DaemonInfo> daemonInfos, Spec<? super DaemonContext> constraint) {
        for (DaemonInfo daemonInfo : daemonInfos) {
            if (!constraint.isSatisfiedBy(daemonInfo.getContext())) {
                LOGGER.debug("Found daemon (address: {}, idle: {}) however it's context does not match the desired criteria.\n"
                        + "  Wanted: {}.\n"
                        + "  Found:  {}.\n"
                        + "  Looking for a different daemon...", daemonInfo.getAddress(), daemonInfo.isIdle(), constraint, daemonInfo.getContext());
                continue;
            }

            try {
                //TODO SF some overlap between the DaemonConnection and DaemonProcessInfo
                return connectToDaemon(daemonInfo, new DaemonProcessInfo(daemonInfo.getContext().getUid(), null));
            } catch (ConnectException e) {
                //this means the daemon died without removing its address from the registry
                //we can safely remove this address now
                LOGGER.debug("We cannot connect to the daemon at " + daemonInfo.getAddress() + " due to " + e + ". "
                        + "We will not remove this daemon from the registry because the connection issue may have been temporary.");
                //TODO it might be good to store in the registry the number of failed attempts to connect to the deamon
                //if the number is high we may decide to remove the daemon from the registry
                //daemonRegistry.remove(address);
            }
        }
        return null;
    }

    public DaemonConnection createConnection() {
        LOGGER.info("Starting Gradle daemon");
        final DaemonProcessInfo processInfo = daemonStarter.startDaemon();
        LOGGER.debug("Started Gradle Daemon: {}", processInfo);
        long expiry = System.currentTimeMillis() + connectTimeout;
        do {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                throw UncheckedException.throwAsUncheckedException(e);
            }
            DaemonConnection daemonConnection = connectToDaemonWithId(processInfo);
            if (daemonConnection != null) {
                return daemonConnection;
            }
        } while (System.currentTimeMillis() < expiry);

        throw new GradleException("Timeout waiting to connect to Gradle daemon.\n" + processInfo.describe());
    }

    private DaemonConnection connectToDaemonWithId(DaemonProcessInfo processInfo) throws ConnectException {
        // Look for 'our' daemon among the busy daemons - a daemon will start in busy state so that nobody else will grab it.
        for (DaemonInfo daemonInfo : daemonRegistry.getBusy()) {
            if (daemonInfo.getContext().getUid().equals(processInfo.getUid())) {
                try {
                    // TODO:DAZ We should verify the connection using the original daemon constraint
                    return connectToDaemon(daemonInfo, processInfo);
                } catch (ConnectException e) {
                    // this means the daemon died without removing its address from the registry
                    // since we have never successfully connected we assume the daemon is dead and remove this address now
                    daemonRegistry.remove(daemonInfo.getAddress());
                    throw new GradleException("The forked daemon process died before we could connect.\n" + processInfo.describe());
                    //TODO SF after the refactorings add some coverage for visibility of the daemon tail.
                }
            }
        }
        return null;
    }

    private DaemonConnection connectToDaemon(DaemonInfo daemonInfo, DaemonProcessInfo processInfo) {
        return new DaemonConnection(daemonInfo.getContext().getUid(), connector.connect(daemonInfo.getAddress()), daemonInfo.getPassword(), processInfo);
    }
}
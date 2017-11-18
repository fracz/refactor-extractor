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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.gradle.api.internal.specs.ExplainingSpec;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.specs.Spec;
import org.gradle.internal.Pair;
import org.gradle.internal.SystemProperties;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.remote.internal.ConnectException;
import org.gradle.internal.remote.internal.OutgoingConnector;
import org.gradle.internal.remote.internal.RemoteConnection;
import org.gradle.internal.serialize.Serializers;
import org.gradle.launcher.daemon.context.DaemonConnectDetails;
import org.gradle.launcher.daemon.context.DaemonContext;
import org.gradle.launcher.daemon.diagnostics.DaemonStartupInfo;
import org.gradle.launcher.daemon.logging.DaemonMessages;
import org.gradle.launcher.daemon.protocol.DaemonMessageSerializer;
import org.gradle.launcher.daemon.protocol.Message;
import org.gradle.launcher.daemon.registry.DaemonInfo;
import org.gradle.launcher.daemon.registry.DaemonRegistry;
import org.gradle.launcher.daemon.registry.DaemonStopEvent;
import org.gradle.util.CollectionUtils;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides the mechanics of connecting to a daemon, starting one via a given runnable if no suitable daemons are already available.
 */
public class DefaultDaemonConnector implements DaemonConnector {
    private static final Logger LOGGER = Logging.getLogger(DefaultDaemonConnector.class);
    public static final int DEFAULT_CONNECT_TIMEOUT = 30000;
    public static final String STARTING_DAEMON_MESSAGE = "Starting a new Gradle Daemon because";
    public static final String ONE_BUSY_DAEMON_MESSAGE = "Another daemon is busy";
    public static final String MULTIPLE_BUSY_DAEMONS_MESSAGE = "Multiple daemons are busy";
    public static final String ONE_INCOMPATIBLE_DAEMON_MESSAGE = "An idle daemon with different constraints could not be used";
    public static final String MULTIPLE_INCOMPATIBLE_DAEMONS_MESSAGE = "Multiple idle daemons with different constraints could not be used";
    public static final String SUBSEQUENT_BUILDS_FASTER_MESSAGE = "Subsequent builds will be faster";
    public static final String DAEMON_WAS_STOPPED_PREFIX = "A daemon was stopped ";
    private static final String LINE_SEPARATOR = SystemProperties.getInstance().getLineSeparator();
    private final DaemonRegistry daemonRegistry;
    protected final OutgoingConnector connector;
    private final DaemonStarter daemonStarter;
    private final DaemonStartListener startListener;
    private long connectTimeout = DefaultDaemonConnector.DEFAULT_CONNECT_TIMEOUT;

    public DefaultDaemonConnector(DaemonRegistry daemonRegistry, OutgoingConnector connector, DaemonStarter daemonStarter, DaemonStartListener startListener) {
        this.daemonRegistry = daemonRegistry;
        this.connector = connector;
        this.daemonStarter = daemonStarter;
        this.startListener = startListener;
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

    public DaemonClientConnection maybeConnect(ExplainingSpec<DaemonContext> constraint) {
        return findConnection(getCompatibleDaemons(daemonRegistry.getAll(), constraint));
    }

    public DaemonClientConnection maybeConnect(DaemonConnectDetails daemon) {
        try {
            return connectToDaemon(daemon, new CleanupOnStaleAddress(daemon, true));
        } catch (ConnectException e) {
            LOGGER.debug("Cannot connect to daemon {} due to {}. Ignoring.", daemon, e);
        }
        return null;
    }

    public DaemonClientConnection connect(ExplainingSpec<DaemonContext> constraint) {
        final Collection<DaemonInfo> allDaemons = daemonRegistry.getAll();
        final Pair<Collection<DaemonInfo>, Collection<DaemonInfo>> idleBusy = CollectionUtils.partition(allDaemons, new Spec<DaemonInfo>() {
            public boolean isSatisfiedBy(DaemonInfo element) {
                return element.isIdle();
            }
        });
        final Collection<DaemonInfo> idleDaemons = idleBusy.getLeft();
        final Collection<DaemonInfo> busyDaemons = idleBusy.getRight();

        final List<DaemonInfo> compatibleIdleDaemons = getCompatibleDaemons(idleDaemons, constraint);
        DaemonClientConnection connection = findConnection(compatibleIdleDaemons);
        if (connection != null) {
            return connection;
        }

        // Remove the stop events we're about to display
        final List<DaemonStopEvent> stopEvents = daemonRegistry.getStopEvents();
        daemonRegistry.removeStopEvents(stopEvents);

        LOGGER.lifecycle(generateStartingMessage(busyDaemons.size(), idleDaemons.size(), stopEvents));

        return startDaemon(constraint);
    }

    @VisibleForTesting
    String generateStartingMessage(final int numBusy, final int numIncompatible, final List<DaemonStopEvent> stopEvents) {
        final List<String> messages = Lists.newArrayList();
        if (numBusy + numIncompatible == 0) {
            messages.add(STARTING_DAEMON_MESSAGE + " none are running");
        } else {
            messages.add(STARTING_DAEMON_MESSAGE);
            if (numBusy > 0) {
                messages.add(numBusy > 1 ? MULTIPLE_BUSY_DAEMONS_MESSAGE : ONE_BUSY_DAEMON_MESSAGE);
            }
            if (numIncompatible > 0) {
                messages.add(numIncompatible > 1 ? MULTIPLE_INCOMPATIBLE_DAEMONS_MESSAGE : ONE_INCOMPATIBLE_DAEMON_MESSAGE);
            }
        }

        if (stopEvents.size() > 0) {
            for (DaemonStopEvent event : stopEvents) {
                messages.add(DAEMON_WAS_STOPPED_PREFIX + event.getReason());
            }
        }
        messages.add(SUBSEQUENT_BUILDS_FASTER_MESSAGE);

        return Joiner.on(LINE_SEPARATOR + "   ").skipNulls().join(messages);
    }

    private List<DaemonInfo> getCompatibleDaemons(Iterable<DaemonInfo> daemons, ExplainingSpec<DaemonContext> constraint) {
        List<DaemonInfo> compatibleDaemons = new LinkedList<DaemonInfo>();
        for (DaemonInfo daemon : daemons) {
            if (constraint.isSatisfiedBy(daemon.getContext())) {
                compatibleDaemons.add(daemon);
            } else {
                LOGGER.info("Found daemon {} however its context does not match the desired criteria.\n"
                    + constraint.whyUnsatisfied(daemon.getContext()) + "\n"
                    + "  Looking for a different daemon...", daemon);
            }
        }
        return compatibleDaemons;
    }

    private DaemonClientConnection findConnection(List<DaemonInfo> compatibleDaemons) {
        for (DaemonInfo daemon : compatibleDaemons) {
            try {
                return connectToDaemon(daemon, new CleanupOnStaleAddress(daemon, true));
            } catch (ConnectException e) {
                LOGGER.debug("Cannot connect to daemon {} due to {}. Trying a different daemon...", daemon, e);
            }
        }
        return null;
    }

    public DaemonClientConnection startDaemon(ExplainingSpec<DaemonContext> constraint) {
        final DaemonStartupInfo startupInfo = daemonStarter.startDaemon();
        LOGGER.debug("Started Gradle daemon {}", startupInfo);
        long expiry = System.currentTimeMillis() + connectTimeout;
        do {
            DaemonClientConnection daemonConnection = connectToDaemonWithId(startupInfo, constraint);
            if (daemonConnection != null) {
                startListener.daemonStarted(daemonConnection.getDaemon());
                return daemonConnection;
            }
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                throw UncheckedException.throwAsUncheckedException(e);
            }
        } while (System.currentTimeMillis() < expiry);

        throw new DaemonConnectionException("Timeout waiting to connect to the Gradle daemon.\n" + startupInfo.describe());
    }

    private DaemonClientConnection connectToDaemonWithId(DaemonStartupInfo daemon, ExplainingSpec<DaemonContext> constraint) throws ConnectException {
        // Look for 'our' daemon among the busy daemons - a daemon will start in busy state so that nobody else will grab it.
        for (DaemonInfo daemonInfo : daemonRegistry.getBusy()) {
            if (daemonInfo.getUid().equals(daemon.getUid())) {
                try {
                    if (!constraint.isSatisfiedBy(daemonInfo.getContext())) {
                        throw new DaemonConnectionException("The newly created daemon process has a different context than expected."
                            + "\nIt won't be possible to reconnect to this daemon. Context mismatch: "
                            + "\n" + constraint.whyUnsatisfied(daemonInfo.getContext()));
                    }
                    return connectToDaemon(daemonInfo, new CleanupOnStaleAddress(daemonInfo, false));
                } catch (ConnectException e) {
                    throw new DaemonConnectionException("Could not connect to the Gradle daemon.\n" + daemon.describe(), e);
                }
            }
        }
        return null;
    }

    private DaemonClientConnection connectToDaemon(DaemonConnectDetails daemon, DaemonClientConnection.StaleAddressDetector staleAddressDetector) throws ConnectException {
        RemoteConnection<Message> connection;
        try {
            connection = connector.connect(daemon.getAddress()).create(Serializers.stateful(DaemonMessageSerializer.create()));
        } catch (ConnectException e) {
            staleAddressDetector.maybeStaleAddress(e);
            throw e;
        }
        return new DaemonClientConnection(connection, daemon, staleAddressDetector);
    }

    private class CleanupOnStaleAddress implements DaemonClientConnection.StaleAddressDetector {
        private final DaemonConnectDetails daemon;
        private final boolean exposeAsStale;

        public CleanupOnStaleAddress(DaemonConnectDetails daemon, boolean exposeAsStale) {
            this.daemon = daemon;
            this.exposeAsStale = exposeAsStale;
        }

        public boolean maybeStaleAddress(Exception failure) {
            LOGGER.info("{}{}", DaemonMessages.REMOVING_DAEMON_ADDRESS_ON_FAILURE, daemon);
            final Date timestamp = new Date(System.currentTimeMillis());
            daemonRegistry.storeStopEvent(new DaemonStopEvent(timestamp, "daemon was terminated"));
            daemonRegistry.remove(daemon.getAddress());
            return exposeAsStale;
        }
    }
}
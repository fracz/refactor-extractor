/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.messaging.remote.internal.hub;

import org.gradle.api.Action;
import org.gradle.internal.concurrent.AsyncStoppable;
import org.gradle.internal.concurrent.ExecutorFactory;
import org.gradle.internal.concurrent.StoppableExecutor;
import org.gradle.messaging.dispatch.Dispatch;
import org.gradle.messaging.remote.internal.Connection;
import org.gradle.messaging.remote.internal.hub.protocol.*;
import org.gradle.messaging.remote.internal.hub.queue.EndPointQueue;
import org.gradle.messaging.remote.internal.hub.queue.MultiChannelQueue;
import org.gradle.messaging.remote.internal.hub.queue.MultiEndPointQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A multi-channel message dispatcher.
 */
public class MessageHub implements AsyncStoppable {
    private enum State {Running, Stopping, Stopped}

    private static final Discard DISCARD = new Discard();
    private final StoppableExecutor workers;
    private final String displayName;
    private final Action<? super Throwable> errorHandler;
    private final Lock lock = new ReentrantLock();
    private State state = State.Running;
    private final IncomingQueue incomingQueue = new IncomingQueue(lock);
    private final OutgoingQueue outgoingQueue = new OutgoingQueue(incomingQueue, lock);

    /**
     * @param errorHandler Notified when some asynch. activity fails. Must be thread-safe.
     */
    public MessageHub(String displayName, ExecutorFactory executorFactory, Action<? super Throwable> errorHandler) {
        this.displayName = displayName;
        this.errorHandler = errorHandler;
        workers = executorFactory.create(String.format("%s workers", displayName));
    }

    /**
     * <p>Adds a {@link Dispatch} implementation that can be used to send outgoing messages on the given channel. The returned value is thread-safe.</p>
     *
     * <p>All messages sent via the dispatch are forwarded to exactly one connection.</p>
     */
    public <T> Dispatch<T> getOutgoing(final String channelName, final Class<T> type) {
        lock.lock();
        try {
            assertRunning("create outgoing dispatch");
            return new ChannelDispatch<T>(type, new ChannelIdentifier(channelName));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds a handler for messages on the given channel. The handler may implement any of the following:
     *
     * <ul>
     *
     * <li>{@link Dispatch} to handle incoming messages received from any connections attached to this hub. Each incoming message is passed to exactly one handler associated to the given channel.
     * </li>
     *
     * <li>{@link RejectedMessageListener} to receive notifications of outgoing messages that cannot be sent on the given channel.</li>
     *
     * <li>{@link HubStateListener} to receive notifications of state changes to this hub.</li>
     *
     * </ul>
     *
     * <p>The given handler does not need to be thread-safe, and is notified by at most one thread at a time. Multiple handlers can be added for a given channel.</p>
     *
     * <p>NOTE: If any method of the handler fails with an exception, the handler is discarded and will receive no further notifications.</p>
     */
    public void addHandler(String channelName, Object handler) {
        lock.lock();
        try {
            assertRunning("add handler");

            RejectedMessageListener rejectedMessageListener;
            if (handler instanceof RejectedMessageListener) {
                rejectedMessageListener = (RejectedMessageListener) handler;
            } else {
                rejectedMessageListener = DISCARD;
            }
            Dispatch<Object> dispatch;
            if (handler instanceof Dispatch) {
                dispatch = (Dispatch) handler;
            } else {
                dispatch = DISCARD;
            }
            HubStateListener stateListener;
            if (handler instanceof HubStateListener) {
                stateListener = (HubStateListener) handler;
            } else {
                stateListener = DISCARD;
            }
            ChannelIdentifier identifier = new ChannelIdentifier(channelName);
            EndPointQueue queue = incomingQueue.getChannel(identifier).newEndpoint();
            workers.execute(new Handler(queue, dispatch, rejectedMessageListener, stateListener));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds a connection to some other message hub. Outgoing messages are forwarded to this connection, and incoming messages are received from it.
     */
    public void addConnection(Connection<InterHubMessage> connection) {
        lock.lock();
        try {
            assertRunning("add connection");
            incomingQueue.queue(new ConnectionEstablished());
            EndPointQueue queue = outgoingQueue.newEndpoint();
            workers.execute(new ConnectionDispatch(connection, queue));
            workers.execute(new ConnectionReceive(connection));
        } finally {
            lock.unlock();
        }
    }

    private void assertRunning(String action) {
        if (state != State.Running) {
            throw new IllegalStateException(String.format("Cannot %s, as %s has been stopped.", action, displayName));
        }
    }

    /**
     * Requests that this message hub commence shutting down. Does the following:
     *
     * <ul>
     *
     * <li>Stops accepting any further outgoing messages.</li>
     *
     * <li>If no connections are available, dispatches queued messages to any handlers that implement {@link RejectedMessageListener}.</li>
     *
     * </ul>
     */
    public void requestStop() {
        lock.lock();
        try {
            if (state != State.Running) {
                return;
            }
            try {
                outgoingQueue.requestStop();
                incomingQueue.requestStop();
            } finally {
                state = State.Stopping;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Requests that this message hub stop. First requests stop as per {@link #requestStop()}, then blocks until stop has completed. This means that:
     *
     * <ul>
     *
     * <li>All calls to {@link Dispatch#dispatch(Object)} for outgoing messages have returned.</li>
     *
     * <li>All dispatches to handlers have completed.</li>
     *
     * <li>All internal threads have completed.</li>
     *
     * </ul>
     */
    public void stop() {
        try {
            requestStop();
            workers.stop();
        } finally {
            lock.lock();
            try {
                state = State.Stopped;
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * NOTE: must be holding lock to use this class.
     */
    private static class OutgoingQueue extends MultiEndPointQueue {
        private boolean connected;
        private final IncomingQueue incomingQueue;

        private OutgoingQueue(IncomingQueue incomingQueue, Lock lock) {
            super(lock);
            this.incomingQueue = incomingQueue;
        }

        void requestStop() {
            if (!connected) {
                List<InterHubMessage> rejected = new ArrayList<InterHubMessage>();
                drain(rejected);
                for (InterHubMessage message : rejected) {
                    if (message instanceof ChannelMessage) {
                        ChannelMessage channelMessage = (ChannelMessage) message;
                        incomingQueue.queue(new RejectedMessage(channelMessage.getChannel(), channelMessage.getPayload()));
                    }
                }
            }
            queue(new EndOfStream());
        }

        @Override
        public EndPointQueue newEndpoint() {
            this.connected = true;
            return super.newEndpoint();
        }
    }

    /**
     * NOTE: must be holding lock to use this class.
     */
    private static class IncomingQueue extends MultiChannelQueue {
        private IncomingQueue(Lock lock) {
            super(lock);
        }

        public void requestStop() {
            queue(new EndOfStream());
        }
    }

    private static class Discard implements Dispatch<Object>, RejectedMessageListener, HubStateListener {
        public void dispatch(Object message) {
        }

        public void messageDiscarded(Object message) {
        }

        public void onConnect() {
        }

        public void onDisconnect() {
        }
    }

    private class ConnectionReceive implements Runnable {
        private final Connection<InterHubMessage> connection;

        public ConnectionReceive(Connection<InterHubMessage> connection) {
            this.connection = connection;
        }

        public void run() {
            while (true) {
                InterHubMessage message = connection.receive();
                if (message == null) {
                    return;
                }
                lock.lock();
                try {
                    incomingQueue.queue(message);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    private class ConnectionDispatch implements Runnable {
        private final Connection<InterHubMessage> connection;
        private final EndPointQueue queue;

        private ConnectionDispatch(Connection<InterHubMessage> connection, EndPointQueue queue) {
            this.connection = connection;
            this.queue = queue;
        }

        public void run() {
            try {
                List<InterHubMessage> messages = new ArrayList<InterHubMessage>();
                while (true) {
                    lock.lock();
                    try {
                        queue.take(messages);
                    } finally {
                        lock.unlock();
                    }
                    for (Object message : messages) {
                        if (message instanceof EndOfStream) {
                            return;
                        }
                        InterHubMessage channelMessage = (InterHubMessage) message;
                        connection.dispatch(channelMessage);
                    }
                    messages.clear();
                }
            } catch (Throwable t) {
                errorHandler.execute(t);
            }
        }
    }

    private class ChannelDispatch<T> implements Dispatch<T> {
        private final Class<T> type;
        private final ChannelIdentifier channelIdentifier;

        public ChannelDispatch(Class<T> type, ChannelIdentifier channelIdentifier) {
            this.type = type;
            this.channelIdentifier = channelIdentifier;
        }

        @Override
        public String toString() {
            return String.format("Dispatch %s to %s channel %s", type.getSimpleName(), displayName, channelIdentifier);
        }

        public void dispatch(T message) {
            lock.lock();
            try {
                assertRunning("dispatch message");
                outgoingQueue.queue(new ChannelMessage(channelIdentifier, message));
            } finally {
                lock.unlock();
            }
        }
    }

    private class Handler implements Runnable {
        private final EndPointQueue queue;
        private final Dispatch<Object> dispatch;
        private final RejectedMessageListener listener;
        private final HubStateListener stateListener;

        public Handler(EndPointQueue queue, Dispatch<Object> dispatch, RejectedMessageListener listener, HubStateListener stateListener) {
            this.queue = queue;
            this.dispatch = dispatch;
            this.listener = listener;
            this.stateListener = stateListener;
        }

        public void run() {
            try {
                List<InterHubMessage> messages = new ArrayList<InterHubMessage>();
                while (true) {
                    lock.lock();
                    try {
                        queue.take(messages);
                    } finally {
                        lock.unlock();
                    }
                    for (InterHubMessage message : messages) {
                        if (message instanceof EndOfStream) {
                            return;
                        }
                        if (message instanceof ChannelMessage) {
                            ChannelMessage channelMessage = (ChannelMessage) message;
                            dispatch.dispatch(channelMessage.getPayload());
                        } else if (message instanceof RejectedMessage) {
                            RejectedMessage rejectedMessage = (RejectedMessage) message;
                            listener.messageDiscarded(rejectedMessage.getPayload());
                        } else if (message instanceof ConnectionEstablished) {
                            stateListener.onConnect();
                        } else {
                            throw new IllegalArgumentException(String.format("Don't know how to handle message %s", message));
                        }
                    }
                    messages.clear();
                }
            } catch (Throwable t) {
                errorHandler.execute(t);
            }
        }
    }
}
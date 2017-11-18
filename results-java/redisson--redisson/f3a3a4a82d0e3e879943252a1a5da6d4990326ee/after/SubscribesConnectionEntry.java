/**
 * Copyright 2014 Nikita Koksharov, Nickolay Borbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.connection;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.redisson.MasterSlaveServersConfig;
import org.redisson.client.ReconnectListener;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisConnection;
import org.redisson.client.RedisPubSubConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;

public class SubscribesConnectionEntry {

    final Logger log = LoggerFactory.getLogger(getClass());

    private final Queue<RedisPubSubConnection> allSubscribeConnections = new ConcurrentLinkedQueue<RedisPubSubConnection>();
    private final Queue<RedisPubSubConnection> freeSubscribeConnections = new ConcurrentLinkedQueue<RedisPubSubConnection>();
    private final AtomicInteger freeSubscribeConnectionsCounter = new AtomicInteger();

    public enum FreezeReason {MANAGER, RECONNECT, SYSTEM}

    private volatile boolean freezed;
    private FreezeReason freezeReason;
    final RedisClient client;

    public enum NodeType {SLAVE, MASTER}

    private final NodeType nodeType;
    private final ConnectionListener connectListener;

    private final Queue<RedisConnection> freeConnections = new ConcurrentLinkedQueue<RedisConnection>();
    private final AtomicInteger freeConnectionsCounter = new AtomicInteger();

    private final AtomicInteger failedAttempts = new AtomicInteger();

    public SubscribesConnectionEntry(RedisClient client, int poolSize, int subscribePoolSize, ConnectionListener connectListener, NodeType serverMode) {
        this.client = client;
        this.freeConnectionsCounter.set(poolSize);
        this.connectListener = connectListener;
        this.nodeType = serverMode;
        freeSubscribeConnectionsCounter.set(subscribePoolSize);
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void resetFailedAttempts() {
        failedAttempts.set(0);
    }

    public int getFailedAttempts() {
        return failedAttempts.get();
    }

    public int incFailedAttempts() {
        return failedAttempts.incrementAndGet();
    }

    public RedisClient getClient() {
        return client;
    }

    public boolean isFreezed() {
        return freezed;
    }

    public void setFreezeReason(FreezeReason freezeReason) {
        this.freezeReason = freezeReason;
    }

    public FreezeReason getFreezeReason() {
        return freezeReason;
    }

    public void setFreezed(boolean freezed) {
        this.freezed = freezed;
    }

    public int getFreeAmount() {
        return freeConnectionsCounter.get();
    }

    private boolean tryAcquire(AtomicInteger counter) {
        while (true) {
            int value = counter.get();
            if (value == 0) {
                return false;
            }
            if (counter.compareAndSet(value, value - 1)) {
                return true;
            }
        }
    }

    public boolean tryAcquireConnection() {
        return tryAcquire(freeConnectionsCounter);
    }

    public void releaseConnection() {
        freeConnectionsCounter.incrementAndGet();
    }

    public RedisConnection pollConnection() {
        return freeConnections.poll();
    }

    public void releaseConnection(RedisConnection connection) {
        freeConnections.add(connection);
    }

    public Future<RedisConnection> connect(final MasterSlaveServersConfig config) {
        final Promise<RedisConnection> connectionFuture = client.getBootstrap().group().next().newPromise();
        Future<RedisConnection> future = client.connectAsync();
        future.addListener(new FutureListener<RedisConnection>() {
            @Override
            public void operationComplete(Future<RedisConnection> future) throws Exception {
                if (!future.isSuccess()) {
                    connectionFuture.tryFailure(future.cause());
                    return;
                }
                RedisConnection conn = future.getNow();
                log.debug("new connection created: {}", conn);

                FutureConnectionListener<RedisConnection> listener = new FutureConnectionListener<RedisConnection>(connectionFuture, conn);
                connectListener.onConnect(config, nodeType, listener);
                listener.executeCommands();

                addReconnectListener(config, conn);
            }

        });
        return connectionFuture;
    }

    private void addReconnectListener(final MasterSlaveServersConfig config, RedisConnection conn) {
        conn.setReconnectListener(new ReconnectListener() {
            @Override
            public void onReconnect(RedisConnection conn, Promise<RedisConnection> connectionFuture) {
                FutureConnectionListener<RedisConnection> listener = new FutureConnectionListener<RedisConnection>(connectionFuture, conn);
                connectListener.onConnect(config, nodeType, listener);
                listener.executeCommands();
            }
        });
    }

    public Future<RedisPubSubConnection> connectPubSub(final MasterSlaveServersConfig config) {
        final Promise<RedisPubSubConnection> connectionFuture = client.getBootstrap().group().next().newPromise();
        Future<RedisPubSubConnection> future = client.connectPubSubAsync();
        future.addListener(new FutureListener<RedisPubSubConnection>() {
            @Override
            public void operationComplete(Future<RedisPubSubConnection> future) throws Exception {
                if (!future.isSuccess()) {
                    connectionFuture.tryFailure(future.cause());
                    return;
                }
                RedisPubSubConnection conn = future.getNow();
                log.debug("new pubsub connection created: {}", conn);

                FutureConnectionListener<RedisPubSubConnection> listener = new FutureConnectionListener<RedisPubSubConnection>(connectionFuture, conn);
                connectListener.onConnect(config, nodeType, listener);
                listener.executeCommands();

                addReconnectListener(config, conn);

                allSubscribeConnections.add(conn);
            }
        });
        return connectionFuture;
    }

    public Queue<RedisPubSubConnection> getAllSubscribeConnections() {
        return allSubscribeConnections;
    }

    public RedisPubSubConnection pollSubscribeConnection() {
        return freeSubscribeConnections.poll();
    }

    public void releaseSubscribeConnection(RedisPubSubConnection connection) {
        freeSubscribeConnections.add(connection);
    }

    public boolean tryAcquireSubscribeConnection() {
        return tryAcquire(freeSubscribeConnectionsCounter);
    }

    public void releaseSubscribeConnection() {
        freeSubscribeConnectionsCounter.incrementAndGet();
    }

    public boolean freezeMaster(FreezeReason reason) {
        synchronized (this) {
            setFreezed(true);
            // only RECONNECT freeze reason could be replaced
            if (getFreezeReason() == null
                    || getFreezeReason() == FreezeReason.RECONNECT) {
                setFreezeReason(reason);
                return true;
            }
        }
        return false;
    }

}

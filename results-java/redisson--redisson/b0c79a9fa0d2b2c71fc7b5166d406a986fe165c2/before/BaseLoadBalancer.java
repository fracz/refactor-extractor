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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.redisson.MasterSlaveServersConfig;
import org.redisson.client.RedisConnection;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisPubSubConnection;
import org.redisson.connection.ConnectionEntry.FreezeReason;
import org.redisson.misc.ConnectionPool;
import org.redisson.misc.PubSubConnectionPoll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.concurrent.Future;
import io.netty.util.internal.PlatformDependent;

abstract class BaseLoadBalancer implements LoadBalancer {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ConnectionManager connectionManager;
    final Map<InetSocketAddress, SubscribesConnectionEntry> addr2Entry = PlatformDependent.newConcurrentHashMap();

    PubSubConnectionPoll pubSubEntries;

    ConnectionPool<RedisConnection> entries;

    public void init(MasterSlaveServersConfig config, ConnectionManager connectionManager, MasterSlaveEntry entry) {
        this.connectionManager = connectionManager;
        entries = new ConnectionPool<RedisConnection>(config, this, connectionManager, entry);
        pubSubEntries = new PubSubConnectionPoll(config, this, connectionManager, entry);
    }

    public synchronized void add(SubscribesConnectionEntry entry) {
        addr2Entry.put(entry.getClient().getAddr(), entry);
        entries.add(entry);
        pubSubEntries.add(entry);
    }

    public int getAvailableClients() {
        int count = 0;
        for (SubscribesConnectionEntry connectionEntry : addr2Entry.values()) {
            if (!connectionEntry.isFreezed()) {
                count++;
            }
        }
        return count;
    }

    public boolean unfreeze(String host, int port, FreezeReason freezeReason) {
        InetSocketAddress addr = new InetSocketAddress(host, port);
        SubscribesConnectionEntry entry = addr2Entry.get(addr);
        if (entry == null) {
            throw new IllegalStateException("Can't find " + addr + " in slaves!");
        }

        synchronized (entry) {
            if (!entry.isFreezed()) {
                return false;
            }
            if ((freezeReason == FreezeReason.RECONNECT
                    && entry.getFreezeReason() == FreezeReason.RECONNECT)
                        || freezeReason != FreezeReason.RECONNECT) {
                entry.setFreezed(false);
                entry.setFreezeReason(null);
                return true;
            }
        }
        return false;
    }

    public Collection<RedisPubSubConnection> freeze(String host, int port, FreezeReason freezeReason) {
        InetSocketAddress addr = new InetSocketAddress(host, port);
        SubscribesConnectionEntry connectionEntry = addr2Entry.get(addr);
        if (connectionEntry == null) {
            return Collections.emptyList();
        }

        synchronized (connectionEntry) {
            log.debug("{} freezed", addr);
            connectionEntry.setFreezed(true);
            // only RECONNECT freeze reason could be replaced
            if (connectionEntry.getFreezeReason() == null
                    || connectionEntry.getFreezeReason() == FreezeReason.RECONNECT) {
                connectionEntry.setFreezeReason(freezeReason);
            }
        }

        // close all connections
        while (true) {
            RedisConnection connection = connectionEntry.pollConnection();
            if (connection == null) {
                break;
            }
            connection.closeAsync();
        }

        // close all pub/sub connections
        while (true) {
            RedisPubSubConnection connection = connectionEntry.pollSubscribeConnection();
            if (connection == null) {
                break;
            }
            connection.closeAsync();
        }

        List<RedisPubSubConnection> list = new ArrayList<RedisPubSubConnection>(connectionEntry.getAllSubscribeConnections());
        connectionEntry.getAllSubscribeConnections().clear();
        return list;
    }

    public Future<RedisPubSubConnection> nextPubSubConnection() {
        return pubSubEntries.get();
    }

    public Future<RedisConnection> getConnection(InetSocketAddress addr) {
        SubscribesConnectionEntry entry = addr2Entry.get(addr);
        if (entry != null) {
            return entries.get(entry);
        }
        RedisConnectionException exception = new RedisConnectionException("Can't find entry for " + addr);
        return connectionManager.getGroup().next().newFailedFuture(exception);
    }

    public Future<RedisConnection> nextConnection() {
        return entries.get();
    }

    public void returnSubscribeConnection(RedisPubSubConnection connection) {
        SubscribesConnectionEntry entry = addr2Entry.get(connection.getRedisClient().getAddr());
        pubSubEntries.returnConnection(entry, connection);
    }

    public void returnConnection(RedisConnection connection) {
        SubscribesConnectionEntry entry = addr2Entry.get(connection.getRedisClient().getAddr());
        entries.returnConnection(entry, connection);
    }

    public void shutdown() {
        for (SubscribesConnectionEntry entry : addr2Entry.values()) {
            entry.getClient().shutdown();
        }
    }

    public void shutdownAsync() {
        for (SubscribesConnectionEntry entry : addr2Entry.values()) {
            connectionManager.shutdownAsync(entry.getClient());
        }
    }

}
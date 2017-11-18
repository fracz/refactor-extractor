/*
 * Copyright 2010 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.ning.http.client.providers.netty;

import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ConnectionsPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.netty.channel.Channel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple implementation of {@link com.ning.http.client.ConnectionsPool} based on a {@link ConcurrentHashMap}
 */
public class NettyConnectionsPool implements ConnectionsPool<String, Channel> {

    private final static Logger log = LoggerFactory.getLogger(NettyAsyncHttpProvider.class);
    private final ConcurrentHashMap<String, List<Channel>> connectionsPool =
            new ConcurrentHashMap<String, List<Channel>>();
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AsyncHttpClientConfig config;


    public NettyConnectionsPool(AsyncHttpClientConfig config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    public boolean offer(String uri, Channel connection) {
        log.debug("Adding uri: {} for channel {}", uri, connection);
        connection.getPipeline().getContext(NettyAsyncHttpProvider.class).setAttachment(new NettyAsyncHttpProvider.DiscardEvent());

        List<Channel> pooledConnectionForHost = connectionsPool.get(uri);
        if (pooledConnectionForHost == null) {
            List<Channel> newPool = new LinkedList<Channel>();
            connectionsPool.putIfAbsent(uri, newPool);
            pooledConnectionForHost = connectionsPool.get(uri);
        }

        synchronized (pooledConnectionForHost) {
            int size = pooledConnectionForHost.size();
            if (config.getMaxConnectionPerHost() == -1 || size < config.getMaxConnectionPerHost()) {
                boolean added = pooledConnectionForHost.add(connection);
                if (added) {
                    totalConnections.incrementAndGet();
                }
                return added;
            } else {
                log.warn("Maximum connections per hosts reached {}", config.getMaxConnectionPerHost());
                return false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Channel poll(String uri) {
        Channel channel = null;
        List<Channel> pooledConnectionForHost = connectionsPool.get(uri);
        if (pooledConnectionForHost != null) {
            boolean poolEmpty = false;
            while (!poolEmpty && channel == null) {
                synchronized (pooledConnectionForHost) {
                    if (pooledConnectionForHost.size() > 0) {
                        channel = pooledConnectionForHost.remove(0);
                    }
                }
                if (channel == null) {
                    poolEmpty = true;
                } else if (!channel.isConnected() || !channel.isOpen()) {
                    removeAll(channel);
                    channel = null;
                } else {
                    totalConnections.decrementAndGet();
                }
            }
        }
        return channel;
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeAll(Channel connection) {
        boolean isRemoved = false;
        Iterator<Map.Entry<String, List<Channel>>> i = connectionsPool.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, List<Channel>> e = i.next();
            synchronized (e.getValue()) {
                boolean removed = e.getValue().remove(connection);
                if (removed) {
                    log.debug("Removing uri: {} for channel {}", e.getKey(), e.getValue());
                    totalConnections.decrementAndGet();

                }
                isRemoved |= removed;
            }
        }
        return isRemoved;
    }

    /**
     * {@inheritDoc}
     */
    public boolean canCacheConnection() {
        if (config.getMaxTotalConnections() != -1 && totalConnections.get() >= config.getMaxTotalConnections()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() {
        try {
            Iterator<Map.Entry<String, List<Channel>>> i = connectionsPool.entrySet().iterator();
            while (i.hasNext()) {
                List<Channel> list = i.next().getValue();
                synchronized (list) {
                    for (int j = 0; j < list.size(); j++) {
                        Channel channel = list.remove(0);
                        removeAll(channel);
                        channel.close();
                    }
                }
            }
        } finally {
            connectionsPool.clear();
        }
    }
}
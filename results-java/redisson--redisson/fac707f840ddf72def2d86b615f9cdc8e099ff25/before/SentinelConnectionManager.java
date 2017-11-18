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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.redisson.Config;
import org.redisson.MasterSlaveServersConfig;
import org.redisson.SentinelServersConfig;
import org.redisson.client.BaseRedisPubSubListener;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisConnection;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisPubSubConnection;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.pubsub.PubSubType;
import org.redisson.connection.ClientConnectionsEntry.FreezeReason;
import org.redisson.misc.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.PlatformDependent;

public class SentinelConnectionManager extends MasterSlaveConnectionManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ConcurrentMap<String, RedisClient> sentinels = PlatformDependent.newConcurrentHashMap();
    private final AtomicReference<String> currentMaster = new AtomicReference<String>();
    private final ConcurrentMap<String, Boolean> slaves = PlatformDependent.newConcurrentHashMap();


    public SentinelConnectionManager(SentinelServersConfig cfg, Config config) {
        super(config);

        final MasterSlaveServersConfig c = new MasterSlaveServersConfig();
        c.setRetryInterval(cfg.getRetryInterval());
        c.setRetryAttempts(cfg.getRetryAttempts());
        c.setTimeout(cfg.getTimeout());
        c.setPingTimeout(cfg.getPingTimeout());
        c.setLoadBalancer(cfg.getLoadBalancer());
        c.setPassword(cfg.getPassword());
        c.setDatabase(cfg.getDatabase());
        c.setClientName(cfg.getClientName());
        c.setMasterConnectionPoolSize(cfg.getMasterConnectionPoolSize());
        c.setSlaveConnectionPoolSize(cfg.getSlaveConnectionPoolSize());
        c.setSlaveSubscriptionConnectionPoolSize(cfg.getSlaveSubscriptionConnectionPoolSize());
        c.setSubscriptionsPerConnection(cfg.getSubscriptionsPerConnection());
        c.setConnectTimeout(cfg.getConnectTimeout());
        c.setIdleConnectionTimeout(cfg.getIdleConnectionTimeout());

        c.setFailedAttempts(cfg.getFailedAttempts());
        c.setReconnectionTimeout(cfg.getReconnectionTimeout());
        c.setMasterConnectionMinimumIdleSize(cfg.getMasterConnectionMinimumIdleSize());
        c.setSlaveConnectionMinimumIdleSize(cfg.getSlaveConnectionMinimumIdleSize());
        c.setSlaveSubscriptionConnectionMinimumIdleSize(cfg.getSlaveSubscriptionConnectionMinimumIdleSize());

        List<String> disconnectedSlaves = new ArrayList<String>();
        for (URI addr : cfg.getSentinelAddresses()) {
            RedisClient client = createClient(addr.getHost(), addr.getPort(), c.getConnectTimeout());
            try {
                RedisConnection connection = client.connect();
                if (!connection.isActive()) {
                    continue;
                }
                Promise<RedisConnection> f = newPromise();
                connectListener.onConnect(f, connection, null, c);
                f.syncUninterruptibly();

                // TODO async
                List<String> master = connection.sync(RedisCommands.SENTINEL_GET_MASTER_ADDR_BY_NAME, cfg.getMasterName());
                String masterHost = master.get(0) + ":" + master.get(1);
                c.setMasterAddress(masterHost);
                currentMaster.set(masterHost);
                log.info("master: {} added", masterHost);
                slaves.put(masterHost, true);

                List<Map<String, String>> sentinelSlaves = connection.sync(StringCodec.INSTANCE, RedisCommands.SENTINEL_SLAVES, cfg.getMasterName());
                for (Map<String, String> map : sentinelSlaves) {
                    if (map.isEmpty()) {
                        continue;
                    }

                    String ip = map.get("ip");
                    String port = map.get("port");
                    String flags = map.get("flags");

                    String host = ip + ":" + port;

                    c.addSlaveAddress(host);
                    slaves.put(host, true);
                    log.info("slave: {} added, params: {}", host, map);

                    if (flags.contains("s_down") || flags.contains("disconnected")) {
                        disconnectedSlaves.add(host);
                    }
                }
                break;
            } catch (RedisConnectionException e) {
                log.warn("Can't connect to sentinel server. {}", e.getMessage());
            } finally {
                client.shutdownAsync();
            }
        }

        if (currentMaster.get() == null) {
            throw new RedisConnectionException("Can't connect to servers!");
        }
        init(c);

        for (String host : disconnectedSlaves) {
            String[] parts = host.split(":");
            slaveDown(parts[0], parts[1]);
        }

        List<Future<RedisPubSubConnection>> connectionFutures = new ArrayList<Future<RedisPubSubConnection>>(cfg.getSentinelAddresses().size());
        for (URI addr : cfg.getSentinelAddresses()) {
            Future<RedisPubSubConnection> future = registerSentinel(cfg, addr, c);
            connectionFutures.add(future);
        }

        for (Future<RedisPubSubConnection> future : connectionFutures) {
            future.syncUninterruptibly();
        }
    }

    private Future<RedisPubSubConnection> registerSentinel(final SentinelServersConfig cfg, final URI addr, final MasterSlaveServersConfig c) {
        RedisClient client = createClient(addr.getHost(), addr.getPort(), c.getConnectTimeout());
        RedisClient oldClient = sentinels.putIfAbsent(addr.getHost() + ":" + addr.getPort(), client);
        if (oldClient != null) {
            return newSucceededFuture(null);
        }

        Future<RedisPubSubConnection> pubsubFuture = client.connectPubSubAsync();
        pubsubFuture.addListener(new FutureListener<RedisPubSubConnection>() {
            @Override
            public void operationComplete(Future<RedisPubSubConnection> future) throws Exception {
                if (!future.isSuccess()) {
                    log.warn("Can't connect to sentinel: {}:{}", addr.getHost(), addr.getPort());
                    return;
                }

                RedisPubSubConnection pubsub = future.getNow();
                pubsub.addListener(new BaseRedisPubSubListener<String>() {

                    @Override
                    public void onMessage(String channel, String msg) {
                        if ("+sentinel".equals(channel)) {
                            onSentinelAdded(cfg, msg, c);
                        }
                        if ("+slave".equals(channel)) {
                            onSlaveAdded(addr, msg);
                        }
                        if ("+sdown".equals(channel)) {
                            onNodeDown(addr, msg);
                        }
                        if ("-sdown".equals(channel)) {
                            onNodeUp(addr, msg);
                        }
                        if ("+switch-master".equals(channel)) {
                            onMasterChange(cfg, addr, msg);
                        }
                    }

                    @Override
                    public boolean onStatus(PubSubType type, String channel) {
                        if (type == PubSubType.SUBSCRIBE) {
                            log.debug("subscribed to channel: {} from Sentinel {}:{}", channel, addr.getHost(), addr.getPort());
                        }
                        return true;
                    }
                });

                pubsub.subscribe(StringCodec.INSTANCE, "+switch-master", "+sdown", "-sdown", "+slave", "+sentinel");
                log.info("sentinel: {}:{} added", addr.getHost(), addr.getPort());
            }
        });

        return pubsubFuture;
    }

    protected void onSentinelAdded(SentinelServersConfig cfg, String msg, MasterSlaveServersConfig c) {
        String[] parts = msg.split(" ");
        if ("sentinel".equals(parts[0])) {
            String ip = parts[2];
            String port = parts[3];

            String addr = ip + ":" + port;
            URI uri = URIBuilder.create(addr);
            registerSentinel(cfg, uri, c);
        }
    }

    protected void onSlaveAdded(URI addr, String msg) {
        String[] parts = msg.split(" ");

        if (parts.length > 4
                 && "slave".equals(parts[0])) {
            String ip = parts[2];
            String port = parts[3];

            String slaveAddr = ip + ":" + port;

            // to avoid addition twice
            if (slaves.putIfAbsent(slaveAddr, true) == null) {
                getEntry(singleSlotRange).addSlave(ip, Integer.valueOf(port));
                log.info("slave: {} added", slaveAddr);
            } else {
                slaveUp(ip, port);
            }
        } else {
            log.warn("onSlaveAdded. Invalid message: {} from Sentinel {}:{}", msg, addr.getHost(), addr.getPort());
        }
    }

    private void onNodeDown(URI sentinelAddr, String msg) {
        String[] parts = msg.split(" ");

        if (parts.length > 3) {
            if ("slave".equals(parts[0])) {
                String ip = parts[2];
                String port = parts[3];

                slaveDown(ip, port);
            } else if ("sentinel".equals(parts[0])) {
                String ip = parts[2];
                String port = parts[3];

                String addr = ip + ":" + port;
                RedisClient sentinel = sentinels.remove(addr);
                if (sentinel != null) {
                    sentinel.shutdownAsync();
                    log.info("sentinel: {} has down", addr);
                }
            } else if ("master".equals(parts[0])) {
                String ip = parts[2];
                String port = parts[3];

                MasterSlaveEntry entry = getEntry(singleSlotRange);
                if (entry.getFreezeReason() != FreezeReason.MANAGER) {
                    entry.freeze();
                    String addr = ip + ":" + port;
                    log.info("master: {} has down", addr);
                }
            }
        } else {
            log.warn("onSlaveDown. Invalid message: {} from Sentinel {}:{}", msg, sentinelAddr.getHost(), sentinelAddr.getPort());
        }
    }

    private void slaveDown(String ip, String port) {
        slaveDown(singleSlotRange, ip, Integer.valueOf(port), FreezeReason.MANAGER);
        log.info("slave: {}:{} has down", ip, port);
    }

    private void onNodeUp(URI addr, String msg) {
        String[] parts = msg.split(" ");

        if (parts.length > 3) {
            if ("slave".equals(parts[0])) {
                String ip = parts[2];
                String port = parts[3];

                slaveUp(ip, port);
            } else if ("master".equals(parts[0])) {
                String ip = parts[2];
                String port = parts[3];

                String masterAddr = ip + ":" + port;
                MasterSlaveEntry entry = getEntry(singleSlotRange);
                if (entry.isFreezed()
                        && entry.getClient().getAddr().equals(new InetSocketAddress(ip, Integer.valueOf(port)))) {
                    entry.unfreeze();
                    log.info("master: {} has up", masterAddr);
                }
            } else {
                log.warn("onSlaveUp. Invalid message: {} from Sentinel {}:{}", msg, addr.getHost(), addr.getPort());
            }
        }
    }

    private void slaveUp(String ip, String port) {
        if (getEntry(singleSlotRange).slaveUp(ip, Integer.valueOf(port), FreezeReason.MANAGER)) {
            String slaveAddr = ip + ":" + port;
            log.info("slave: {} has up", slaveAddr);
        }
    }

    private void onMasterChange(SentinelServersConfig cfg, URI addr, String msg) {
        String[] parts = msg.split(" ");

        if (parts.length > 3) {
            if (cfg.getMasterName().equals(parts[0])) {
                String ip = parts[3];
                String port = parts[4];

                String current = currentMaster.get();
                String newMaster = ip + ":" + port;
                if (!newMaster.equals(current)
                        && currentMaster.compareAndSet(current, newMaster)) {
                    changeMaster(singleSlotRange, ip, Integer.valueOf(port));
                    log.info("master has changed from {} to {}", current, newMaster);
                }
            }
        } else {
            log.warn("Invalid message: {} from Sentinel {}:{}", msg, addr.getHost(), addr.getPort());
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();

        for (RedisClient sentinel : sentinels.values()) {
            sentinel.shutdown();
        }
    }
}

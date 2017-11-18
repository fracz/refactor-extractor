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
package org.redisson;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.redisson.core.RAtomicLong;
import org.redisson.core.RCountDownLatch;
import org.redisson.core.RList;
import org.redisson.core.RLock;
import org.redisson.core.RMap;
import org.redisson.core.RObject;
import org.redisson.core.RQueue;
import org.redisson.core.RSet;
import org.redisson.core.RTopic;

import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.codec.JsonCodec;
import com.lambdaworks.redis.pubsub.RedisPubSubConnection;

// TODO lazy connection
public class Redisson {

    // TODO drain by weak reference
    private final ConcurrentMap<String, RedissonCountDownLatch> latchesMap = new ConcurrentHashMap<String, RedissonCountDownLatch>();
    private final ConcurrentMap<String, RedissonAtomicLong> atomicLongsMap = new ConcurrentHashMap<String, RedissonAtomicLong>();
    private final ConcurrentMap<String, RedissonQueue> queuesMap = new ConcurrentHashMap<String, RedissonQueue>();
    private final ConcurrentMap<String, RedissonTopic> topicsMap = new ConcurrentHashMap<String, RedissonTopic>();
    private final ConcurrentMap<String, RedissonSet> setsMap = new ConcurrentHashMap<String, RedissonSet>();
    private final ConcurrentMap<String, RedissonList> listsMap = new ConcurrentHashMap<String, RedissonList>();
    private final ConcurrentMap<String, RedissonMap> mapsMap = new ConcurrentHashMap<String, RedissonMap>();
    private final ConcurrentMap<String, RedissonLock> locksMap = new ConcurrentHashMap<String, RedissonLock>();

    private JsonCodec codec = new JsonCodec();

    RedisClient redisClient;

    Redisson(String host, int port) {
        redisClient = new RedisClient(host, port);
    }

    public static Redisson create() {
        return create("localhost");
    }

    public static Redisson create(String host) {
        return create(host, 6379);
    }

    public static Redisson create(String host, int port) {
        return new Redisson(host, port);
    }

    public <V> RList<V> getList(String name) {
        RedissonList<V> list = listsMap.get(name);
        if (list == null) {
            RedisConnection<Object, Object> connection = connect();
            list = new RedissonList<V>(this, connection, name);
            RedissonList<V> oldList = listsMap.putIfAbsent(name, list);
            if (oldList != null) {
                connection.close();

                list = oldList;
            }
        }

        return list;
    }

    public <K, V> RMap<K, V> getMap(String name) {
        RedissonMap<K, V> map = mapsMap.get(name);
        if (map == null) {
            RedisConnection<Object, Object> connection = connect();
            map = new RedissonMap<K, V>(this, connection, name);
            RedissonMap<K, V> oldMap = mapsMap.putIfAbsent(name, map);
            if (oldMap != null) {
                connection.close();

                map = oldMap;
            }
        }

        return map;
    }

    public RLock getLock(String name) {
        RedissonLock lock = locksMap.get(name);
        if (lock == null) {
            RedisConnection<Object, Object> connection = connect();
            RedisPubSubConnection<Object, Object> pubSubConnection = connectPubSub();

            lock = new RedissonLock(this, pubSubConnection, connection, name);
            RedissonLock oldLock = locksMap.putIfAbsent(name, lock);
            if (oldLock != null) {
                connection.close();
                pubSubConnection.close();

                lock = oldLock;
            }
        }

        lock.subscribe();
        return lock;
    }

    public <V> RSet<V> getSet(String name) {
        RedissonSet<V> set = setsMap.get(name);
        if (set == null) {
            RedisConnection<Object, Object> connection = connect();
            set = new RedissonSet<V>(this, connection, name);
            RedissonSet<V> oldSet = setsMap.putIfAbsent(name, set);
            if (oldSet != null) {
                connection.close();

                set = oldSet;
            }
        }

        return set;
    }

    public <M> RTopic<M> getTopic(String name) {
        RedissonTopic<M> topic = topicsMap.get(name);
        if (topic == null) {
            RedisConnection<Object, Object> connection = connect();
            RedisPubSubConnection<String, M> pubSubConnection = connectPubSub();

            topic = new RedissonTopic<M>(this, pubSubConnection, connection, name);
            RedissonTopic<M> oldTopic = topicsMap.putIfAbsent(name, topic);
            if (oldTopic != null) {
                connection.close();
                pubSubConnection.close();

                topic = oldTopic;
            }
        }

        topic.subscribe();
        return topic;

    }

    public <V> RQueue<V> getQueue(String name) {
        RedissonQueue<V> queue = queuesMap.get(name);
        if (queue == null) {
            RedisConnection<Object, Object> connection = connect();
            queue = new RedissonQueue<V>(this, connection, name);
            RedissonQueue<V> oldQueue = queuesMap.putIfAbsent(name, queue);
            if (oldQueue != null) {
                connection.close();

                queue = oldQueue;
            }
        }

        return queue;
    }

    public RAtomicLong getAtomicLong(String name) {
        RedissonAtomicLong atomicLong = atomicLongsMap.get(name);
        if (atomicLong == null) {
            RedisConnection<Object, Object> connection = connect();
            atomicLong = new RedissonAtomicLong(this, connection, name);
            RedissonAtomicLong oldAtomicLong = atomicLongsMap.putIfAbsent(name, atomicLong);
            if (oldAtomicLong != null) {
                connection.close();

                atomicLong = oldAtomicLong;
            }
        }

        return atomicLong;

    }

    public RCountDownLatch getCountDownLatch(String name) {
        RedissonCountDownLatch latch = latchesMap.get(name);
        if (latch == null) {
            RedisConnection<Object, Object> connection = connect();
            RedisPubSubConnection<Object, Object> pubSubConnection = connectPubSub();

            latch = new RedissonCountDownLatch(this, pubSubConnection, connection, name);
            RedissonCountDownLatch oldLatch = latchesMap.putIfAbsent(name, latch);
            if (oldLatch != null) {
                connection.close();
                pubSubConnection.close();

                latch = oldLatch;
            }
        }

        latch.subscribe();
        return latch;
    }

    private <K, V> RedisPubSubConnection<K, V> connectPubSub() {
        return (RedisPubSubConnection<K, V>) redisClient.connectPubSub(codec);
    }

    // TODO implement
//    public void getSemaphore() {
//    }

    void remove(RObject robject) {
        if (robject instanceof RedissonLock) {
            locksMap.remove(robject.getName());
        }
        if (robject instanceof RedissonSet) {
            setsMap.remove(robject.getName());
        }
        if (robject instanceof RedissonTopic) {
            topicsMap.remove(robject.getName());
        }
        if (robject instanceof RedissonAtomicLong) {
            atomicLongsMap.remove(robject.getName());
        }
        if (robject instanceof RedissonCountDownLatch) {
            latchesMap.remove(robject.getName());
        }
        if (robject instanceof RedissonList) {
            listsMap.remove(robject.getName());
        }
        if (robject instanceof RedissonMap) {
            mapsMap.remove(robject.getName());
        }
        if (robject instanceof RedissonQueue) {
            queuesMap.remove(robject.getName());
        }
    }

    public void shutdown() {
        redisClient.shutdown();
    }

    RedisConnection<Object, Object> connect() {
        return redisClient.connect(codec);
    }

    RedisAsyncConnection<Object, Object> connectAsync() {
        return redisClient.connectAsync(codec);
    }

}

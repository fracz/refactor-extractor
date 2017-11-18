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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.redisson.core.RCountDownLatch;
import org.redisson.misc.internal.ReclosableLatch;

import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.pubsub.RedisPubSubAdapter;
import com.lambdaworks.redis.pubsub.RedisPubSubConnection;

public class RedissonCountDownLatch implements RCountDownLatch {

    private final CountDownLatch subscribeLatch = new CountDownLatch(1);
    private final RedisPubSubConnection<Object, Object> pubSubConnection;
    private final RedisConnection<Object, Object> connection;
    private final Redisson redisson;

    private final String groupName = "redisson_countdownlatch_";
    private final String name;

    private static final Integer unlockMessage = 0;

    private final AtomicBoolean subscribeOnce = new AtomicBoolean();

    private final ReclosableLatch msg = new ReclosableLatch();

    RedissonCountDownLatch(Redisson redisson, RedisPubSubConnection<Object, Object> pubSubConnection, RedisConnection<Object, Object> connection, String name) {
        this.connection = connection;
        this.name = name;
        this.pubSubConnection = pubSubConnection;
        this.redisson = redisson;

    }

    public void subscribe() {
        if (subscribeOnce.compareAndSet(false, true)) {
            RedisPubSubAdapter<Object, Object> listener = new RedisPubSubAdapter<Object, Object>() {

                @Override
                public void subscribed(Object channel, long count) {
                    subscribeLatch.countDown();
                }

                @Override
                public void message(Object channel, Object message) {
                    if (message.equals(unlockMessage)) {
                        msg.open();
                    }
                }

            };
            pubSubConnection.addListener(listener);
            pubSubConnection.subscribe(getChannelName());
        }

        try {
            subscribeLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void await() throws InterruptedException {
        while (getCount() > 0) {
            // waiting for message
            msg.await();
        }
    }


    @Override
    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        time = unit.toMillis(time);
        while (getCount() > 0) {
            if (time <= 0) {
                return false;
            }
            long current = System.currentTimeMillis();
            // waiting for message
            msg.await(time, TimeUnit.MILLISECONDS);
            long elapsed = System.currentTimeMillis() - current;
            time = time - elapsed;
        }

        return true;
    }

    @Override
    public void countDown() {
        if (getCount() <= 0) {
            return;
        }

        Long val = connection.decr(name);
        if (val == 0) {
            connection.publish(getChannelName(), unlockMessage);
            connection.del(name);
        } else if (val < 0) {
            connection.del(name);
        }
    }

    private String getChannelName() {
        return groupName + name;
    }

    @Override
    public long getCount() {
        Number val = (Number) connection.get(name);
        if (val == null) {
            return 0;
        }
        return val.longValue();
    }

    @Override
    public boolean trySetCount(long count) {
        Boolean res = connection.setnx(name, count);
        if (res) {
            msg.close();
        }
        return res;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void destroy() {
        connection.close();
        pubSubConnection.close();

        redisson.remove(this);
    }

}
/**
 * Copyright 2016 Nikita Koksharov
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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.MapLoader;
import org.redisson.api.map.MapWriter;
import org.redisson.api.mapreduce.RMapReduce;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.MapScanCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommand.ValueType;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.convertor.BooleanReplayConvertor;
import org.redisson.client.protocol.convertor.NumberConvertor;
import org.redisson.client.protocol.decoder.MapScanResult;
import org.redisson.client.protocol.decoder.ScanObjectEntry;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.decoder.MapGetAllDecoder;
import org.redisson.mapreduce.RedissonMapReduce;
import org.redisson.misc.Hash;
import org.redisson.misc.RPromise;
import org.redisson.misc.RedissonPromise;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

/**
 * Distributed and concurrent implementation of {@link java.util.concurrent.ConcurrentMap}
 * and {@link java.util.Map}
 *
 * @author Nikita Koksharov
 *
 * @param <K> key
 * @param <V> value
 */
public class RedissonMap<K, V> extends RedissonExpirable implements RMap<K, V> {

    final RedissonClient redisson;
    MapLoader<K, V> mapLoader;
    MapWriter<K, V> mapWriter;

    protected RedissonMap(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson, MapLoader<K, V> mapLoader, MapWriter<K, V> mapWriter) {
        super(commandExecutor, name);
        this.redisson = redisson;
        this.mapLoader = mapLoader;
        this.mapWriter = mapWriter;
    }

    public RedissonMap(Codec codec, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson, MapLoader<K, V> mapLoader, MapWriter<K, V> mapWriter) {
        super(codec, commandExecutor, name);
        this.redisson = redisson;
        this.mapLoader = mapLoader;
        this.mapWriter = mapWriter;
    }

    @Override
    public <KOut, VOut> RMapReduce<K, V, KOut, VOut> mapReduce() {
        return new RedissonMapReduce<K, V, KOut, VOut>(this, redisson, commandExecutor.getConnectionManager());
    }

    @Override
    public RLock getLock(K key) {
        String lockName = getLockName(key);
        return redisson.getLock(lockName);
    }

    @Override
    public RReadWriteLock getReadWriteLock(K key) {
        String lockName = getLockName(key);
        return redisson.getReadWriteLock(lockName);
    }

    private String getLockName(Object key) {
        try {
            byte[] keyState = codec.getMapKeyEncoder().encode(key);
            return suffixName(getName(), Hash.hashToBase64(keyState) + ":key");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int size() {
        return get(sizeAsync());
    }

    @Override
    public RFuture<Integer> sizeAsync() {
        return commandExecutor.readAsync(getName(), codec, RedisCommands.HLEN, getName());
    }

    @Override
    public int valueSize(K key) {
        return get(valueSizeAsync(key));
    }

    @Override
    public RFuture<Integer> valueSizeAsync(K key) {
        checkKey(key);

        return commandExecutor.readAsync(getName(), codec, RedisCommands.HSTRLEN, getName(key), key);
    }

    protected void checkKey(Object key) {
        if (key == null) {
            throw new NullPointerException("map key can't be null");
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(containsKeyAsync(key));
    }

    @Override
    public RFuture<Boolean> containsKeyAsync(Object key) {
        checkKey(key);

        return commandExecutor.readAsync(getName(key), codec, RedisCommands.HEXISTS, getName(key), key);
    }

    @Override
    public boolean containsValue(Object value) {
        return get(containsValueAsync(value));
    }

    @Override
    public RFuture<Boolean> containsValueAsync(Object value) {
        checkValue(value);

        return commandExecutor.evalReadAsync(getName(), codec, new RedisCommand<Boolean>("EVAL", new BooleanReplayConvertor(), 4),
                "local s = redis.call('hvals', KEYS[1]);" +
                        "for i = 1, #s, 1 do "
                            + "if ARGV[1] == s[i] then "
                                + "return 1 "
                            + "end "
                       + "end;" +
                     "return 0",
                Collections.<Object>singletonList(getName()), value);
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) {
        return get(getAllAsync(keys));
    }

    @Override
    public RFuture<Map<K, V>> getAllAsync(final Set<K> keys) {
        if (keys.isEmpty()) {
            return newSucceededFuture(Collections.<K, V>emptyMap());
        }

        RFuture<Map<K, V>> future = getAllValuesAsync(keys);
        if (mapLoader == null) {
            return future;
        }

        final RPromise<Map<K, V>> result = new RedissonPromise<Map<K, V>>();
        future.addListener(new FutureListener<Map<K, V>>() {
            @Override
            public void operationComplete(final Future<Map<K, V>> mapFuture) throws Exception {
                if (!mapFuture.isSuccess()) {
                    result.tryFailure(mapFuture.cause());
                    return;
                }

                if (!mapFuture.getNow().keySet().containsAll(keys)) {
                    Set<K> newKeys = new HashSet<K>(keys);
                    newKeys.removeAll(mapFuture.getNow().keySet());

                    loadAllAsync(newKeys, false, 1, mapFuture.getNow()).addListener(new FutureListener<Void>() {
                        @Override
                        public void operationComplete(Future<Void> future) throws Exception {
                            result.trySuccess(mapFuture.getNow());
                        }
                    });
                } else {
                    result.trySuccess(mapFuture.getNow());
                }
            }
        });
        return result;
    }

    protected RFuture<Map<K, V>> getAllValuesAsync(final Set<K> keys) {
        List<Object> args = new ArrayList<Object>(keys.size() + 1);
        args.add(getName());
        args.addAll(keys);
        RFuture<Map<K, V>> future = commandExecutor.readAsync(getName(), codec, new RedisCommand<Map<Object, Object>>("HMGET", new MapGetAllDecoder(args, 1), 2, ValueType.MAP_KEY, ValueType.MAP_VALUE), args.toArray());
        return future;
    }

    @Override
    public V get(Object key) {
        return get(getAsync((K)key));
    }

    @Override
    public V put(K key, V value) {
        return get(putAsync(key, value));
    }

    @Override
    public V remove(Object key) {
        return get(removeAsync((K)key));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        get(putAllAsync(map));
    }

    @Override
    public RFuture<Void> putAllAsync(final Map<? extends K, ? extends V> map) {
        if (map.isEmpty()) {
            return newSucceededFuture(null);
        }

        RFuture<Void> future = putAllOperationAsync(map);
        if (mapWriter == null) {
            return future;
        }

        RPromise<Void> result = new MapWriterExecutorPromise<Void>(future, commandExecutor) {
            @Override
            public void executeWriter() {
                mapWriter.writeAll((Map<K, V>) map);
            }
        };
        return result;
    }

    protected RFuture<Void> putAllOperationAsync(Map<? extends K, ? extends V> map) {
        List<Object> params = new ArrayList<Object>(map.size()*2 + 1);
        params.add(getName());
        for (java.util.Map.Entry<? extends K, ? extends V> t : map.entrySet()) {
            if (t.getKey() == null) {
                throw new NullPointerException("map key can't be null");
            }
            if (t.getValue() == null) {
                throw new NullPointerException("map value can't be null");
            }

            params.add(encodeMapKey(t.getKey()));
            params.add(encodeMapValue(t.getValue()));
        }

        RFuture<Void> future = commandExecutor.writeAsync(getName(), codec, RedisCommands.HMSET, params.toArray());
        return future;
    }

    @Override
    public void clear() {
        delete();
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public Collection<V> values() {
        return new Values();
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public Set<K> readAllKeySet() {
        return get(readAllKeySetAsync());
    }

    @Override
    public RFuture<Set<K>> readAllKeySetAsync() {
        return commandExecutor.readAsync(getName(), codec, RedisCommands.HKEYS, getName());
    }

    @Override
    public Collection<V> readAllValues() {
        return get(readAllValuesAsync());
    }

    @Override
    public RFuture<Collection<V>> readAllValuesAsync() {
        return commandExecutor.readAsync(getName(), codec, RedisCommands.HVALS, getName());
    }

    @Override
    public Set<Entry<K, V>> readAllEntrySet() {
        return get(readAllEntrySetAsync());
    }

    @Override
    public RFuture<Set<Entry<K, V>>> readAllEntrySetAsync() {
        return commandExecutor.readAsync(getName(), codec, RedisCommands.HGETALL_ENTRY, getName());
    }

    @Override
    public Map<K, V> readAllMap() {
        return get(readAllMapAsync());
    }

    @Override
    public RFuture<Map<K, V>> readAllMapAsync() {
        return commandExecutor.readAsync(getName(), codec, RedisCommands.HGETALL, getName());
    }


    @Override
    public V putIfAbsent(K key, V value) {
        return get(putIfAbsentAsync(key, value));
    }

    @Override
    public RFuture<V> putIfAbsentAsync(final K key, final V value) {
        checkKey(key);
        checkValue(key);

        RFuture<V> future = putIfAbsentOperationAsync(key, value);
        if (mapWriter == null) {
            return future;
        }

        RPromise<V> result = new MapWriterExecutorPromise<V>(future, commandExecutor) {
            @Override
            protected void executeWriter() {
                mapWriter.write(key, value);
            }

            @Override
            protected boolean condition(Future<V> future) {
                return future.getNow() == null;
            }
        };
        return result;
    }

    protected RFuture<V> putIfAbsentOperationAsync(K key, V value) {
        return commandExecutor.evalWriteAsync(getName(key), codec, RedisCommands.EVAL_MAP_VALUE,
                 "if redis.call('hsetnx', KEYS[1], ARGV[1], ARGV[2]) == 1 then "
                    + "return nil "
                + "else "
                    + "return redis.call('hget', KEYS[1], ARGV[1]) "
                + "end",
                Collections.<Object>singletonList(getName(key)), encodeMapKey(key), encodeMapValue(value));
    }

    @Override
    public boolean fastPutIfAbsent(K key, V value) {
        return get(fastPutIfAbsentAsync(key, value));
    }

    @Override
    public RFuture<Boolean> fastPutIfAbsentAsync(final K key, final V value) {
        checkKey(key);
        checkValue(value);

        RFuture<Boolean> future = fastPutIfAbsentOperationAsync(key, value);
        if (mapWriter == null) {
            return future;
        }

        RPromise<Boolean> result = new MapWriterExecutorPromise<Boolean>(future, commandExecutor) {
            @Override
            protected void executeWriter() {
                mapWriter.write(key, value);
            }

            @Override
            protected boolean condition(Future<Boolean> future) {
                return future.getNow();
            }
        };

        return result;
    }

    protected RFuture<Boolean> fastPutIfAbsentOperationAsync(K key, V value) {
        return commandExecutor.writeAsync(getName(key), codec, RedisCommands.HSETNX, getName(key), encodeMapKey(key), encodeMapValue(value));
    }

    @Override
    public boolean remove(Object key, Object value) {
        return get(removeAsync(key, value));
    }

    @Override
    public RFuture<Boolean> removeAsync(final Object key, Object value) {
        checkKey(key);
        checkValue(value);

        RFuture<Boolean> future = removeOperationAsync(key, value);
        if (mapWriter == null) {
            return future;
        }

        RPromise<Boolean> result = new MapWriterExecutorPromise<Boolean>(future, commandExecutor) {
            @Override
            protected void executeWriter() {
                mapWriter.delete((K) key);
            }

            @Override
            protected boolean condition(Future<Boolean> future) {
                return future.getNow();
            }
        };

        return result;
    }

    protected RFuture<Boolean> removeOperationAsync(Object key, Object value) {
        RFuture<Boolean> future = commandExecutor.evalWriteAsync(getName(key), codec, RedisCommands.EVAL_BOOLEAN,
                "if redis.call('hget', KEYS[1], ARGV[1]) == ARGV[2] then "
                        + "return redis.call('hdel', KEYS[1], ARGV[1]) "
                + "else "
                    + "return 0 "
                + "end",
            Collections.<Object>singletonList(getName(key)), encodeMapKey(key), encodeMapValue(value));
        return future;
    }

    protected void checkValue(Object value) {
        if (value == null) {
            throw new NullPointerException("map value can't be null");
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return get(replaceAsync(key, oldValue, newValue));
    }

    @Override
    public RFuture<Boolean> replaceAsync(final K key, V oldValue, final V newValue) {
        checkKey(key);
        if (oldValue == null) {
            throw new NullPointerException("map oldValue can't be null");
        }
        if (newValue == null) {
            throw new NullPointerException("map newValue can't be null");
        }

        RFuture<Boolean> future = replaceOperationAsync(key, oldValue, newValue);
        if (mapWriter == null) {
            return future;
        }

        RPromise<Boolean> result = new MapWriterExecutorPromise<Boolean>(future, commandExecutor) {
            @Override
            protected void executeWriter() {
                mapWriter.write(key, newValue);
            }

            @Override
            protected boolean condition(Future<Boolean> future) {
                return future.getNow();
            }
        };

        return result;
    }

    protected RFuture<Boolean> replaceOperationAsync(K key, V oldValue, V newValue) {
        return commandExecutor.evalWriteAsync(getName(key), codec, RedisCommands.EVAL_BOOLEAN,
                "if redis.call('hget', KEYS[1], ARGV[1]) == ARGV[2] then "
                    + "redis.call('hset', KEYS[1], ARGV[1], ARGV[3]); "
                    + "return 1; "
                + "else "
                    + "return 0; "
                + "end",
                Collections.<Object>singletonList(getName(key)), encodeMapKey(key), encodeMapValue(oldValue), encodeMapValue(newValue));
    }

    @Override
    public V replace(K key, V value) {
        return get(replaceAsync(key, value));
    }

    @Override
    public RFuture<V> replaceAsync(final K key, final V value) {
        checkKey(key);
        checkValue(value);

        RFuture<V> future = replaceOperationAsync(key, value);
        if (mapWriter == null) {
            return future;
        }

        RPromise<V> result = new MapWriterExecutorPromise<V>(future, commandExecutor) {
            @Override
            protected void executeWriter() {
                mapWriter.write(key, value);
            }

            @Override
            protected boolean condition(Future<V> future) {
                return future.getNow() != null;
            }
        };

        return result;
    }

    protected RFuture<V> replaceOperationAsync(final K key, final V value) {
        return commandExecutor.evalWriteAsync(getName(key), codec, RedisCommands.EVAL_MAP_VALUE,
                "if redis.call('hexists', KEYS[1], ARGV[1]) == 1 then "
                    + "local v = redis.call('hget', KEYS[1], ARGV[1]); "
                    + "redis.call('hset', KEYS[1], ARGV[1], ARGV[2]); "
                    + "return v; "
                + "else "
                    + "return nil; "
                + "end",
            Collections.<Object>singletonList(getName(key)), encodeMapKey(key), encodeMapValue(value));
    }

    protected RFuture<V> getOperationAsync(K key) {
        return commandExecutor.readAsync(getName(key), codec, RedisCommands.HGET, getName(key), key);
    }

    @Override
    public RFuture<V> getAsync(final K key) {
        checkKey(key);

        RFuture<V> future = getOperationAsync(key);
        if (mapLoader == null) {
            return future;
        }

        final RPromise<V> result = new RedissonPromise<V>();
        future.addListener(new FutureListener<V>() {
            @Override
            public void operationComplete(Future<V> future) throws Exception {
                if (!future.isSuccess()) {
                    result.tryFailure(future.cause());
                    return;
                }

                if (future.getNow() == null) {
                    loadValue(key, result, false);
                } else {
                    result.trySuccess(future.getNow());
                }
            }
        });
        return result;
    }

    private RFuture<V> externalPutAsync(K key, V value) {
        return putAsync(key, value);
    }

    @Override
    public void loadAll(boolean replaceExistingValues, int parallelism) {
        get(loadAllAsync(replaceExistingValues, parallelism));
    }

    @Override
    public RFuture<Void> loadAllAsync(boolean replaceExistingValues, int parallelism) {
        return loadAllAsync(mapLoader.loadAllKeys(), replaceExistingValues, parallelism, null);
    }

    @Override
    public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, int parallelism) {
        get(loadAllAsync(keys, replaceExistingValues, parallelism));
    }

    @Override
    public RFuture<Void> loadAllAsync(final Set<? extends K> keys, boolean replaceExistingValues, int parallelism) {
        return loadAllAsync((Iterable<K>)keys, replaceExistingValues, parallelism, null);
    }

    private RFuture<Void> loadAllAsync(final Iterable<? extends K> keys, boolean replaceExistingValues, int parallelism, Map<K, V> loadedEntires) {
        if (parallelism < 1) {
            throw new IllegalArgumentException("parallelism can't be lower than 1");
        }

        for (K key : keys) {
            checkKey(key);
        }

        final RPromise<Void> result = new RedissonPromise<Void>();
        final AtomicInteger counter = new AtomicInteger();
        final Iterator<? extends K> iter = keys.iterator();
        for (int i = 0; i < parallelism; i++) {
            if (!iter.hasNext()) {
                if (counter.get() == 0) {
                    result.trySuccess(null);
                }
                break;
            }

            counter.incrementAndGet();
            K key = iter.next();
            if (replaceExistingValues) {
                loadValue(result, counter, iter, key, loadedEntires);
            } else {
                checkAndLoadValue(result, counter, iter, key, loadedEntires);
            }
        }

        return result;
    }

    private void checkAndLoadValue(final RPromise<Void> result, final AtomicInteger counter, final Iterator<? extends K> iter,
            final K key, final Map<K, V> loadedEntires) {
        containsKeyAsync(key).addListener(new FutureListener<Boolean>() {
            @Override
            public void operationComplete(Future<Boolean> future) throws Exception {
                if (!future.isSuccess()) {
                    result.tryFailure(future.cause());
                    return;
                }

                if (!future.getNow()) {
                    RPromise<V> promise = new RedissonPromise<V>();
                    promise.addListener(new FutureListener<V>() {
                        @Override
                        public void operationComplete(Future<V> future) throws Exception {
                            if (!future.isSuccess()) {
                                result.tryFailure(future.cause());
                                return;
                            }

                            if (loadedEntires != null && future.getNow() != null) {
                                loadedEntires.put(key, future.getNow());
                            }

                            checkAndLoadValue(result, counter, iter, loadedEntires);
                        }

                    });
                    loadValue(key, promise, false);
                } else {
                    checkAndLoadValue(result, counter, iter, loadedEntires);
                }
            }
        });
    }

    private void checkAndLoadValue(RPromise<Void> result, AtomicInteger counter, Iterator<? extends K> iter, Map<K, V> loadedEntires) {
        K key = null;
        synchronized (iter) {
            if (iter.hasNext()) {
                key = iter.next();
            }
        }

        if (key == null) {
            if (counter.decrementAndGet() == 0) {
                result.trySuccess(null);
            }
        } else if (!result.isDone()) {
            checkAndLoadValue(result, counter, iter, key, loadedEntires);
        }
    }

    private void loadValue(final RPromise<Void> result, final AtomicInteger counter, final Iterator<? extends K> iter,
            final K key, final Map<K, V> loadedEntires) {
        RPromise<V> promise = new RedissonPromise<V>();
        promise.addListener(new FutureListener<V>() {
            @Override
            public void operationComplete(Future<V> future) throws Exception {
                if (!future.isSuccess()) {
                    result.tryFailure(future.cause());
                    return;
                }

                if (loadedEntires != null && future.getNow() != null) {
                    loadedEntires.put(key, future.getNow());
                }

                K key = null;
                synchronized (iter) {
                    if (iter.hasNext()) {
                        key = iter.next();
                    }
                }

                if (key == null) {
                    if (counter.decrementAndGet() == 0) {
                        result.trySuccess(null);
                    }
                } else if (!result.isDone()) {
                    loadValue(result, counter, iter, key, loadedEntires);
                }
            }
        });
        loadValue(key, promise, true);
    }

    @Override
    public RFuture<V> putAsync(final K key, final V value) {
        checkKey(key);
        checkValue(value);

        RFuture<V> future = putOperationAsync(key, value);
        if (mapWriter == null) {
            return future;
        }

        RPromise<V> result = new MapWriterExecutorPromise<V>(future, commandExecutor) {
            @Override
            public void executeWriter() {
                mapWriter.write(key, value);
            }
        };

        return result;
    }

    protected RFuture<V> putOperationAsync(K key, V value) {
        return commandExecutor.evalWriteAsync(getName(key), codec, RedisCommands.EVAL_MAP_VALUE,
                "local v = redis.call('hget', KEYS[1], ARGV[1]); "
                + "redis.call('hset', KEYS[1], ARGV[1], ARGV[2]); "
                + "return v",
                Collections.<Object>singletonList(getName(key)), encodeMapKey(key), encodeMapValue(value));
    }


    @Override
    public RFuture<V> removeAsync(final K key) {
        checkKey(key);

        RFuture<V> future = removeOperationAsync(key);
        if (mapWriter == null) {
            return future;
        }

        RPromise<V> result = new MapWriterExecutorPromise<V>(future, commandExecutor) {
            @Override
            public void executeWriter() {
                mapWriter.delete(key);
            }
        };

        return result;
    }

    protected RFuture<V> removeOperationAsync(K key) {
        return commandExecutor.evalWriteAsync(getName(key), codec, RedisCommands.EVAL_MAP_VALUE,
                "local v = redis.call('hget', KEYS[1], ARGV[1]); "
                + "redis.call('hdel', KEYS[1], ARGV[1]); "
                + "return v",
                Collections.<Object>singletonList(getName(key)), encodeMapKey(key));
    }

    @Override
    public RFuture<Boolean> fastPutAsync(final K key, final V value) {
        checkKey(key);
        checkValue(value);

        RFuture<Boolean> future = fastPutOperationAsync(key, value);
        if (mapWriter == null) {
            return future;
        }

        RPromise<Boolean> result = new MapWriterExecutorPromise<Boolean>(future, commandExecutor) {
            @Override
            public void executeWriter() {
                mapWriter.write(key, value);
            }
        };

        return result;
    }

    protected RFuture<Boolean> fastPutOperationAsync(K key, V value) {
        return commandExecutor.writeAsync(getName(key), codec, RedisCommands.HSET, getName(key), key, value);
    }

    @Override
    public boolean fastPut(K key, V value) {
        return get(fastPutAsync(key, value));
    }

    @Override
    public RFuture<Long> fastRemoveAsync(final K ... keys) {
        if (keys == null) {
            throw new NullPointerException();
        }

        if (keys.length == 0) {
            return newSucceededFuture(0L);
        }

        if (mapWriter == null) {
            return fastRemoveOperationAsync(keys);
        }

        RFuture<List<Long>> future = fastRemoveOperationBatchAsync(keys);

        final RPromise<Long> result = new RedissonPromise<Long>();
        future.addListener(new FutureListener<List<Long>>() {
            @Override
            public void operationComplete(Future<List<Long>> future) throws Exception {
                if (!future.isSuccess()) {
                    result.tryFailure(future.cause());
                    return;
                }

                final List<K> deletedKeys = new ArrayList<K>();
                for (int i = 0; i < future.getNow().size(); i++) {
                    if (future.getNow().get(i) == 1) {
                        deletedKeys.add(keys[i]);
                    }
                }
                commandExecutor.getConnectionManager().getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        mapWriter.deleteAll(deletedKeys);
                        result.trySuccess((long)deletedKeys.size());
                    }
                });
            }
        });
        return result;
    }

    protected RFuture<List<Long>> fastRemoveOperationBatchAsync(final K... keys) {
        List<Object> args = new ArrayList<Object>(keys.length);
        for (K key : keys) {
            args.add(encodeMapKey(key));
        }

        RFuture<List<Long>> future = commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_LIST,
                        "local result = {}; " +
                        "for i = 1, #ARGV, 1 do "
                        + "local val = redis.call('hdel', KEYS[1], ARGV[i]); "
                        + "table.insert(result, val); "
                      + "end;"
                      + "return result;",
                        Arrays.<Object>asList(getName()),
                        args.toArray());
        return future;
    }

    protected RFuture<Long> fastRemoveOperationAsync(K... keys) {
        List<Object> args = new ArrayList<Object>(keys.length + 1);
        args.add(getName());
        for (K key : keys) {
            args.add(encodeMapKey(key));
        }
        return commandExecutor.writeAsync(getName(), codec, RedisCommands.HDEL, args.toArray());
    }

    @Override
    public long fastRemove(K ... keys) {
        return get(fastRemoveAsync(keys));
    }

    MapScanResult<ScanObjectEntry, ScanObjectEntry> scanIterator(String name, InetSocketAddress client, long startPos) {
        RFuture<MapScanResult<ScanObjectEntry, ScanObjectEntry>> f
            = commandExecutor.readAsync(client, name, new MapScanCodec(codec), RedisCommands.HSCAN, name, startPos);
        return get(f);
    }

    @Override
    public V addAndGet(K key, Number value) {
        return get(addAndGetAsync(key, value));
    }

    @Override
    public RFuture<V> addAndGetAsync(final K key, Number value) {
        checkKey(key);
        checkValue(value);

        final RFuture<V> future = addAndGetOperationAsync(key, value);
        if (mapWriter == null) {
            return future;
        }

        RPromise<V> result = new MapWriterExecutorPromise<V>(future, commandExecutor) {
            @Override
            public void executeWriter() {
                mapWriter.write(key, future.getNow());
            }
        };

        return result;
    }

    protected RFuture<V> addAndGetOperationAsync(K key, Number value) {
        byte[] keyState = encodeMapKey(key);
        RFuture<V> future = commandExecutor.writeAsync(getName(key), StringCodec.INSTANCE,
                new RedisCommand<Object>("HINCRBYFLOAT", new NumberConvertor(value.getClass())),
                getName(key), keyState, new BigDecimal(value.toString()).toPlainString());
        return future;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Map))
            return false;
        Map<?,?> m = (Map<?,?>) o;
        if (m.size() != size())
            return false;

        try {
            Iterator<Entry<K,V>> i = entrySet().iterator();
            while (i.hasNext()) {
                Entry<K,V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key)==null && m.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(m.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        Iterator<Entry<K,V>> i = entrySet().iterator();
        while (i.hasNext())
            h += i.next().hashCode();
        return h;
    }

    protected Iterator<K> keyIterator() {
        return new RedissonMapIterator<K, V, K>(RedissonMap.this) {
            @Override
            protected K getValue(java.util.Map.Entry<ScanObjectEntry, ScanObjectEntry> entry) {
                return (K) entry.getKey().getObj();
            }
        };
    }

    final class KeySet extends AbstractSet<K> {

        @Override
        public Iterator<K> iterator() {
            return keyIterator();
        }

        @Override
        public boolean contains(Object o) {
            return RedissonMap.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return RedissonMap.this.fastRemove((K)o) == 1;
        }

        @Override
        public int size() {
            return RedissonMap.this.size();
        }

        @Override
        public void clear() {
            RedissonMap.this.clear();
        }

    }

    protected Iterator<V> valueIterator() {
        return new RedissonMapIterator<K, V, V>(RedissonMap.this) {
            @Override
            protected V getValue(java.util.Map.Entry<ScanObjectEntry, ScanObjectEntry> entry) {
                return (V) entry.getValue().getObj();
            }
        };
    }

    final class Values extends AbstractCollection<V> {

        @Override
        public Iterator<V> iterator() {
            return valueIterator();
        }

        @Override
        public boolean contains(Object o) {
            return RedissonMap.this.containsValue(o);
        }

        @Override
        public int size() {
            return RedissonMap.this.size();
        }

        @Override
        public void clear() {
            RedissonMap.this.clear();
        }

    }

    protected Iterator<Map.Entry<K,V>> entryIterator() {
        return new RedissonMapIterator<K, V, Map.Entry<K, V>>(RedissonMap.this);
    }

    private void loadValue(final K key, final RPromise<V> result, final boolean replaceValue) {
        final RLock lock = getLock(key);
        final long threadId = Thread.currentThread().getId();
        lock.lockAsync(threadId).addListener(new FutureListener<Void>() {
            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                if (!future.isSuccess()) {
                    lock.unlockAsync(threadId);
                    result.tryFailure(future.cause());
                    return;
                }

                if (replaceValue) {
                    loadValue(key, result, lock, threadId);
                    return;
                }

                getOperationAsync(key).addListener(new FutureListener<V>() {
                    @Override
                    public void operationComplete(Future<V> valueFuture) throws Exception {
                        if (!valueFuture.isSuccess()) {
                            lock.unlockAsync(threadId);
                            result.tryFailure(valueFuture.cause());
                            return;
                        }

                        if (valueFuture.getNow() != null) {
                            unlock(result, lock, threadId, valueFuture.getNow());
                            return;
                        }

                        loadValue(key, result, lock, threadId);
                    }

                });

            }
        });
    }

    private void loadValue(final K key, final RPromise<V> result, final RLock lock,
            final long threadId) {
        commandExecutor.getConnectionManager().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final V value = mapLoader.load(key);
                if (value == null) {
                    result.trySuccess(value);
                    return;
                }

                externalPutAsync(key, value).addListener(new FutureListener<V>() {
                    @Override
                    public void operationComplete(Future<V> future) throws Exception {
                        if (!future.isSuccess()) {
                            lock.unlockAsync(threadId);
                            result.tryFailure(future.cause());
                            return;
                        }

                        unlock(result, lock, threadId, value);
                    }
                });
            }
        });
    }

    private void unlock(final RPromise<V> result, RLock lock, long threadId,
            final V value) {
        lock.unlockAsync(threadId).addListener(new FutureListener<Void>() {
            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                if (!future.isSuccess()) {
                    result.tryFailure(future.cause());
                    return;
                }

                result.trySuccess(value);
            }
        });
    }

    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {

        public final Iterator<Map.Entry<K,V>> iterator() {
            return entryIterator();
        }

        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            Object key = e.getKey();
            V value = get(key);
            return value != null && value.equals(e);
        }

        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return RedissonMap.this.remove(key, value);
            }
            return false;
        }

        public final int size() {
            return RedissonMap.this.size();
        }

        public final void clear() {
            RedissonMap.this.clear();
        }

    }

}
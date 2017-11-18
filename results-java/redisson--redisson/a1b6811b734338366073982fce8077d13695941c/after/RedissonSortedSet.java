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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import org.redisson.client.RedisConnection;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandExecutor;
import org.redisson.core.RSortedSet;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;

/**
 *
 * @author Nikita Koksharov
 *
 * @param <V>
 */
public class RedissonSortedSet<V> extends RedissonObject implements RSortedSet<V> {

    private static class NaturalComparator<V> implements Comparator<V>, Serializable {

        private static final long serialVersionUID = 7207038068494060240L;

        static final NaturalComparator NATURAL_ORDER = new NaturalComparator();

        public int compare(V c1, V c2) {
            Comparable<Object> c1co = (Comparable<Object>) c1;
            Comparable<Object> c2co = (Comparable<Object>) c2;
            return c1co.compareTo(c2co);
        }

    }

    public static class BinarySearchResult<V> {

        private V value;
        private int index;

        public BinarySearchResult(V value) {
            super();
            this.value = value;
        }

        public BinarySearchResult() {
        }

        public void setIndex(int index) {
            this.index = index;
        }
        public int getIndex() {
            return index;
        }

        public V getValue() {
            return value;
        }


    }

    private Comparator<? super V> comparator = NaturalComparator.NATURAL_ORDER;

    CommandExecutor commandExecutor;

    protected RedissonSortedSet(CommandExecutor commandExecutor, String name) {
        super(commandExecutor, name);
        this.commandExecutor = commandExecutor;

        loadComparator();
    }

    public RedissonSortedSet(Codec codec, CommandExecutor commandExecutor, String name) {
        super(codec, commandExecutor, name);
        this.commandExecutor = commandExecutor;

        loadComparator();
    }

    private void loadComparator() {
        commandExecutor.read(getName(), codec, new SyncOperation<Void>() {
            @Override
            public Void execute(Codec codec, RedisConnection conn) {
                loadComparator(conn);
                return null;
            }
        });
    }

    private void loadComparator(RedisConnection connection) {
        try {
            String comparatorSign = connection.sync(StringCodec.INSTANCE, RedisCommands.GET, getComparatorKeyName());
            if (comparatorSign != null) {
                String[] parts = comparatorSign.split(":");
                String className = parts[0];
                String sign = parts[1];

                String result = calcClassSign(className);
                if (!result.equals(sign)) {
                    throw new IllegalStateException("Local class signature of " + className + " differs from used by this SortedSet!");
                }

                Class<?> clazz = Class.forName(className);
                comparator = (Comparator<V>) clazz.newInstance();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // TODO cache result
    private static String calcClassSign(String name) {
        try {
            Class<?> clazz = Class.forName(name);

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(result);
            outputStream.writeObject(clazz);
            outputStream.close();

            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(result.toByteArray());

            return new BigInteger(1, crypt.digest()).toString(16);
        } catch (Exception e) {
            throw new IllegalStateException("Can't calculate sign of " + name, e);
        }
    }

    @Override
    public int size() {
        return commandExecutor.read(getName(), codec, RedisCommands.LLEN_INT, getName());
    }

    private int size(RedisConnection connection) {
        return connection.sync(RedisCommands.LLEN_INT, getName()).intValue();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(final Object o) {
        return commandExecutor.read(getName(), codec, new SyncOperation<Boolean>() {
            @Override
            public Boolean execute(Codec codec, RedisConnection conn) {
                return binarySearch((V)o, codec, conn).getIndex() >= 0;
            }
        });
    }

    public Iterator<V> iterator() {
        final int ind = 0;
        return new Iterator<V>() {

            private int currentIndex = ind - 1;
            private V currentElement;
            private boolean removeExecuted;

            @Override
            public boolean hasNext() {
                int size = size();
                return currentIndex+1 < size && size > 0;
            }

            @Override
            public V next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No such element at index " + currentIndex);
                }
                currentIndex++;
                removeExecuted = false;
                currentElement = RedissonSortedSet.this.get(currentIndex);
                return currentElement;
            }

            @Override
            public void remove() {
                if (removeExecuted) {
                    throw new IllegalStateException("Element been already deleted");
                }
                RedissonSortedSet.this.remove(currentElement);
                currentIndex--;
                removeExecuted = true;
            }

        };
    }

    private V get(int index) {
        return commandExecutor.read(getName(), codec, RedisCommands.LINDEX, getName(), index);
    }

    @Override
    public Object[] toArray() {
        List<V> res = commandExecutor.read(getName(), codec, RedisCommands.LRANGE, getName(), 0, -1);
        return res.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        List<V> res = commandExecutor.read(getName(), codec, RedisCommands.LRANGE, getName(), 0, -1);
        return res.toArray(a);
    }

    @Override
    public boolean add(final V value) {
        return commandExecutor.write(getName(), codec, new SyncOperation<Boolean>() {
            @Override
            public Boolean execute(Codec codec, RedisConnection conn) {
                return add(value, codec, conn);
            }
        });
    }

    public Future<Boolean> addAsync(final V value) {
        final Promise<Boolean> promise = new DefaultPromise<Boolean>(){};
        GlobalEventExecutor.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean res = add(value);
                    promise.setSuccess(res);
                } catch (Exception e) {
                    promise.setFailure(e);
                }
            }
        });
        return promise;
    }

    boolean add(V value, Codec codec, RedisConnection connection) {
        while (true) {
            connection.sync(RedisCommands.WATCH, getName(), getComparatorKeyName());

            checkComparator(connection);

            BinarySearchResult<V> res = binarySearch(value, codec, connection);
            if (res.getIndex() < 0) {
                int index = -(res.getIndex() + 1);

                byte[] encodedValue = null;
                try {
                    encodedValue = codec.getValueEncoder().encode(value);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }

                connection.sync(RedisCommands.MULTI);
                connection.sync(RedisCommands.EVAL_VOID,
                        "local len = redis.call('llen', KEYS[1]);"
                        + "if tonumber(ARGV[1]) < len then "
                            + "local pivot = redis.call('lindex', KEYS[1], ARGV[1]);"
                            + "redis.call('linsert', KEYS[1], 'before', pivot, ARGV[2]);"
                            + "return;"
                        + "end;"
                        + "redis.call('rpush', KEYS[1], ARGV[2]);", 1, getName(), index, encodedValue);
                List<Object> re = connection.sync(codec, RedisCommands.EXEC);
                if (re.size() == 1) {
                    return true;
                }
            } else {
                connection.sync(RedisCommands.UNWATCH);
                return false;
            }
        }
    }

    private void checkComparator(RedisConnection connection) {
        String comparatorSign = connection.sync(StringCodec.INSTANCE, RedisCommands.GET, getComparatorKeyName());
        if (comparatorSign != null) {
            String[] vals = comparatorSign.split(":");
            String className = vals[0];
            if (!comparator.getClass().getName().equals(className)) {
                loadComparator(connection);
            }
        }
    }

    public static double calcIncrement(double value) {
        BigDecimal b = BigDecimal.valueOf(value);
        BigDecimal r = b.remainder(BigDecimal.ONE);
        if (r.compareTo(BigDecimal.ZERO) == 0) {
            return 1;
        }
        double res = 1/Math.pow(10, r.scale());
        return res;
    }

    @Override
    public Future<Boolean> removeAsync(final V value) {
        EventLoopGroup group = commandExecutor.getConnectionManager().getGroup();
        final Promise<Boolean> promise = group.next().newPromise();

        group.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean result = remove(value);
                    promise.setSuccess(result);
                } catch (Exception e) {
                    promise.setFailure(e);
                }
            }
        });

        return promise;
    }

    @Override
    public boolean remove(final Object value) {
        return commandExecutor.write(getName(), codec, new SyncOperation<Boolean>() {
            @Override
            public Boolean execute(Codec codec, RedisConnection conn) {
                return remove(value, codec, conn);
            }
        });
    }

    boolean remove(Object value, Codec codec, RedisConnection conn) {
        while (true) {
            conn.sync(RedisCommands.WATCH, getName());
            BinarySearchResult<V> res = binarySearch((V) value, codec, conn);
            if (res.getIndex() < 0) {
                conn.sync(RedisCommands.UNWATCH);
                return false;
            }

            conn.sync(RedisCommands.MULTI);
            if (res.getIndex() == 0) {
                conn.sync(codec, RedisCommands.LPOP, getName());
            } else {
                conn.sync(RedisCommands.EVAL_VOID,
                        "local len = redis.call('llen', KEYS[1]);"
                                + "local tail = redis.call('lrange', KEYS[1], tonumber(ARGV[1]) + 1, len);"
                                + "redis.call('ltrim', KEYS[1], 0, tonumber(ARGV[1]) - 1);"
                                + "if #tail > 0 then "
                                + "redis.call('rpush', KEYS[1], unpack(tail)); "
                                + "end;", 1, getName(), res.getIndex());
            }

            if (((List<Object>)conn.sync(codec, RedisCommands.EXEC)).size() == 1) {
                return true;
            }
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object object : c) {
            if (!contains(object)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        boolean changed = false;
        for (V v : c) {
            if (add(v)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (Iterator iterator = iterator(); iterator.hasNext();) {
            Object object = (Object) iterator.next();
            if (!c.contains(object)) {
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object obj : c) {
            if (remove(obj)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void clear() {
        delete();
    }

    @Override
    public Comparator<? super V> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<V> subSet(V fromElement, V toElement) {
        throw new UnsupportedOperationException();
//        return new RedissonSubSortedSet<V>(this, connectionManager, fromElement, toElement);
    }

    @Override
    public SortedSet<V> headSet(V toElement) {
        return subSet(null, toElement);
    }

    @Override
    public SortedSet<V> tailSet(V fromElement) {
        return subSet(fromElement, null);
    }

    @Override
    public V first() {
        V res = commandExecutor.read(getName(), codec, RedisCommands.LINDEX, getName(), 0);
        if (res == null) {
            throw new NoSuchElementException();
        }
        return res;
    }

    @Override
    public V last() {
        V res = commandExecutor.read(getName(), codec, RedisCommands.LINDEX, getName(), -1);
        if (res == null) {
            throw new NoSuchElementException();
        }
        return res;
    }

    private String getComparatorKeyName() {
        return "redisson__sortedset__comparator__{" + getName() + "}";
    }

    @Override
    public boolean trySetComparator(Comparator<? super V> comparator) {
        String className = comparator.getClass().getName();
        final String comparatorSign = className + ":" + calcClassSign(className);

        Boolean res = commandExecutor.evalWrite(getName(), StringCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "if redis.call('llen', KEYS[1]) == 0 then "
                + "redis.call('set', KEYS[2], ARGV[1]); "
                + "return 1; "
                + "else "
                + "return 0; "
                + "end",
                Arrays.<Object>asList(getName(), getComparatorKeyName()), comparatorSign);
        if (res) {
            this.comparator = comparator;
        }
        return res;
    }

    private V getAtIndex(Codec codec, int index, RedisConnection connection) {
        return connection.sync(codec, RedisCommands.LINDEX, getName(), index);
    }

    /**
     * Binary search algorithm
     *
     * @param value
     * @param connection
     * @param lowerIndex
     * @param upperIndex
     * @return
     */
    private BinarySearchResult<V> binarySearch(V value, Codec codec, RedisConnection connection, int lowerIndex, int upperIndex) {
        while (lowerIndex <= upperIndex) {
            int index = lowerIndex + (upperIndex - lowerIndex) / 2;

            V res = getAtIndex(codec, index, connection);
            int cmp = comparator.compare(value, res);

            if (cmp == 0) {
                BinarySearchResult<V> indexRes = new BinarySearchResult<V>();
                indexRes.setIndex(index);
                return indexRes;
            } else if (cmp < 0) {
                upperIndex = index - 1;
            } else {
                lowerIndex = index + 1;
            }
        }

        BinarySearchResult<V> indexRes = new BinarySearchResult<V>();
        indexRes.setIndex(-(lowerIndex + 1));
        return indexRes;
    }

    public BinarySearchResult<V> binarySearch(V value, Codec codec, RedisConnection connection) {
        int upperIndex = size(connection) - 1;
        return binarySearch(value, codec, connection, 0, upperIndex);
    }

    public String toString() {
        Iterator<V> it = iterator();
        if (! it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            V e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (! it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

}
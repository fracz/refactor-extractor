package redis.clients.jedis;

import static redis.clients.jedis.Protocol.toByteArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.exceptions.JedisDataException;

abstract class PipelineBase extends Queable implements
        BasicRedisPipeline,
        BinaryRedisPipeline,
        RedisPipeline,
        MultiKeyBinaryRedisPipeline,
        MultiKeyCommandsPipeline {

    protected Client client;

    public Response<Long> append(String key, String value) {
        client.append(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> append(byte[] key, byte[] value) {
        client.append(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> blpop(String arg) {
        String[] temp = new String[1];
        temp[0] = arg;
        client.blpop(temp);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> brpop(String arg) {
        String[] temp = new String[1];
        temp[0] = arg;
        client.brpop(temp);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<byte[]>> blpop(byte[] arg) {
        byte[][] temp = new byte[1][];
        temp[0] = arg;
        client.blpop(temp);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<List<byte[]>> brpop(byte[] arg) {
        byte[][] temp = new byte[1][];
        temp[0] = arg;
        client.brpop(temp);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<List<String>> blpop(String... args) {
        client.blpop(args);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<byte[]>> blpop(byte[]... args) {
        client.blpop(args);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<List<String>> brpop(String... args) {
        client.brpop(args);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<byte[]>> brpop(byte[]... args) {
        client.brpop(args);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<Long> decr(String key) {
        client.decr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> decr(byte[] key) {
        client.decr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> decrBy(String key, long integer) {
        client.decrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> decrBy(byte[] key, long integer) {
        client.decrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> del(String key) {
        client.del(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> del(String... keys) {
        client.del(keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> del(byte[] key) {
        client.del(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> del(byte[]... keys) {
        client.del(keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> echo(String string) {
        client.echo(string);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> echo(byte[] string) {
        client.echo(string);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Boolean> exists(String key) {
        client.exists(key);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Boolean> exists(byte[] key) {
        client.exists(key);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Long> expire(String key, int seconds) {
        client.expire(key, seconds);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> expire(byte[] key, int seconds) {
        client.expire(key, seconds);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> expireAt(String key, long unixTime) {
        client.expireAt(key, unixTime);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> expireAt(byte[] key, long unixTime) {
        client.expireAt(key, unixTime);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> get(String key) {
        client.get(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> get(byte[] key) {
        client.get(key);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Boolean> getbit(String key, long offset) {
        client.getbit(key, offset);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Boolean> getbit(byte[] key, long offset) {
        client.getbit(key, offset);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<String> getrange(String key, long startOffset,
                                     long endOffset) {
        client.getrange(key, startOffset, endOffset);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> getSet(String key, String value) {
        client.getSet(key, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> getSet(byte[] key, byte[] value) {
        client.getSet(key, value);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Long> getrange(byte[] key, long startOffset, long endOffset) {
        client.getrange(key, startOffset, endOffset);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hdel(String key, String field) {
        client.hdel(key, field);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hdel(byte[] key, byte[] field) {
        client.hdel(key, field);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Boolean> hexists(String key, String field) {
        client.hexists(key, field);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Boolean> hexists(byte[] key, byte[] field) {
        client.hexists(key, field);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<String> hget(String key, String field) {
        client.hget(key, field);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> hget(byte[] key, byte[] field) {
        client.hget(key, field);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Map<String, String>> hgetAll(String key) {
        client.hgetAll(key);
        return getResponse(BuilderFactory.STRING_MAP);
    }

    public Response<Map<byte[], byte[]>> hgetAll(byte[] key) {
        client.hgetAll(key);
        return getResponse(BuilderFactory.BYTE_ARRAY_MAP);
    }

    public Response<Long> hincrBy(String key, String field, long value) {
        client.hincrBy(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hincrBy(byte[] key, byte[] field, long value) {
        client.hincrBy(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> hkeys(String key) {
        client.hkeys(key);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<byte[]>> hkeys(byte[] key) {
        client.hkeys(key);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Long> hlen(String key) {
        client.hlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hlen(byte[] key) {
        client.hlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> hmget(String key, String... fields) {
        client.hmget(key, fields);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<byte[]>> hmget(byte[] key, byte[]... fields) {
        client.hmget(key, fields);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<String> hmset(String key, Map<String, String> hash) {
        client.hmset(key, hash);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> hmset(byte[] key, Map<byte[], byte[]> hash) {
        client.hmset(key, hash);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> hset(String key, String field, String value) {
        client.hset(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hset(byte[] key, byte[] field, byte[] value) {
        client.hset(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hsetnx(String key, String field, String value) {
        client.hsetnx(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hsetnx(byte[] key, byte[] field, byte[] value) {
        client.hsetnx(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> hvals(String key) {
        client.hvals(key);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<byte[]>> hvals(byte[] key) {
        client.hvals(key);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<Long> incr(String key) {
        client.incr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> incr(byte[] key) {
        client.incr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> incrBy(String key, long integer) {
        client.incrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> incrBy(byte[] key, long integer) {
        client.incrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> keys(String pattern) {
        client.keys(pattern);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<byte[]>> keys(byte[] pattern) {
        client.keys(pattern);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<String> lindex(String key, int index) {
        client.lindex(key, index);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> lindex(byte[] key, int index) {
        client.lindex(key, index);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Long> linsert(String key, LIST_POSITION where,
                                  String pivot, String value) {
        client.linsert(key, where, pivot, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> linsert(byte[] key, LIST_POSITION where,
                                  byte[] pivot, byte[] value) {
        client.linsert(key, where, pivot, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> llen(String key) {
        client.llen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> llen(byte[] key) {
        client.llen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> lpop(String key) {
        client.lpop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> lpop(byte[] key) {
        client.lpop(key);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Long> lpush(String key, String string) {
        client.lpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lpush(byte[] key, byte[] string) {
        client.lpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lpushx(String key, String string) {
        client.lpushx(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lpushx(byte[] key, byte[] bytes) {
        client.lpushx(key, bytes);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> lrange(String key, long start, long end) {
        client.lrange(key, start, end);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<byte[]>> lrange(byte[] key, long start, long end) {
        client.lrange(key, start, end);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<Long> lrem(String key, long count, String value) {
        client.lrem(key, count, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lrem(byte[] key, long count, byte[] value) {
        client.lrem(key, count, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> lset(String key, long index, String value) {
        client.lset(key, index, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> lset(byte[] key, long index, byte[] value) {
        client.lset(key, index, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> ltrim(String key, long start, long end) {
        client.ltrim(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> ltrim(byte[] key, long start, long end) {
        client.ltrim(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<List<String>> mget(String... keys) {
        client.mget(keys);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<byte[]>> mget(byte[]... keys) {
        client.mget(keys);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<Long> move(String key, int dbIndex) {
        client.move(key, dbIndex);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> move(byte[] key, int dbIndex) {
        client.move(key, dbIndex);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> mset(String... keysvalues) {
        client.mset(keysvalues);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> mset(byte[]... keysvalues) {
        client.mset(keysvalues);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> msetnx(String... keysvalues) {
        client.msetnx(keysvalues);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> msetnx(byte[]... keysvalues) {
        client.msetnx(keysvalues);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> persist(String key) {
        client.persist(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> persist(byte[] key) {
        client.persist(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> rename(String oldkey, String newkey) {
        client.rename(oldkey, newkey);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> rename(byte[] oldkey, byte[] newkey) {
        client.rename(oldkey, newkey);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> renamenx(String oldkey, String newkey) {
        client.renamenx(oldkey, newkey);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> renamenx(byte[] oldkey, byte[] newkey) {
        client.renamenx(oldkey, newkey);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> rpop(String key) {
        client.rpop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> rpop(byte[] key) {
        client.rpop(key);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<String> rpoplpush(String srckey, String dstkey) {
        client.rpoplpush(srckey, dstkey);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> rpoplpush(byte[] srckey, byte[] dstkey) {
        client.rpoplpush(srckey, dstkey);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Long> rpush(String key, String string) {
        client.rpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> rpush(byte[] key, byte[] string) {
        client.rpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> rpushx(String key, String string) {
        client.rpushx(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> rpushx(byte[] key, byte[] string) {
        client.rpushx(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sadd(String key, String member) {
        client.sadd(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sadd(byte[] key, byte[] member) {
        client.sadd(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> scard(String key) {
        client.scard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> scard(byte[] key) {
        client.scard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> sdiff(String... keys) {
        client.sdiff(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<byte[]>> sdiff(byte[]... keys) {
        client.sdiff(keys);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Long> sdiffstore(String dstkey, String... keys) {
        client.sdiffstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sdiffstore(byte[] dstkey, byte[]... keys) {
        client.sdiffstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> set(String key, String value) {
        client.set(key, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> set(byte[] key, byte[] value) {
        client.set(key, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Boolean> setbit(String key, long offset, boolean value) {
        client.setbit(key, offset, value);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Boolean> setbit(byte[] key, long offset, byte[] value) {
        client.setbit(key, offset, value);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<String> setex(String key, int seconds, String value) {
        client.setex(key, seconds, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> setex(byte[] key, int seconds, byte[] value) {
        client.setex(key, seconds, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> setnx(String key, String value) {
        client.setnx(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> setnx(byte[] key, byte[] value) {
        client.setnx(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> setrange(String key, long offset, String value) {
        client.setrange(key, offset, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> setrange(byte[] key, long offset, byte[] value) {
        client.setrange(key, offset, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> sinter(String... keys) {
        client.sinter(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<byte[]>> sinter(byte[]... keys) {
        client.sinter(keys);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Long> sinterstore(String dstkey, String... keys) {
        client.sinterstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sinterstore(byte[] dstkey, byte[]... keys) {
        client.sinterstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Boolean> sismember(String key, String member) {
        client.sismember(key, member);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Boolean> sismember(byte[] key, byte[] member) {
        client.sismember(key, member);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Set<String>> smembers(String key) {
        client.smembers(key);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<byte[]>> smembers(byte[] key) {
        client.smembers(key);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Long> smove(String srckey, String dstkey, String member) {
        client.smove(srckey, dstkey, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> smove(byte[] srckey, byte[] dstkey, byte[] member) {
        client.smove(srckey, dstkey, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> sort(String key) {
        client.sort(key);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<byte[]>> sort(byte[] key) {
        client.sort(key);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<List<String>> sort(String key,
                                       SortingParams sortingParameters) {
        client.sort(key, sortingParameters);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<byte[]>> sort(byte[] key,
                                       SortingParams sortingParameters) {
        client.sort(key, sortingParameters);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<List<String>> sort(String key,
                                       SortingParams sortingParameters, String dstkey) {
        client.sort(key, sortingParameters, dstkey);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<byte[]>> sort(byte[] key,
                                       SortingParams sortingParameters, byte[] dstkey) {
        client.sort(key, sortingParameters, dstkey);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<List<String>> sort(String key, String dstkey) {
        client.sort(key, dstkey);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<byte[]>> sort(byte[] key, byte[] dstkey) {
        client.sort(key, dstkey);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<String> spop(String key) {
        client.spop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> spop(byte[] key) {
        client.spop(key);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<String> srandmember(String key) {
        client.srandmember(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> srandmember(byte[] key) {
        client.srandmember(key);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Long> srem(String key, String member) {
        client.srem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> srem(byte[] key, byte[] member) {
        client.srem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> strlen(String key) {
        client.strlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> strlen(byte[] key) {
        client.strlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> substr(String key, int start, int end) {
        client.substr(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> substr(byte[] key, int start, int end) {
        client.substr(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Set<String>> sunion(String... keys) {
        client.sunion(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<byte[]>> sunion(byte[]... keys) {
        client.sunion(keys);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Long> sunionstore(String dstkey, String... keys) {
        client.sunionstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sunionstore(byte[] dstkey, byte[]... keys) {
        client.sunionstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> ttl(String key) {
        client.ttl(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> ttl(byte[] key) {
        client.ttl(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> type(String key) {
        client.type(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> type(byte[] key) {
        client.type(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> watch(String... keys) {
        client.watch(keys);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> watch(byte[]... keys) {
        client.watch(keys);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> zadd(String key, double score, String member) {
        client.zadd(key, score, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zadd(byte[] key, double score, byte[] member) {
        client.zadd(key, score, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcard(String key) {
        client.zcard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcard(byte[] key) {
        client.zcard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcount(String key, double min, double max) {
        client.zcount(key, min, max);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcount(byte[] key, double min, double max) {
        client.zcount(key, toByteArray(min), toByteArray(max));
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Double> zincrby(String key, double score, String member) {
        client.zincrby(key, score, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Double> zincrby(byte[] key, double score, byte[] member) {
        client.zincrby(key, score, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Long> zinterstore(String dstkey, String... sets) {
        client.zinterstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zinterstore(byte[] dstkey, byte[]... sets) {
        client.zinterstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zinterstore(String dstkey, ZParams params,
                                      String... sets) {
        client.zinterstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zinterstore(byte[] dstkey, ZParams params,
                                      byte[]... sets) {
        client.zinterstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> zrange(String key, int start, int end) {
        client.zrange(key, start, end);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<byte[]>> zrange(byte[] key, int start, int end) {
        client.zrange(key, start, end);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<String>> zrangeByScore(String key, double min,
                                               double max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<byte[]>> zrangeByScore(byte[] key, double min,
                                               double max) {
        return zrangeByScore(key, toByteArray(min), toByteArray(max));
    }

    public Response<Set<String>> zrangeByScore(String key, String min,
                                               String max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<byte[]>> zrangeByScore(byte[] key, byte[] min,
                                               byte[] max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<String>> zrangeByScore(String key, double min,
                                               double max, int offset, int count) {
        client.zrangeByScore(key, min, max, offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<byte[]>> zrangeByScore(byte[] key, double min,
                                               double max, int offset, int count) {
        return zrangeByScore(key, toByteArray(min), toByteArray(max), offset, count);
    }

    public Response<Set<byte[]>> zrangeByScore(byte[] key, byte[] min,
                                               byte[] max, int offset, int count) {
        client.zrangeByScore(key, min, max, offset, count);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(String key, double min,
                                                        double max) {
        client.zrangeByScoreWithScores(key, min, max);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, double min,
                                                        double max) {
        return zrangeByScoreWithScores(key, toByteArray(min), toByteArray(max));
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, byte[] min,
                                                        byte[] max) {
        client.zrangeByScoreWithScores(key, min, max);
        return getResponse(BuilderFactory.TUPLE_ZSET_BINARY);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(String key, double min,
                                                        double max, int offset, int count) {
        client.zrangeByScoreWithScores(key, min, max, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, double min,
                                                        double max, int offset, int count) {
        client.zrangeByScoreWithScores(key, toByteArray(min), toByteArray(max), offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET_BINARY);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, byte[] min,
                                                        byte[] max, int offset, int count) {
        client.zrangeByScoreWithScores(key, min, max, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET_BINARY);
    }

    public Response<Set<String>> zrevrangeByScore(String key, double max,
                                                  double min) {
        client.zrevrangeByScore(key, max, min);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<byte[]>> zrevrangeByScore(byte[] key, double max,
                                                  double min) {
        client.zrevrangeByScore(key, toByteArray(max), toByteArray(min));
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(String key, String max,
                                                  String min) {
        client.zrevrangeByScore(key, max, min);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<byte[]>> zrevrangeByScore(byte[] key, byte[] max,
                                                  byte[] min) {
        client.zrevrangeByScore(key, max, min);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(String key, double max,
                                                  double min, int offset, int count) {
        client.zrevrangeByScore(key, max, min, offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<byte[]>> zrevrangeByScore(byte[] key, double max,
                                                  double min, int offset, int count) {
        client.zrevrangeByScore(key, toByteArray(max), toByteArray(min), offset, count);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<byte[]>> zrevrangeByScore(byte[] key, byte[] max,
                                                  byte[] min, int offset, int count) {
        client.zrevrangeByScore(key, max, min, offset, count);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(String key,
                                                           double max, double min) {
        client.zrevrangeByScoreWithScores(key, max, min);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
                                                           double max, double min) {
        client.zrevrangeByScoreWithScores(key, toByteArray(max), toByteArray(min));
        return getResponse(BuilderFactory.TUPLE_ZSET_BINARY);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
                                                           byte[] max, byte[] min) {
        client.zrevrangeByScoreWithScores(key, max, min);
        return getResponse(BuilderFactory.TUPLE_ZSET_BINARY);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(String key,
                                                           double max, double min, int offset, int count) {
        client.zrevrangeByScoreWithScores(key, max, min, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
                                                           double max, double min, int offset, int count) {
        client.zrevrangeByScoreWithScores(key, toByteArray(max), toByteArray(min), offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET_BINARY);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
                                                           byte[] max, byte[] min, int offset, int count) {
        client.zrevrangeByScoreWithScores(key, max, min, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET_BINARY);
    }

    public Response<Set<Tuple>> zrangeWithScores(String key, int start, int end) {
        client.zrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeWithScores(byte[] key, int start, int end) {
        client.zrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET_BINARY);
    }

    public Response<Long> zrank(String key, String member) {
        client.zrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrank(byte[] key, byte[] member) {
        client.zrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrem(String key, String member) {
        client.zrem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrem(byte[] key, byte[] member) {
        client.zrem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByRank(String key, int start, int end) {
        client.zremrangeByRank(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByRank(byte[] key, int start, int end) {
        client.zremrangeByRank(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByScore(String key, double start, double end) {
        client.zremrangeByScore(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByScore(byte[] key, double start, double end) {
        client.zremrangeByScore(key, toByteArray(start), toByteArray(end));
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByScore(byte[] key, byte[] start, byte[] end) {
        client.zremrangeByScore(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> zrevrange(String key, int start, int end) {
        client.zrevrange(key, start, end);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<byte[]>> zrevrange(byte[] key, int start, int end) {
        client.zrevrange(key, start, end);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeWithScores(String key, int start,
                                                    int end) {
        client.zrevrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeWithScores(byte[] key, int start,
                                                    int end) {
        client.zrevrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Long> zrevrank(String key, String member) {
        client.zrevrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrevrank(byte[] key, byte[] member) {
        client.zrevrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Double> zscore(String key, String member) {
        client.zscore(key, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Double> zscore(byte[] key, byte[] member) {
        client.zscore(key, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Long> zunionstore(String dstkey, String... sets) {
        client.zunionstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zunionstore(byte[] dstkey, byte[]... sets) {
        client.zunionstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zunionstore(String dstkey, ZParams params,
                                      String... sets) {
        client.zunionstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zunionstore(byte[] dstkey, ZParams params,
                                      byte[]... sets) {
        client.zunionstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> bgrewriteaof() {
        client.bgrewriteaof();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> bgsave() {
        client.bgsave();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> configGet(String pattern) {
        client.configGet(pattern);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> configSet(String parameter, String value) {
        client.configSet(parameter, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> brpoplpush(String source, String destination,
                                       int timeout) {
        client.brpoplpush(source, destination, timeout);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> brpoplpush(byte[] source, byte[] destination,
                                       int timeout) {
        client.brpoplpush(source, destination, timeout);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<String> configResetStat() {
        client.configResetStat();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> save() {
        client.save();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> lastsave() {
        client.lastsave();
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> publish(String channel, String message) {
        client.publish(channel, message);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> publish(byte[] channel, byte[] message) {
        client.publish(channel, message);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> randomKey() {
        client.randomKey();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> randomKeyBinary() {
        client.randomKey();
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<String> flushDB() {
        client.flushDB();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> flushAll() {
        client.flushAll();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> info() {
        client.info();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> dbSize() {
        client.dbSize();
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> shutdown() {
        client.shutdown();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> ping() {
        client.ping();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> select(int index) {
        client.select(index);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> bitcount(String key) {
        client.bitcount(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> bitcount(String key, long start, long end) {
        client.bitcount(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> bitcount(byte[] key) {
        client.bitcount(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> bitcount(byte[] key, long start, long end) {
        client.bitcount(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
        client.bitop(op, destKey, srcKeys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> bitop(BitOP op, String destKey, String... srcKeys) {
        client.bitop(op, destKey, srcKeys);
        return getResponse(BuilderFactory.LONG);
    }
}
package org.redisson.client;

import java.util.concurrent.TimeUnit;

import org.redisson.client.handler.RedisData;
import org.redisson.client.protocol.Codec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStringCommand;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

public class RedisConnection implements RedisCommands {

    final Channel channel;
    final RedisClient redisClient;

    public RedisConnection(RedisClient redisClient, Channel channel) {
        super();
        this.redisClient = redisClient;
        this.channel = channel;
    }

    public RedisClient getRedisClient() {
        return redisClient;
    }

    public <R> R await(Future<R> cmd) {
        if (!cmd.awaitUninterruptibly(redisClient.getTimeout(), TimeUnit.MILLISECONDS)) {
            Promise<R> promise = (Promise<R>)cmd;
            RedisTimeoutException ex = new RedisTimeoutException();
            promise.setFailure(ex);
            throw ex;
        }
        if (!cmd.isSuccess()) {
            if (cmd.cause() instanceof RedisException) {
                throw (RedisException) cmd.cause();
            }
            throw new RedisException("Unexpected exception while processing command", cmd.cause());
        }
        return cmd.getNow();
    }

    public <V> V get(Future<V> future) {
        future.awaitUninterruptibly();
        if (future.isSuccess()) {
            return future.getNow();
        }

        if (future.cause() instanceof RedisException) {
            throw (RedisException) future.cause();
        }
        throw new RedisException("Unexpected exception while processing command", future.cause());
    }

    public String sync(RedisStringCommand command, Object ... params) {
        Future<String> r = async(command, params);
        return await(r);
    }

    public Future<String> async(RedisStringCommand command, Object ... params) {
        Promise<String> promise = redisClient.getBootstrap().group().next().<String>newPromise();
        channel.writeAndFlush(new RedisData<String, String>(promise, command.getCodec(), command, params));
        return promise;
    }

    public <T, R> void send(RedisData<T, R> data) {
        channel.writeAndFlush(data);
    }

    public <T, R> R sync(Codec encoder, RedisCommand<T> command, Object ... params) {
        Future<R> r = async(encoder, command, params);
        return await(r);
    }

    public <T, R> Future<R> async(Codec encoder, RedisCommand<T> command, Object ... params) {
        Promise<R> promise = redisClient.getBootstrap().group().next().<R>newPromise();
        channel.writeAndFlush(new RedisData<T, R>(promise, encoder, command, params));
        return promise;
    }

    public ChannelFuture closeAsync() {
        return channel.close();
    }

//  public <R> Future<R> execute(Codec encoder, RedisCommand<R> command, Object ... params) {
//  Promise<R> promise = bootstrap.group().next().<R>newPromise();
//  channel.writeAndFlush(new RedisData<R, R>(promise, encoder, command, params));
//  return promise;
//}



}
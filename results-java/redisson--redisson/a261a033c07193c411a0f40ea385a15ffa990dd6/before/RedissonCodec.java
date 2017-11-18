package org.redisson.codec;

import java.nio.ByteBuffer;

/**
 *
 * @author Nikita Koksharov
 *
 */
public interface RedissonCodec {

    Object decodeKey(ByteBuffer bytes);

    Object decodeValue(ByteBuffer bytes);

    byte[] encodeKey(Object key);

    byte[] encodeValue(Object value);

}
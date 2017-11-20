package uk.co.real_logic.aeron.common.command;

import uk.co.real_logic.aeron.common.Flyweight;

import java.nio.ByteOrder;

import static uk.co.real_logic.aeron.common.BitUtil.SIZE_OF_LONG;

/**
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                            Client ID                          |
 * |                                                               |
 * +---------------------------------------------------------------+
 * |                         Correlation ID                        |
 * |                                                               |
 * +---------------------------------------------------------------+
 */
public class CorrelatedMessageFlyweight extends Flyweight
{
    public static final int CLIENT_ID_FIELD_OFFSET = 0;
    public static final int CORRELATION_ID_FIELD_OFFSET = CLIENT_ID_FIELD_OFFSET + SIZE_OF_LONG;

    public static final int LENGTH = 2 * SIZE_OF_LONG;

    /**
     * return client id field
     *
     * @return client id field
     */
    public long clientId()
    {
        return atomicBuffer().getLong(offset() + CLIENT_ID_FIELD_OFFSET, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * set client id field
     *
     * @param clientId field value
     * @return flyweight
     */
    public CorrelatedMessageFlyweight clientId(final long clientId)
    {
        atomicBuffer().putLong(offset() + CLIENT_ID_FIELD_OFFSET, clientId, ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    /**
     * return correlation id field
     *
     * @return correlation id field
     */
    public long correlationId()
    {
        return atomicBuffer().getLong(offset() + CORRELATION_ID_FIELD_OFFSET, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * set correlation id field
     *
     * @param correlationId field value
     * @return flyweight
     */
    public CorrelatedMessageFlyweight correlationId(final long correlationId)
    {
        atomicBuffer().putLong(offset() + CORRELATION_ID_FIELD_OFFSET, correlationId, ByteOrder.LITTLE_ENDIAN);
        return this;
    }
}
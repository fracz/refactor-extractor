/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry.support.kryo.serial;

import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;
import org.jasig.cas.ticket.support.HardTimeoutExpirationPolicy;

/**
 * Unit test for {@link HardTimeoutExpirationPolicy}.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class HardTimeoutExpirationPolicySerializer extends SimpleSerializer<HardTimeoutExpirationPolicy> {

    protected final FieldHelper fieldHelper;

    public HardTimeoutExpirationPolicySerializer(final FieldHelper helper) {
        this.fieldHelper = helper;
    }

    public void write(final ByteBuffer buffer, final HardTimeoutExpirationPolicy policy) {
        buffer.putLong((Long) fieldHelper.getFieldValue(policy, "timeToKillInMilliSeconds"));
    }

    public HardTimeoutExpirationPolicy read(final ByteBuffer buffer) {
        return new HardTimeoutExpirationPolicy(buffer.getLong());
    }
}
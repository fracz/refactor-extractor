package org.jasig.cas.util;

import org.jasig.cas.mock.MockServiceTicket;
import org.jasig.cas.mock.MockTicketGrantingTicket;
import org.jasig.cas.ticket.Ticket;

import com.google.common.io.ByteSource;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link CompressionUtils}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class TicketEncryptionDecryptionTests {

    private MockTicketGrantingTicket tgt = new MockTicketGrantingTicket("casuser");
    private BinaryCipherExecutor cipher = new BinaryCipherExecutor("1234567890123456",
            "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w", 512, 16);

    @Test
    public void checkSerializationOfTgt() {
        final byte[] bytes = SerializationUtils.serializeAndEncodeObject(cipher, tgt);
        final Ticket obj = SerializationUtils.decodeAndSerializeObject(bytes, cipher, Ticket.class);
        assertNotNull(obj);
    }

    @Test
    public void checkSerializationOfSt() {
        final MockServiceTicket st = new MockServiceTicket("serviceid", org.jasig.cas.services.TestUtils.getService(), tgt);
        final byte[] bytes = SerializationUtils.serializeAndEncodeObject(cipher, st);
        final Ticket obj = SerializationUtils.decodeAndSerializeObject(bytes, cipher, Ticket.class);
        assertNotNull(obj);
    }

    @Test
    public void checkSerializationOfStBase64Encode() {
        final MockServiceTicket st = new MockServiceTicket("serviceid", org.jasig.cas.services.TestUtils.getService(), tgt);
        final byte[] bytes = SerializationUtils.serializeAndEncodeObject(cipher, st);
        final String string = EncodingUtils.encodeBase64(bytes);
        assertNotNull(string);
        final byte[] result = EncodingUtils.decodeBase64(string);
        final Ticket obj = SerializationUtils.decodeAndSerializeObject(result, cipher, Ticket.class);
        assertNotNull(obj);
    }

    @Test
    public void checkSerializationOfTgtByteSource() throws Exception {
        final ByteSource bytes = ByteSource.wrap(SerializationUtils.serializeAndEncodeObject(cipher, tgt));
        final Ticket obj = SerializationUtils.decodeAndSerializeObject(bytes.read(), cipher, Ticket.class);
        assertNotNull(obj);
    }

}
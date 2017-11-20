commit 38a9584e09d661809457813d9687bd935c604d81
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Thu Apr 26 14:26:33 2012 +0000

    Hi all,
    I've slightly refactored the index binary serializer to improve performance and let to 3rd part serializer to be registered. This is the Bayoda's case where they have a SHA256 and now are managing it as string instead of lighter and faster byte[32].

    So I've created this class:

    public class OHash256Serializer implements OBinarySerializer<byte[]> {
            public static final OBinaryTypeSerializer       INSTANCE        = new OBinaryTypeSerializer();
            public static final byte                                        ID                      = 100;
            public static final int                                 LENGTH          = 32;

            public int getObjectSize(final int length) { return length; }
            public int getObjectSize(final byte[] object) { return object.length; }
            public void serialize(final byte[] object, final byte[] stream, final int startPosition) { System.arraycopy(object, 0, stream, startPosition, object.length);   }
            public byte[] deserialize(final byte[] stream, final int startPosition) { return Arrays.copyOfRange(stream, startPosition, startPosition + LENGTH);}
            public int getObjectSize(byte[] stream, int startPosition) { return LENGTH; }
            public byte getId() { return ID; }
    }

    And registered as OBinarySerializerFactory.registerSerializer(OHash256Serializer.ID, OHash256Serializer.INSTANCE);

    OBinarySerializerFactory supportes 2 registration mode: by object for stateless serializer and by class for stateful ones. All are steteless but SimpleKeySerializer I've registered as stateful, so each index has own implementation that cache the TYPE and BINARY SERIALIZER to avoid to lookup it everytime.

    Furthermore OLinkSerializer is now OBinarySerializer<OIdentifiable> instead of OBinarySerializer<ORID>. In this way eats both records and RID without the need to check before to use it.
package com.orientechnologies.orient.core.serialization.serializer.record.binary;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.orientechnologies.common.collection.OMultiCollectionIterator;
import com.orientechnologies.common.collection.OMultiValue;
import com.orientechnologies.common.serialization.types.ODecimalSerializer;
import com.orientechnologies.common.serialization.types.OIntegerSerializer;
import com.orientechnologies.common.serialization.types.OLongSerializer;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag;
import com.orientechnologies.orient.core.id.OClusterPositionLong;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class ORecordSerializerBinaryV0 implements ODocumentSerializer {

  private Charset           utf8;
  private static final long MILLISEC_PER_DAY = 86400000;
  private static final long ONE_HOUR         = 3600000;

  public ORecordSerializerBinaryV0() {
    utf8 = Charset.forName("UTF-8");
  }

  @Override
  public void deserialize(ODocument document, BytesContainer bytes) {
    int last = 0;
    String field;
    while ((field = readString(bytes)) != null) {
      int valuePos = OIntegerSerializer.INSTANCE.deserialize(bytes.bytes, bytes.offset);
      bytes.read(OIntegerSerializer.INT_SIZE);
      OType type = readOType(bytes);
      if (valuePos != 0) {
        int headerCursor = bytes.offset;
        bytes.offset = valuePos;
        Object value = readSingleValue(bytes, type);
        if (bytes.offset > last)
          last = bytes.offset;
        bytes.offset = headerCursor;
        document.field(field, value, type);
      } else
        document.field(field, null, OType.ANY);
    }
    if (last > bytes.offset)
      bytes.offset = last;
  }

  private OType readOType(BytesContainer bytes) {
    OType type = OType.values()[bytes.bytes[bytes.offset]];
    bytes.read(1);
    return type;
  }

  private void writeOType(BytesContainer bytes, int pos, OType type) {
    bytes.bytes[pos] = (byte) type.ordinal();
  }

  private Object readSingleValue(BytesContainer bytes, OType type) {
    Object value = null;
    switch (type) {
    case INTEGER:
      value = OVarIntSerializer.read(bytes).intValue();
      break;
    case LONG:
      value = OVarIntSerializer.read(bytes).longValue();
      break;
    case SHORT:
      value = OVarIntSerializer.read(bytes).shortValue();
      break;
    case STRING:
      value = readString(bytes);
      break;
    case DOUBLE:
      long parsedd = OLongSerializer.INSTANCE.deserialize(bytes.bytes, bytes.offset);
      value = Double.longBitsToDouble(parsedd);
      bytes.read(OLongSerializer.LONG_SIZE);
      break;
    case FLOAT:
      int parsedf = OIntegerSerializer.INSTANCE.deserialize(bytes.bytes, bytes.offset);
      value = Float.intBitsToFloat(parsedf);
      bytes.read(OIntegerSerializer.INT_SIZE);
      break;
    case BYTE:
      value = bytes.bytes[bytes.offset];
      bytes.read(1);
      break;
    case BOOLEAN:
      value = bytes.bytes[bytes.offset] == 1 ? true : false;
      bytes.read(1);
      break;
    case DATETIME:
      value = new Date(OVarIntSerializer.read(bytes).longValue());
      break;
    case DATE:
      value = new Date((OVarIntSerializer.read(bytes).longValue() * MILLISEC_PER_DAY) - ONE_HOUR);
      break;
    case EMBEDDED:
      value = new ODocument();
      deserialize((ODocument) value, bytes);
      break;
    case EMBEDDEDSET:
      value = readEmbeddedCollection(bytes, new HashSet<Object>());
      break;
    case EMBEDDEDLIST:
      value = readEmbeddedCollection(bytes, new ArrayList<Object>());
      break;
    case LINKSET:
      value = readLinkCollection(bytes, new HashSet<OIdentifiable>());
      break;
    case LINKLIST:
      value = readLinkCollection(bytes, new ArrayList<OIdentifiable>());
      break;
    case BINARY:
      Number n = OVarIntSerializer.read(bytes);
      byte[] newValue = new byte[n.intValue()];
      System.arraycopy(bytes.bytes, bytes.offset, newValue, 0, newValue.length);
      bytes.read(n.intValue());
      value = newValue;
      break;
    case LINK:
      value = readOptimizedLink(bytes);
      break;
    case LINKMAP:
      value = readLinkMap(bytes);
      break;
    case EMBEDDEDMAP:
      value = readEmbeddedMap(bytes);
      break;
    case DECIMAL:
      value = ODecimalSerializer.INSTANCE.deserialize(bytes.bytes, bytes.offset);
      bytes.read(ODecimalSerializer.INSTANCE.getObjectSize(bytes.bytes, bytes.offset));
      break;
    case LINKBAG:
      ORidBag bag = new ORidBag();
      bag.fromStream(bytes);
      value = bag;
      break;
    case TRANSIENT:
      break;
    case CUSTOM:
      break;
    case ANY:
      break;
    }
    return value;
  }

  private Map<Object, OIdentifiable> readLinkMap(BytesContainer bytes) {
    int size = OVarIntSerializer.read(bytes).intValue();
    Map<Object, OIdentifiable> result = new HashMap<Object, OIdentifiable>(size);
    while ((size--) > 0) {
      OType keyType = readOType(bytes);
      Object key = readSingleValue(bytes, keyType);
      OIdentifiable value = readOptimizedLink(bytes);
      result.put(key, value);
    }
    return result;
  }

  private Object readEmbeddedMap(BytesContainer bytes) {
    int size = OVarIntSerializer.read(bytes).intValue();
    Map<Object, Object> result = new HashMap<Object, Object>(size);
    while ((size--) > 0) {
      OType keyType = readOType(bytes);
      Object key = readSingleValue(bytes, keyType);
      int valuePos = OIntegerSerializer.INSTANCE.deserialize(bytes.bytes, bytes.offset);
      bytes.read(OIntegerSerializer.INT_SIZE);
      OType type = readOType(bytes);
      int headerCursor = bytes.offset;
      bytes.offset = valuePos;
      Object value = readSingleValue(bytes, type);
      bytes.offset = headerCursor;
      result.put(key, value);
    }
    return result;
  }

  private Collection<OIdentifiable> readLinkCollection(BytesContainer bytes, Collection<OIdentifiable> found) {
    int items = OVarIntSerializer.read(bytes).intValue();
    for (int i = 0; i < items; i++) {
      found.add(readLink(bytes));
    }
    return found;
  }

  private OIdentifiable readLink(BytesContainer bytes) {
    int cluster = OIntegerSerializer.INSTANCE.deserialize(bytes.bytes, bytes.offset);
    bytes.read(OIntegerSerializer.INT_SIZE);
    long record = OLongSerializer.INSTANCE.deserialize(bytes.bytes, bytes.offset);
    bytes.read(OLongSerializer.LONG_SIZE);
    return new ORecordId(cluster, new OClusterPositionLong(record));
  }

  private OIdentifiable readOptimizedLink(BytesContainer bytes) {
    int cluster = OVarIntSerializer.read(bytes).intValue();
    long record = OVarIntSerializer.read(bytes).longValue();
    return new ORecordId(cluster, new OClusterPositionLong(record));
  }

  private Collection<?> readEmbeddedCollection(BytesContainer bytes, Collection<Object> found) {
    int items = OVarIntSerializer.read(bytes).intValue();
    OType type = readOType(bytes);
    if (type == OType.ANY) {
      for (int i = 0; i < items; i++) {
        OType itemType = readOType(bytes);
        found.add(readSingleValue(bytes, itemType));
      }
      return found;
    }
    // TODO: manage case where type is known
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void serialize(ODocument document, BytesContainer bytes) {
    int[] pos = new int[document.fields()];
    int i = 0;
    Entry<String, ?> values[] = new Entry[document.fields()];
    for (Entry<String, Object> entry : document) {
      writeString(bytes, entry.getKey());
      pos[i] = bytes.alloc(OIntegerSerializer.INT_SIZE + 1);
      values[i] = entry;
      i++;
    }
    OVarIntSerializer.write(bytes, 0);

    for (i = 0; i < values.length; i++) {
      int pointer = 0;
      Object value = values[i].getValue();
      if (value != null) {
        OType type = getFieldType(document, values[i].getKey(), value);
        // temporary skip serialization skip of unknown types
        if (type == null)
          continue;
        pointer = writeSingleValue(bytes, value, type);
        OIntegerSerializer.INSTANCE.serialize(pointer, bytes.bytes, pos[i]);
        writeOType(bytes, (pos[i] + OIntegerSerializer.INT_SIZE), type);
      }
    }

  }

  @SuppressWarnings("unchecked")
  private int writeSingleValue(BytesContainer bytes, Object value, OType type) {
    int pointer = 0;
    switch (type) {
    case INTEGER:
    case LONG:
    case SHORT:
      pointer = OVarIntSerializer.write(bytes, ((Number) value).longValue());
      break;
    case STRING:
      pointer = writeString(bytes, (String) value);
      break;
    case DOUBLE:
      long dg = Double.doubleToLongBits((Double) value);
      pointer = bytes.alloc(OLongSerializer.LONG_SIZE);
      OLongSerializer.INSTANCE.serialize(dg, bytes.bytes, pointer);
      break;
    case FLOAT:
      int fg = Float.floatToIntBits((Float) value);
      pointer = bytes.alloc(OIntegerSerializer.INT_SIZE);
      OIntegerSerializer.INSTANCE.serialize(fg, bytes.bytes, pointer);
      break;
    case BYTE:
      pointer = bytes.alloc(1);
      bytes.bytes[pointer] = (Byte) value;
      break;
    case BOOLEAN:
      pointer = bytes.alloc(1);
      bytes.bytes[pointer] = ((Boolean) value) ? (byte) 1 : (byte) 0;
      break;
    case DATETIME:
      pointer = OVarIntSerializer.write(bytes, ((Date) value).getTime());
      break;
    case DATE:
      pointer = OVarIntSerializer.write(bytes, (((Date) value).getTime() + ONE_HOUR) / MILLISEC_PER_DAY);
      break;
    case EMBEDDED:
      pointer = bytes.offset;
      serialize((ODocument) value, bytes);
      break;
    case EMBEDDEDSET:
    case EMBEDDEDLIST:
      if (value.getClass().isArray())
        pointer = writeEmbeddedCollection(bytes, Arrays.asList(OMultiValue.array(value)));
      else
        pointer = writeEmbeddedCollection(bytes, (Collection<?>) value);
      break;
    case DECIMAL:
      BigDecimal decimalValue = (BigDecimal) value;
      pointer = bytes.alloc(ODecimalSerializer.INSTANCE.getObjectSize(decimalValue));
      ODecimalSerializer.INSTANCE.serialize(decimalValue, bytes.bytes, pointer);
      break;
    case BINARY:
      byte[] valueBytes = (byte[]) (value);
      pointer = OVarIntSerializer.write(bytes, valueBytes.length);
      int start = bytes.alloc(valueBytes.length);
      System.arraycopy(valueBytes, 0, bytes.bytes, start, valueBytes.length);
      break;
    case LINKSET:
    case LINKLIST:
      Collection<OIdentifiable> ridCollection = (Collection<OIdentifiable>) value;
      pointer = writeLinkCollection(bytes, ridCollection);
      break;
    case LINK:
      pointer = writeOptimizedLink(bytes, (OIdentifiable) value);
      break;
    case LINKMAP:
      pointer = writeLinkMap(bytes, (Map<Object, OIdentifiable>) value);
      break;
    case EMBEDDEDMAP:
      pointer = writeEmbeddedMap(bytes, (Map<Object, Object>) value);
      break;
    case LINKBAG:
      pointer = ((ORidBag) value).toStream(bytes);
    case TRANSIENT:
      break;
    case CUSTOM:
      break;
    case ANY:
      break;
    }
    return pointer;
  }

  private int writeLinkMap(BytesContainer bytes, Map<Object, OIdentifiable> map) {
    int fullPos = OVarIntSerializer.write(bytes, map.size());
    for (Entry<Object, OIdentifiable> entry : map.entrySet()) {
      // TODO:check skip of complex types
      OType type = getTypeFromValue(entry.getKey(), true);
      writeOType(bytes, bytes.alloc(1), type);
      writeSingleValue(bytes, entry.getKey(), type);

      writeOptimizedLink(bytes, entry.getValue());
    }
    return fullPos;
  }

  @SuppressWarnings("unchecked")
  private int writeEmbeddedMap(BytesContainer bytes, Map<Object, Object> map) {
    int[] pos = new int[map.size()];
    int i = 0;
    Entry<Object, Object> values[] = new Entry[map.size()];
    int fullPos = OVarIntSerializer.write(bytes, map.size());
    for (Entry<Object, Object> entry : map.entrySet()) {
      // TODO:check skip of complex types
      OType type = getTypeFromValue(entry.getKey(), true);
      writeOType(bytes, bytes.alloc(1), type);
      writeSingleValue(bytes, entry.getKey(), type);
      pos[i] = bytes.alloc(OIntegerSerializer.INT_SIZE + 1);
      values[i] = entry;
      i++;
    }

    for (i = 0; i < values.length; i++) {
      int pointer = 0;
      Object value = values[i].getValue();
      OType type = getTypeFromValue(value, true);
      // temporary skip serialization of unknown types
      if (type == null)
        continue;
      pointer = writeSingleValue(bytes, value, type);
      OIntegerSerializer.INSTANCE.serialize(pointer, bytes.bytes, pos[i]);
      writeOType(bytes, (pos[i] + OIntegerSerializer.INT_SIZE), type);
    }
    return fullPos;
  }

  private int writeOptimizedLink(BytesContainer bytes, OIdentifiable link) {
    int pos = OVarIntSerializer.write(bytes, link.getIdentity().getClusterId());
    OVarIntSerializer.write(bytes, link.getIdentity().getClusterPosition().longValue());
    return pos;
  }

  private int writeLink(BytesContainer bytes, OIdentifiable link) {
    int pos = bytes.alloc(OIntegerSerializer.INT_SIZE);
    OIntegerSerializer.INSTANCE.serialize(link.getIdentity().getClusterId(), bytes.bytes, pos);
    int posR = bytes.alloc(OLongSerializer.LONG_SIZE);
    OLongSerializer.INSTANCE.serialize(link.getIdentity().getClusterPosition().longValue(), bytes.bytes, posR);
    return pos;
  }

  private int writeLinkCollection(BytesContainer bytes, Collection<OIdentifiable> value) {
    int pos = OVarIntSerializer.write(bytes, value.size());
    for (OIdentifiable itemValue : value) {
      writeLink(bytes, itemValue);
    }
    return pos;
  }

  private int writeEmbeddedCollection(BytesContainer bytes, Collection<?> value) {
    int pos = OVarIntSerializer.write(bytes, value.size());
    // TODO manage embedded type from schema and autodeterminated.
    writeOType(bytes, bytes.alloc(1), OType.ANY);
    for (Object itemValue : value) {
      // TODO:manage null entry;
      OType type = getTypeFromValue(itemValue, true);
      if (type != null) {
        writeOType(bytes, bytes.alloc(1), type);
        writeSingleValue(bytes, itemValue, type);
      }
    }
    return pos;
  }

  private OType getFieldType(ODocument document, String key, Object fieldValue) {
    OType type = document.fieldType(key);
    if (type == null) {
      type = getTypeFromValue(fieldValue, false);
    }
    return type;
  }

  private OType getTypeFromValue(Object fieldValue, boolean forceEmbedded) {
    OType type = null;
    if (fieldValue.getClass() == byte[].class)
      type = OType.BINARY;
    else if (fieldValue instanceof ORecord<?>) {
      if (fieldValue instanceof ODocument && (((ODocument) fieldValue).hasOwners() || forceEmbedded)) {
        type = OType.EMBEDDED;
      } else
        type = OType.LINK;
    } else if (fieldValue instanceof ORID)
      type = OType.LINK;
    else if (fieldValue instanceof Date)
      type = OType.DATETIME;
    else if (fieldValue instanceof String)
      type = OType.STRING;
    else if (fieldValue instanceof Integer || fieldValue instanceof BigInteger)
      type = OType.INTEGER;
    else if (fieldValue instanceof Long)
      type = OType.LONG;
    else if (fieldValue instanceof Float)
      type = OType.FLOAT;
    else if (fieldValue instanceof Short)
      type = OType.SHORT;
    else if (fieldValue instanceof Byte)
      type = OType.BYTE;
    else if (fieldValue instanceof Double)
      type = OType.DOUBLE;
    else if (fieldValue instanceof BigDecimal)
      type = OType.DECIMAL;
    else if (fieldValue instanceof Boolean)
      type = OType.BOOLEAN;
    else if (fieldValue instanceof ORidBag)
      type = OType.LINKBAG;
    else if (fieldValue instanceof Map<?, ?>)
      type = OType.EMBEDDEDMAP;
    else if (fieldValue instanceof OMultiCollectionIterator<?>)
      type = ((OMultiCollectionIterator<?>) fieldValue).isEmbedded() ? OType.EMBEDDEDLIST : OType.LINKLIST;
    else if (fieldValue.getClass().isArray()) {
      type = OType.EMBEDDEDLIST;
    } else if (fieldValue instanceof Collection<?>) {
      if (fieldValue instanceof Set<?>) {
        type = OType.EMBEDDEDSET;
      } else {
        type = OType.EMBEDDEDLIST;
      }
    }
    return type;
  }

  private String readString(BytesContainer bytes) {
    Number n = OVarIntSerializer.read(bytes);
    int len = n.intValue();
    if (len == 0)
      return null;
    String res = new String(bytes.bytes, bytes.offset, len, utf8);
    bytes.read(len);
    return res;
  }

  private int writeString(BytesContainer bytes, String toWrite) {
    byte[] nameBytes = toWrite.getBytes(utf8);
    int pointer = OVarIntSerializer.write(bytes, nameBytes.length);
    int start = bytes.alloc(nameBytes.length);
    System.arraycopy(nameBytes, 0, bytes.bytes, start, nameBytes.length);
    return pointer;
  }
}
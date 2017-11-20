package com.orientechnologies.orient.core.storage.impl.local.eh;

import java.io.IOException;

import com.orientechnologies.common.serialization.types.OByteSerializer;
import com.orientechnologies.common.serialization.types.OIntegerSerializer;
import com.orientechnologies.common.serialization.types.OLongSerializer;
import com.orientechnologies.orient.core.config.OStorageFileConfiguration;
import com.orientechnologies.orient.core.storage.impl.local.OSingleFileSegment;
import com.orientechnologies.orient.core.storage.impl.local.OStorageLocal;
import com.orientechnologies.orient.core.storage.impl.memory.eh.OEHNodeMetadata;

/**
 * @author Andrey Lomakin
 * @since 11.02.13
 */
public class OEHTreeStateStore extends OSingleFileSegment {
  private static final int   RECORD_SIZE   = 256 * OLongSerializer.LONG_SIZE + 3 * OByteSerializer.BYTE_SIZE;

  public static final String DEF_EXTENSION = ".oet";

  public OEHTreeStateStore(String iPath, String iType) throws IOException {
    super(iPath, iType);
  }

  public OEHTreeStateStore(OStorageLocal iStorage, OStorageFileConfiguration iConfig) throws IOException {
    super(iStorage, iConfig);
  }

  public OEHTreeStateStore(OStorageLocal iStorage, OStorageFileConfiguration iConfig, String iType) throws IOException {
    super(iStorage, iConfig, iType);
  }

  public void setHashTreeSize(long hashTreeSize) throws IOException {
    file.writeHeaderLong(0, hashTreeSize);
  }

  public long getHashTreeSize() throws IOException {
    return file.readHeaderLong(0);
  }

  public void storeTreeState(long[][] hashTree, OEHNodeMetadata[] nodesMetadata, int[] bucketsSizes) throws IOException {
    truncate();

    file.allocateSpace(hashTree.length * RECORD_SIZE + bucketsSizes.length * OIntegerSerializer.INT_SIZE
        + OIntegerSerializer.INT_SIZE);

    long filePosition = 0;
    file.writeInt(filePosition, bucketsSizes.length);

    for (int bucketSize : bucketsSizes) {
      file.writeInt(filePosition, bucketSize);
      filePosition += OIntegerSerializer.INT_SIZE;
    }

    for (int i = 0; i < hashTree.length; i++) {
      long[] node = hashTree[i];
      byte[] nodeContentBuffer = new byte[RECORD_SIZE];
      int offset = 0;

      for (long position : node) {
        OLongSerializer.INSTANCE.serializeNative(position, nodeContentBuffer, offset);
        offset += OLongSerializer.LONG_SIZE;
      }

      OEHNodeMetadata nodeMetadata = nodesMetadata[i];
      nodeContentBuffer[offset++] = (byte) nodeMetadata.getMaxLeftChildDepth();
      nodeContentBuffer[offset++] = (byte) nodeMetadata.getMaxRightChildDepth();
      nodeContentBuffer[offset] = (byte) nodeMetadata.getNodeLocalDepth();

      file.write(filePosition, nodeContentBuffer);
      filePosition += nodeContentBuffer.length;
    }
  }

  public long getBucketsOffset() throws IOException {
    return file.readInt(0) * OIntegerSerializer.INT_SIZE + OIntegerSerializer.INT_SIZE;
  }

  public int[] loadBucketsSizes() throws IOException {
    int len = file.readInt(0);
    final int[] bucketsSizes = new int[len];

    long filePosition = OIntegerSerializer.INT_SIZE;
    for (int i = 0; i < len; i++) {
      bucketsSizes[i] = file.readInt(filePosition);
      filePosition += OIntegerSerializer.INT_SIZE;
    }
    return bucketsSizes;
  }

  public long[] loadTreeNode(int index, long bucketsOffset) throws IOException {
    long nodePosition = index * RECORD_SIZE + bucketsOffset;
    byte[] nodeContentBuffer = new byte[256 * OLongSerializer.LONG_SIZE];
    file.read(nodePosition, nodeContentBuffer, nodeContentBuffer.length);

    long[] node = new long[256];
    for (int i = 0; i < node.length; i++)
      node[i] = OLongSerializer.INSTANCE.deserializeNative(nodeContentBuffer, i * OLongSerializer.LONG_SIZE);

    return node;
  }

  public OEHNodeMetadata loadMetadata(int index, long bucketsOffset) throws IOException {
    long nodePosition = index * RECORD_SIZE + 256 * OLongSerializer.LONG_SIZE + bucketsOffset;
    byte[] nodeContentBuffer = new byte[3];

    file.read(nodePosition, nodeContentBuffer, 3);

    return new OEHNodeMetadata(nodeContentBuffer[0], nodeContentBuffer[1], nodeContentBuffer[2]);
  }
}
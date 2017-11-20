package com.orientechnologies.orient.core.index.sbtree.local;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.orientechnologies.common.directmemory.ODirectMemory;
import com.orientechnologies.common.directmemory.ODirectMemoryFactory;
import com.orientechnologies.common.serialization.types.OLongSerializer;
import com.orientechnologies.orient.core.id.OClusterPositionFactory;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.sbtree.OSBTreeBucket;

/**
 * @author Andrey Lomakin
 * @since 09.08.13
 */
@Test
public class SBTreeLeafBucketTest {
  private final ODirectMemory directMemory = ODirectMemoryFactory.INSTANCE.directMemory();

  public void testInitialization() {
    long pointer = directMemory.allocate(OSBTreeBucket.MAX_BUCKET_SIZE_BYTES);

    OSBTreeBucket<Long> treeBucket = new OSBTreeBucket<Long>(pointer, true, OLongSerializer.INSTANCE);
    Assert.assertEquals(treeBucket.size(), 0);
    Assert.assertTrue(treeBucket.isLeaf());

    treeBucket = new OSBTreeBucket<Long>(pointer, OLongSerializer.INSTANCE);
    Assert.assertEquals(treeBucket.size(), 0);
    Assert.assertTrue(treeBucket.isLeaf());
    Assert.assertEquals(treeBucket.getLeftSibling(), -1);
    Assert.assertEquals(treeBucket.getRightSibling(), -1);

    directMemory.free(pointer);
  }

  public void testSearch() {
    long seed = System.currentTimeMillis();
    System.out.println("testSearch seed : " + seed);

    TreeSet<Long> keys = new TreeSet<Long>();
    Random random = new Random(seed);

    while (keys.size() < 2 * OSBTreeBucket.MAX_BUCKET_SIZE_BYTES / OLongSerializer.LONG_SIZE) {
      keys.add(random.nextLong());
    }

    long pointer = directMemory.allocate(OSBTreeBucket.MAX_BUCKET_SIZE_BYTES);
    OSBTreeBucket<Long> treeBucket = new OSBTreeBucket<Long>(pointer, true, OLongSerializer.INSTANCE);

    int index = 0;
    Map<Long, Integer> keyIndexMap = new HashMap<Long, Integer>();
    for (Long key : keys) {
      if (!treeBucket.addEntry(index, new OSBTreeBucket.SBTreeEntry<Long>(-1, -1, key, new ORecordId(index,
          OClusterPositionFactory.INSTANCE.valueOf(index)))))
        break;
      keyIndexMap.put(key, index);
      index++;
    }

    Assert.assertEquals(treeBucket.size(), keyIndexMap.size());

    for (Map.Entry<Long, Integer> keyIndexEntry : keyIndexMap.entrySet()) {
      int bucketIndex = treeBucket.find(keyIndexEntry.getKey());
      Assert.assertEquals(bucketIndex, (int) keyIndexEntry.getValue());
    }

    directMemory.free(pointer);
  }

  public void testUpdateValue() {
    long seed = System.currentTimeMillis();
    System.out.println("testUpdateValue seed : " + seed);

    TreeSet<Long> keys = new TreeSet<Long>();
    Random random = new Random(seed);

    while (keys.size() < 2 * OSBTreeBucket.MAX_BUCKET_SIZE_BYTES / OLongSerializer.LONG_SIZE) {
      keys.add(random.nextLong());
    }

    long pointer = directMemory.allocate(OSBTreeBucket.MAX_BUCKET_SIZE_BYTES);
    OSBTreeBucket<Long> treeBucket = new OSBTreeBucket<Long>(pointer, true, OLongSerializer.INSTANCE);

    Map<Long, Integer> keyIndexMap = new HashMap<Long, Integer>();
    int index = 0;
    for (Long key : keys) {
      if (!treeBucket.addEntry(index, new OSBTreeBucket.SBTreeEntry<Long>(-1, -1, key, new ORecordId(index,
          OClusterPositionFactory.INSTANCE.valueOf(index)))))
        break;

      keyIndexMap.put(key, index);
      index++;
    }

    Assert.assertEquals(keyIndexMap.size(), treeBucket.size());

    for (int i = 0; i < treeBucket.size(); i++)
      treeBucket.updateValue(i, new ORecordId(i + 5, OClusterPositionFactory.INSTANCE.valueOf(i + 5)));

    for (Map.Entry<Long, Integer> keyIndexEntry : keyIndexMap.entrySet()) {
      OSBTreeBucket.SBTreeEntry<Long> entry = treeBucket.getEntry(keyIndexEntry.getValue());

      Assert.assertEquals(entry,
          new OSBTreeBucket.SBTreeEntry<Long>(-1, -1, keyIndexEntry.getKey(), new ORecordId(keyIndexEntry.getValue() + 5,
              OClusterPositionFactory.INSTANCE.valueOf(keyIndexEntry.getValue() + 5))));
      Assert.assertEquals(keyIndexEntry.getKey(), treeBucket.getKey(keyIndexEntry.getValue()));
    }

    directMemory.free(pointer);
  }

  public void testShrink() {
    long seed = System.currentTimeMillis();
    System.out.println("testShrink seed : " + seed);

    TreeSet<Long> keys = new TreeSet<Long>();
    Random random = new Random(seed);

    while (keys.size() < 2 * OSBTreeBucket.MAX_BUCKET_SIZE_BYTES / OLongSerializer.LONG_SIZE) {
      keys.add(random.nextLong());
    }

    long pointer = directMemory.allocate(OSBTreeBucket.MAX_BUCKET_SIZE_BYTES);
    OSBTreeBucket<Long> treeBucket = new OSBTreeBucket<Long>(pointer, true, OLongSerializer.INSTANCE);

    int index = 0;
    for (Long key : keys) {
      if (!treeBucket.addEntry(index, new OSBTreeBucket.SBTreeEntry<Long>(-1, -1, key, new ORecordId(index,
          OClusterPositionFactory.INSTANCE.valueOf(index)))))
        break;

      index++;
    }

    int originalSize = treeBucket.size();

    treeBucket.shrink(treeBucket.size() / 2);
    Assert.assertEquals(treeBucket.size(), index / 2);

    index = 0;
    final Map<Long, Integer> keyIndexMap = new HashMap<Long, Integer>();

    Iterator<Long> keysIterator = keys.iterator();
    while (keysIterator.hasNext() && index < treeBucket.size()) {
      Long key = keysIterator.next();
      keyIndexMap.put(key, index);
      index++;
    }

    for (Map.Entry<Long, Integer> keyIndexEntry : keyIndexMap.entrySet()) {
      int bucketIndex = treeBucket.find(keyIndexEntry.getKey());
      Assert.assertEquals(bucketIndex, (int) keyIndexEntry.getValue());
    }

    int keysToAdd = originalSize - treeBucket.size();
    int addedKeys = 0;
    while (keysIterator.hasNext() && index < originalSize) {
      Long key = keysIterator.next();

      if (!treeBucket.addEntry(index, new OSBTreeBucket.SBTreeEntry<Long>(-1, -1, key, new ORecordId(index,
          OClusterPositionFactory.INSTANCE.valueOf(index)))))
        break;

      keyIndexMap.put(key, index);
      index++;
      addedKeys++;
    }

    for (Map.Entry<Long, Integer> keyIndexEntry : keyIndexMap.entrySet()) {
      OSBTreeBucket.SBTreeEntry<Long> entry = treeBucket.getEntry(keyIndexEntry.getValue());

      Assert.assertEquals(entry,
          new OSBTreeBucket.SBTreeEntry<Long>(-1, -1, keyIndexEntry.getKey(), new ORecordId(keyIndexEntry.getValue(),
              OClusterPositionFactory.INSTANCE.valueOf(keyIndexEntry.getValue()))));
    }

    Assert.assertEquals(treeBucket.size(), originalSize);
    Assert.assertEquals(addedKeys, keysToAdd);

    directMemory.free(pointer);
  }

  public void testRemove() {
    long seed = System.currentTimeMillis();
    System.out.println("testRemove seed : " + seed);

    TreeSet<Long> keys = new TreeSet<Long>();
    Random random = new Random(seed);

    while (keys.size() < 2 * OSBTreeBucket.MAX_BUCKET_SIZE_BYTES / OLongSerializer.LONG_SIZE) {
      keys.add(random.nextLong());
    }

    long pointer = directMemory.allocate(OSBTreeBucket.MAX_BUCKET_SIZE_BYTES);
    OSBTreeBucket<Long> treeBucket = new OSBTreeBucket<Long>(pointer, true, OLongSerializer.INSTANCE);

    int index = 0;
    for (Long key : keys) {
      if (!treeBucket.addEntry(index, new OSBTreeBucket.SBTreeEntry<Long>(-1, -1, key, new ORecordId(index,
          OClusterPositionFactory.INSTANCE.valueOf(index)))))
        break;

      index++;
    }

    int originalSize = treeBucket.size();

    int itemsToDelete = originalSize / 2;
    for (int i = 0; i < itemsToDelete; i++) {
      treeBucket.remove(treeBucket.size() - 1);
    }

    Assert.assertEquals(treeBucket.size(), originalSize - itemsToDelete);

    final Map<Long, Integer> keyIndexMap = new HashMap<Long, Integer>();
    Iterator<Long> keysIterator = keys.iterator();

    index = 0;
    while (keysIterator.hasNext() && index < treeBucket.size()) {
      Long key = keysIterator.next();
      keyIndexMap.put(key, index);
      index++;
    }

    for (Map.Entry<Long, Integer> keyIndexEntry : keyIndexMap.entrySet()) {
      int bucketIndex = treeBucket.find(keyIndexEntry.getKey());
      Assert.assertEquals(bucketIndex, (int) keyIndexEntry.getValue());
    }

    int keysToAdd = originalSize - treeBucket.size();
    int addedKeys = 0;
    while (keysIterator.hasNext() && index < originalSize) {
      Long key = keysIterator.next();

      if (!treeBucket.addEntry(index, new OSBTreeBucket.SBTreeEntry<Long>(-1, -1, key, new ORecordId(index,
          OClusterPositionFactory.INSTANCE.valueOf(index)))))
        break;

      keyIndexMap.put(key, index);
      index++;
      addedKeys++;
    }

    for (Map.Entry<Long, Integer> keyIndexEntry : keyIndexMap.entrySet()) {
      OSBTreeBucket.SBTreeEntry<Long> entry = treeBucket.getEntry(keyIndexEntry.getValue());

      Assert.assertEquals(entry,
          new OSBTreeBucket.SBTreeEntry<Long>(-1, -1, keyIndexEntry.getKey(), new ORecordId(keyIndexEntry.getValue(),
              OClusterPositionFactory.INSTANCE.valueOf(keyIndexEntry.getValue()))));
    }

    Assert.assertEquals(treeBucket.size(), originalSize);
    Assert.assertEquals(addedKeys, keysToAdd);

    directMemory.free(pointer);
  }

  public void testSetLeftSibling() {
    long pointer = directMemory.allocate(OSBTreeBucket.MAX_BUCKET_SIZE_BYTES);
    OSBTreeBucket<Long> treeBucket = new OSBTreeBucket<Long>(pointer, true, OLongSerializer.INSTANCE);
    treeBucket.setLeftSibling(123);
    Assert.assertEquals(treeBucket.getLeftSibling(), 123);

    directMemory.free(pointer);
  }

  public void testSetRightSibling() {
    long pointer = directMemory.allocate(OSBTreeBucket.MAX_BUCKET_SIZE_BYTES);
    OSBTreeBucket<Long> treeBucket = new OSBTreeBucket<Long>(pointer, true, OLongSerializer.INSTANCE);
    treeBucket.setRightSibling(123);
    Assert.assertEquals(treeBucket.getRightSibling(), 123);

    directMemory.free(pointer);
  }
}
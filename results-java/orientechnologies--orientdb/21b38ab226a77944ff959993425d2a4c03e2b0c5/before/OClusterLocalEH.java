/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.storage.impl.local.eh;

import java.io.IOException;
import java.util.Iterator;

import com.orientechnologies.common.concur.resource.OSharedResourceAdaptive;
import com.orientechnologies.common.io.OFileUtils;
import com.orientechnologies.common.util.OPair;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.config.OStorageClusterConfiguration;
import com.orientechnologies.orient.core.config.OStorageEHClusterConfiguration;
import com.orientechnologies.orient.core.config.OStorageFileConfiguration;
import com.orientechnologies.orient.core.config.OStorageSegmentConfiguration;
import com.orientechnologies.orient.core.id.OClusterPosition;
import com.orientechnologies.orient.core.id.OClusterPositionFactory;
import com.orientechnologies.orient.core.storage.OCluster;
import com.orientechnologies.orient.core.storage.OClusterEntryIterator;
import com.orientechnologies.orient.core.storage.OPhysicalPosition;
import com.orientechnologies.orient.core.storage.OStorage;
import com.orientechnologies.orient.core.storage.fs.OFile;
import com.orientechnologies.orient.core.storage.fs.OFileFactory;
import com.orientechnologies.orient.core.storage.impl.local.OMultiFileSegment;
import com.orientechnologies.orient.core.storage.impl.local.OStorageLocal;
import com.orientechnologies.orient.core.storage.impl.local.OStorageVariableParser;
import com.orientechnologies.orient.core.storage.impl.memory.eh.OEHNodeMetadata;
import com.orientechnologies.orient.core.version.ORecordVersion;

/**
 * @author Andrey Lomakin
 * @since 06.02.13
 */
public class OClusterLocalEH extends OSharedResourceAdaptive implements OCluster {
  private static final double                       MERGE_THRESHOLD   = 0.2;

  public static final String                        TYPE              = "PHYSICAL";

  private long[][]                                  hashTree;
  private OEHNodeMetadata[]                         nodesMetadata;

  private OEHFileMetadata[]                         filesMetadata     = new OEHFileMetadata[64];

  private int                                       hashTreeSize;
  private long                                      size;
  private long                                      tombstonesCount;

  private int                                       hashTreeTombstone = -1;

  private final int                                 maxLevelDepth     = 8;
  private final int                                 maxLevelSize;

  private final int                                 levelMask;

  private OStorageLocal                             storage;

  private int                                       id;
  private String                                    name;

  private final int                                 bucketBufferSize;
  private final int                                 keySize;
  private final int                                 entreeSize;

  private OEHFileMetadataStore                      metadataStore;
  private OEHTreeStateStore                         treeStateStore;

  private OStorageEHClusterConfiguration            config;

  private final OClusterPosition                    zeroKey;
  private final OClusterPositionFactory             clusterPositionFactory;
  private final boolean                             removeDataInTruncate;

  private final ThreadLocal<OPair<Long, OEHBucket>> bucketToTrack     = new ThreadLocal<OPair<Long, OEHBucket>>();

  public OClusterLocalEH() {
    super(OGlobalConfiguration.ENVIRONMENT_CONCURRENT.getValueAsBoolean());

    this.maxLevelSize = 1 << maxLevelDepth;
    this.levelMask = Integer.MAX_VALUE >>> (31 - maxLevelDepth);
    keySize = OClusterPositionFactory.INSTANCE.getSerializedSize();
    entreeSize = OPhysicalPosition.binarySize();
    bucketBufferSize = OEHBucket.calculateBufferSize(keySize, entreeSize).getBufferSize();

    zeroKey = OClusterPositionFactory.INSTANCE.valueOf(0);
    clusterPositionFactory = OClusterPositionFactory.INSTANCE;

    removeDataInTruncate = true;
  }

  public OClusterLocalEH(final int entreeSize, final int keySize, OClusterPosition zeroKey,
      OClusterPositionFactory clusterPositionFactory, boolean skipTruncateBeforeDelete) {
    super(OGlobalConfiguration.ENVIRONMENT_CONCURRENT.getValueAsBoolean());

    this.maxLevelSize = 1 << maxLevelDepth;
    this.levelMask = Integer.MAX_VALUE >>> (31 - maxLevelDepth);

    this.keySize = keySize;
    this.entreeSize = entreeSize;

    this.bucketBufferSize = OEHBucket.calculateBufferSize(keySize, entreeSize).getBufferSize();
    this.zeroKey = zeroKey;
    this.clusterPositionFactory = clusterPositionFactory;
    this.removeDataInTruncate = skipTruncateBeforeDelete;
  }

  @Override
  public void configure(OStorage iStorage, int iId, String iClusterName, String iLocation, int iDataSegmentId,
      Object... iParameters) throws IOException {
    acquireExclusiveLock();
    try {
      config = new OStorageEHClusterConfiguration(iStorage.getConfiguration(), iId, iClusterName, iLocation, iDataSegmentId);
      init(iStorage, iId, iClusterName, iLocation, iDataSegmentId);
    } finally {
      releaseExclusiveLock();
    }
  }

  @Override
  public void configure(OStorage iStorage, OStorageClusterConfiguration iConfig) throws IOException {
    acquireExclusiveLock();
    try {
      config = (OStorageEHClusterConfiguration) iConfig;
      init(iStorage, config.getId(), config.getName(), config.getLocation(), config.getDataSegmentId());
    } finally {
      releaseExclusiveLock();
    }
  }

  @Override
  public void create(int iStartSize) throws IOException {
    acquireExclusiveLock();
    try {
      for (int i = 0; i < 2; i++) {
        final OEHFileMetadata metadata = createFileMetadata(i);

        filesMetadata[i] = metadata;
      }

      metadataStore.create(-1);
      treeStateStore.create(-1);

      initHashTreeState();

      if (config.root.clusters.size() <= config.id)
        config.root.clusters.add(config);
      else
        config.root.clusters.set(config.id, config);
    } finally {
      releaseExclusiveLock();
    }
  }

  private OEHFileMetadata createFileMetadata(int i) throws IOException {
    final OEHFileMetadata metadata = new OEHFileMetadata();
    final OStorageSegmentConfiguration fileConfiguration = new OStorageSegmentConfiguration(storage.getConfiguration(), name + i, i);
    final OMultiFileSegment bucketFile = new OMultiFileSegment(storage, fileConfiguration, OEHFileMetadata.DEF_EXTENSION,
        bucketBufferSize);

    bucketFile.create(bucketBufferSize * maxLevelSize);

    metadata.setFile(bucketFile);
    return metadata;
  }

  private void initHashTreeState() throws IOException {
    final byte[] emptyBuffer = new byte[bucketBufferSize];
    final OEHBucket emptyBucket = new OEHBucket(maxLevelDepth, emptyBuffer, 0, keySize, entreeSize, clusterPositionFactory);

    final OMultiFileSegment zeroLevelFile = filesMetadata[0].getFile();

    zeroLevelFile.allocateSpace(bucketBufferSize * maxLevelSize);

    for (long filePosition = 0; filePosition < bucketBufferSize * maxLevelSize; filePosition += bucketBufferSize)
      saveBucket(0, filePosition, emptyBucket);

    filesMetadata[0].setBucketsCount(maxLevelSize);

    final long[] rootTree = new long[maxLevelSize];
    for (int i = 0; i < maxLevelSize; i++)
      rootTree[i] = createBucketPointer(i * bucketBufferSize, 0);

    hashTree = new long[1][];
    hashTree[0] = rootTree;

    nodesMetadata = new OEHNodeMetadata[1];
    nodesMetadata[0] = new OEHNodeMetadata((byte) 0, (byte) 0, (byte) maxLevelDepth);

    size = 0;
    hashTreeSize = 1;
  }

  @Override
  public void open() throws IOException {
    acquireExclusiveLock();
    try {
      metadataStore.open();
      treeStateStore.open();

      filesMetadata = metadataStore.loadMetadata(storage, bucketBufferSize);
      for (OEHFileMetadata fileMetadata : filesMetadata)
        if (fileMetadata != null)
          fileMetadata.getFile().open();

      size = metadataStore.getRecordsCount();

      hashTreeSize = (int) treeStateStore.getHashTreeSize();

      final int arraySize;
      int bitsCount = Integer.bitCount(hashTreeSize);
      if (bitsCount == 1)
        arraySize = hashTreeSize;
      else
        arraySize = 1 << (Integer.highestOneBit(hashTreeSize) + 1);

      hashTree = new long[arraySize][];
      nodesMetadata = new OEHNodeMetadata[arraySize];

      for (int i = 0; i < hashTreeSize; i++) {
        hashTree[i] = treeStateStore.loadTreeNode(i);
        nodesMetadata[i] = treeStateStore.loadMetadata(i);
      }

      size = metadataStore.getRecordsCount();
      tombstonesCount = metadataStore.getTombstonesCount();
    } finally {
      releaseExclusiveLock();
    }
  }

  protected void init(final OStorage iStorage, final int iId, final String iClusterName, final String iLocation,
      final int iDataSegmentId, final Object... iParameters) throws IOException {
    OFileUtils.checkValidName(iClusterName);

    config.dataSegmentId = iDataSegmentId;
    storage = (OStorageLocal) iStorage;
    name = iClusterName;
    id = iId;

    final OStorageFileConfiguration metadataConfiguration = new OStorageFileConfiguration(null,
        OStorageVariableParser.DB_PATH_VARIABLE + '/' + name + OEHFileMetadataStore.DEF_EXTENSION, OFileFactory.MMAP, "0", "50%");

    final OStorageFileConfiguration treeStateConfiguration = new OStorageFileConfiguration(null,
        OStorageVariableParser.DB_PATH_VARIABLE + '/' + name + OEHTreeStateStore.DEF_EXTENSION, OFileFactory.MMAP, "0", "50%");

    metadataStore = new OEHFileMetadataStore(storage, metadataConfiguration);
    treeStateStore = new OEHTreeStateStore(storage, treeStateConfiguration);
  }

  @Override
  public void close() throws IOException {
    acquireExclusiveLock();
    try {
      if (metadataStore.getFile().isOpen()) {
        storeMetadata();
        metadataStore.close();
      }

      if (treeStateStore.getFile().isOpen()) {
        storeHashTree();
        treeStateStore.close();
      }

      for (OEHFileMetadata metadata : filesMetadata)
        if (metadata != null)
          metadata.getFile().close();
    } finally {
      releaseExclusiveLock();
    }
  }

  private void storeMetadata() throws IOException {
    metadataStore.setRecordsCount(size);
    metadataStore.storeMetadata(filesMetadata);
    metadataStore.setTombstonesCount(tombstonesCount);
  }

  private void storeHashTree() throws IOException {
    treeStateStore.setHashTreeSize(hashTreeSize);
    for (int i = 0; i < hashTreeSize; i++)
      treeStateStore.storeTreeState(i, hashTree[i], nodesMetadata[i]);
  }

  @Override
  public void delete() throws IOException {
    acquireExclusiveLock();
    try {
      truncate();

      for (OEHFileMetadata metadata : filesMetadata)
        if (metadata != null)
          metadata.getFile().delete();

      metadataStore.delete();
      treeStateStore.delete();
    } finally {
      releaseExclusiveLock();
    }

  }

  public void set(ATTRIBUTES iAttribute, Object iValue) throws IOException {
    if (iAttribute == null)
      throw new IllegalArgumentException("attribute is null");

    final String stringValue = iValue != null ? iValue.toString() : null;

    acquireExclusiveLock();
    try {

      switch (iAttribute) {
      case NAME:
        setNameInternal(stringValue);
        break;
      case DATASEGMENT:
        setDataSegmentInternal(stringValue);
        break;
      }

    } finally {
      releaseExclusiveLock();
    }
  }

  private void setNameInternal(final String iNewName) {
    if (storage.getClusterIdByName(iNewName) > -1)
      throw new IllegalArgumentException("Cluster with name '" + iNewName + "' already exists");

    for (OEHFileMetadata fileMetadata : filesMetadata)
      fileMetadata.rename(name, iNewName);

    treeStateStore.rename(name, iNewName);
    metadataStore.rename(name, iNewName);

    config.name = iNewName;
    storage.renameCluster(name, iNewName);
    name = iNewName;
    storage.getConfiguration().update();
  }

  /**
   * Assigns a different data-segment id.
   *
   * @param iName
   *          Data-segment's name
   */
  private void setDataSegmentInternal(final String iName) {
    final int dataId = storage.getDataSegmentIdByName(iName);
    config.dataSegmentId = dataId;
    storage.getConfiguration().update();
  }

  @Override
  public void convertToTombstone(OClusterPosition iPosition) throws IOException {
    acquireExclusiveLock();
    try {
      BucketPath bucketPath = getBucket(iPosition);
      final long bucketPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];
      final int fileLevel = getFileLevel(bucketPointer);
      final long filePosition = getFilePosition(bucketPointer);

      final OEHBucket bucket = readBucket(fileLevel, filePosition);
      final int index = bucket.getIndex(iPosition);
      if (index >= 0) {
        bucket.convertToTombstone(index);
        bucket.toStream();
        saveBucket(fileLevel, filePosition, bucket);
        tombstonesCount++;
      }
    } finally {
      releaseExclusiveLock();
    }
  }

  @Override
  public long getTombstonesCount() {
    acquireSharedLock();
    try {
      return tombstonesCount;
    } finally {
      releaseSharedLock();
    }

  }

  @Override
  public boolean hasTombstonesSupport() {
    return true;
  }

  @Override
  public void truncate() throws IOException {
    storage.checkForClusterPermissions(getName());

    acquireExclusiveLock();
    try {
      if (removeDataInTruncate) {
        BucketPath bucketPath = getBucket(zeroKey);
        while (bucketPath != null) {
          final long[] node = hashTree[bucketPath.nodeIndex];
          final long bucketPointer = node[bucketPath.itemIndex + bucketPath.hashMapOffset];

          final long filePosition = getFilePosition(bucketPointer);
          final int fileLevel = getFileLevel(bucketPointer);

          final OEHBucket bucket = readBucket(fileLevel, filePosition);
          bucketToTrack.set(new OPair<Long, OEHBucket>(bucketPointer, bucket));

          for (int i = 0; i < bucket.size(); i++) {
            final OPhysicalPosition position = bucket.getEntry(i);
            if (storage.checkForRecordValidity(position)) {
              storage.getDataSegmentById(position.dataSegmentId).deleteRecord(position.dataSegmentPos);
            }
          }

          bucketPath = nextBucketToFind(bucketPath, bucket.getDepth());
        }
      }

      bucketToTrack.set(null);

      for (OEHFileMetadata metadata : filesMetadata) {
        if (metadata == null)
          continue;

        metadata.getFile().truncate();
        metadata.setBucketsCount(0);
        metadata.setTombstonePosition(-1);
      }

      metadataStore.truncate();
      treeStateStore.truncate();

      initHashTreeState();
    } finally {
      releaseExclusiveLock();
    }
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public int getDataSegmentId() {
    acquireSharedLock();
    try {
      return config.dataSegmentId;
    } finally {
      releaseSharedLock();
    }

  }

  @Override
  public boolean addPhysicalPosition(OPhysicalPosition iPPosition) throws IOException {
    acquireExclusiveLock();
    try {
      final BucketPath bucketPath = getBucket(iPPosition.clusterPosition);
      long[] node = hashTree[bucketPath.nodeIndex];

      final long bucketPointer = node[bucketPath.itemIndex + bucketPath.hashMapOffset];
      if (bucketPointer == 0)
        throw new IllegalStateException("In this version of hash table buckets are added through split only.");

      final int fileLevel = getFileLevel(bucketPointer);
      final long filePosition = getFilePosition(bucketPointer);

      final OEHBucket bucket = readBucket(fileLevel, filePosition);

      assert bucket.getDepth() - maxLevelDepth == fileLevel;

      if (bucket.getIndex(iPPosition.clusterPosition) > -1)
        return false;

      if (bucket.size() < OEHBucket.MAX_BUCKET_SIZE) {
        bucket.addEntry(iPPosition);

        assert bucket.getEntry(bucket.getIndex(iPPosition.clusterPosition)).equals(iPPosition);

        saveBucket(fileLevel, filePosition, bucket);

        size++;
        return true;
      }

      final BucketSplitResult splitResult = splitBucket(bucket, fileLevel, filePosition);

      final long updatedBucketPointer = splitResult.updatedBucketPointer;
      final long newBucketPointer = splitResult.newBucketPointer;
      final int bucketDepth = splitResult.newDepth;

      if (bucketDepth <= bucketPath.nodeGlobalDepth) {
        updateNodeAfterBucketSplit(bucketPath, bucketDepth, newBucketPointer, updatedBucketPointer);
      } else {
        if (bucketPath.nodeLocalDepth < maxLevelDepth) {
          final NodeSplitResult nodeSplitResult = splitNode(bucketPath, node);

          assert !(nodeSplitResult.allLeftHashMapsEqual && nodeSplitResult.allRightHashMapsEqual);

          final long[] newNode = nodeSplitResult.newNode;

          final int nodeLocalDepth = bucketPath.nodeLocalDepth + 1;
          final int hashMapSize = 1 << nodeLocalDepth;

          assert nodeSplitResult.allRightHashMapsEqual == checkAllMapsContainSameBucket(newNode, hashMapSize);

          int newNodeIndex = -1;
          if (!nodeSplitResult.allRightHashMapsEqual || bucketPath.itemIndex >= maxLevelSize / 2)
            newNodeIndex = addNewNode(newNode, nodeLocalDepth);

          final int updatedItemIndex = bucketPath.itemIndex << 1;
          final int updatedOffset = bucketPath.hashMapOffset << 1;
          final int updatedGlobalDepth = bucketPath.nodeGlobalDepth + 1;

          boolean allLeftHashMapsEqual = nodeSplitResult.allLeftHashMapsEqual;
          boolean allRightHashMapsEqual = nodeSplitResult.allRightHashMapsEqual;

          if (updatedOffset < maxLevelSize) {
            allLeftHashMapsEqual = false;
            final BucketPath updatedBucketPath = new BucketPath(bucketPath.parent, updatedOffset, updatedItemIndex,
                bucketPath.nodeIndex, nodeLocalDepth, updatedGlobalDepth);
            updateNodeAfterBucketSplit(updatedBucketPath, bucketDepth, newBucketPointer, updatedBucketPointer);
          } else {
            allRightHashMapsEqual = false;
            final BucketPath newBucketPath = new BucketPath(bucketPath.parent, updatedOffset - maxLevelSize, updatedItemIndex,
                newNodeIndex, nodeLocalDepth, updatedGlobalDepth);
            updateNodeAfterBucketSplit(newBucketPath, bucketDepth, newBucketPointer, updatedBucketPointer);
          }

          final long[] updatedNode = hashTree[bucketPath.nodeIndex];
          updateNodesAfterSplit(bucketPath, updatedNode, newNode, nodeLocalDepth, hashMapSize, allLeftHashMapsEqual,
              allRightHashMapsEqual, newNodeIndex);

          if (allLeftHashMapsEqual)
            deleteNode(bucketPath.nodeIndex);

        } else {
          addNewLevelNode(bucketPath, node, newBucketPointer, updatedBucketPointer);
        }
      }

      return addPhysicalPosition(iPPosition);
    } finally {
      releaseExclusiveLock();
    }
  }

  private void mergeNodeToParent(long[] node, BucketPath nodePath) {
    final int startIndex = findParentNodeStartIndex(nodePath);
    final int localNodeDepth = nodePath.nodeLocalDepth;
    final int hashMapSize = 1 << localNodeDepth;

    final long[] parentNode = hashTree[nodePath.parent.nodeIndex];
    for (int i = 0, k = startIndex; i < node.length; i += hashMapSize, k++) {
      parentNode[k] = node[i];
    }

    deleteNode(nodePath.nodeIndex);

    final OEHNodeMetadata metadata = nodesMetadata[nodePath.parent.nodeIndex];
    if (nodePath.parent.itemIndex < maxLevelSize / 2) {
      final int maxChildDepth = metadata.getMaxLeftChildDepth();
      if (maxChildDepth == localNodeDepth)
        metadata.setMaxLeftChildDepth(getMaxLevelDepth(parentNode, 0, parentNode.length / 2));
    } else {
      final int maxChildDepth = metadata.getMaxRightChildDepth();
      if (maxChildDepth == localNodeDepth)
        metadata.setMaxRightChildDepth(getMaxLevelDepth(parentNode, parentNode.length / 2, parentNode.length));
    }
  }

  private boolean mergeBucketsAfterDeletion(BucketPath nodePath, OEHBucket bucket) throws IOException {
    final int bucketDepth = bucket.getDepth();
    if (bucket.size() > OEHBucket.MAX_BUCKET_SIZE * MERGE_THRESHOLD)
      return false;

    if (bucketDepth - maxLevelDepth < 1)
      return false;

    int offset = nodePath.nodeGlobalDepth - (bucketDepth - 1);
    BucketPath currentNode = nodePath;
    int nodeLocalDepth = nodePath.nodeLocalDepth;
    while (offset > 0) {
      offset -= nodeLocalDepth;
      if (offset > 0) {
        currentNode = nodePath.parent;
        nodeLocalDepth = currentNode.nodeLocalDepth;
      }
    }

    final int diff = bucketDepth - 1 - (currentNode.nodeGlobalDepth - nodeLocalDepth);
    final int interval = (1 << (nodeLocalDepth - diff - 1));

    int firstStartIndex = currentNode.itemIndex & ((levelMask << (nodeLocalDepth - diff)) & levelMask);
    int firstEndIndex = firstStartIndex + interval;

    final int secondStartIndex = firstEndIndex;
    final int secondEndIndex = secondStartIndex + interval;

    final OEHBucket buddyBucket;

    int buddyLevel;
    long buddyPosition;
    long buddyPointer;

    final long[] node = hashTree[currentNode.nodeIndex];
    if ((currentNode.itemIndex >>> (nodeLocalDepth - diff - 1) & 1) == 1) {
      buddyPointer = node[firstStartIndex + currentNode.hashMapOffset];

      while (buddyPointer < 0) {
        final int nodeIndex = (int) ((buddyPointer & Long.MAX_VALUE) >> 8);
        final int itemOffset = (int) buddyPointer & 0xFF;

        buddyPointer = hashTree[nodeIndex][itemOffset];
      }

      assert buddyPointer > 0;

      buddyLevel = getFileLevel(buddyPointer);
      buddyPosition = getFilePosition(buddyPointer);
    } else {
      buddyPointer = node[secondStartIndex + currentNode.hashMapOffset];

      while (buddyPointer < 0) {
        final int nodeIndex = (int) ((buddyPointer & Long.MAX_VALUE) >> 8);
        final int itemOffset = (int) buddyPointer & 0xFF;

        buddyPointer = hashTree[nodeIndex][itemOffset];
      }

      assert buddyPointer > 0;

      buddyLevel = getFileLevel(buddyPointer);
      buddyPosition = getFilePosition(buddyPointer);
    }

    buddyBucket = readBucket(buddyLevel, buddyPosition);

    if (buddyBucket.getDepth() != bucketDepth)
      return false;

    if (bucket.size() + buddyBucket.size() >= OEHBucket.MAX_BUCKET_SIZE)
      return false;

    for (OPhysicalPosition position : bucket)
      buddyBucket.addEntry(position);

    final long bucketPosition = getFilePosition(hashTree[nodePath.nodeIndex][nodePath.itemIndex + nodePath.hashMapOffset]);

    int oldBuddyLevel = buddyLevel;
    long oldBuddyPosition = buddyPosition;

    filesMetadata[buddyLevel].setBucketsCount(filesMetadata[buddyLevel].geBucketsCount() - 2);

    buddyLevel--;
    filesMetadata[buddyLevel].setBucketsCount(filesMetadata[buddyLevel].geBucketsCount() + 1);

    buddyPosition = buddyBucket.getSplitHistory(buddyLevel);

    assert bucketPosition == oldBuddyPosition - bucketBufferSize || oldBuddyPosition == bucketPosition - bucketBufferSize;

    buddyBucket.setDepth(bucketDepth - 1);
    saveBucket(buddyLevel, buddyPosition, buddyBucket);

    buddyPointer = createBucketPointer(buddyPosition, buddyLevel);

    for (int i = firstStartIndex; i < secondEndIndex; i++)
      updateBucket(currentNode.nodeIndex, i, currentNode.hashMapOffset, buddyPointer);

    assert checkBucketDepth(buddyBucket);

    final OEHFileMetadata oldBuddyFileMetadata = filesMetadata[oldBuddyLevel];
    if (oldBuddyFileMetadata.geBucketsCount() > 0) {
      final OEHBucket tombstone = new OEHBucket(new byte[bucketBufferSize], 0, keySize, entreeSize, clusterPositionFactory);

      if (oldBuddyFileMetadata.getTombstonePosition() >= 0)
        tombstone.setNextRemovedBucketPair(oldBuddyFileMetadata.getTombstonePosition());
      else
        tombstone.setNextRemovedBucketPair(-1);

      oldBuddyFileMetadata.setTombstonePosition(Math.min(bucketPosition, oldBuddyPosition));

      saveBucket(oldBuddyLevel, oldBuddyPosition, tombstone);
    } else
      oldBuddyFileMetadata.setTombstonePosition(-1);

    return true;
  }

  private BucketPath nextBucketToFind(final BucketPath bucketPath, int bucketDepth) {
    int offset = bucketPath.nodeGlobalDepth - bucketDepth;

    BucketPath currentNode = bucketPath;
    int nodeLocalDepth = nodesMetadata[bucketPath.nodeIndex].getNodeLocalDepth();
    assert nodesMetadata[bucketPath.nodeIndex].getNodeLocalDepth() == bucketPath.nodeLocalDepth;

    while (offset > 0) {
      offset -= nodeLocalDepth;
      if (offset > 0) {
        currentNode = bucketPath.parent;
        nodeLocalDepth = currentNode.nodeLocalDepth;
        assert nodesMetadata[currentNode.nodeIndex].getNodeLocalDepth() == currentNode.nodeLocalDepth;
      }
    }

    final int diff = bucketDepth - (currentNode.nodeGlobalDepth - nodeLocalDepth);
    final int interval = (1 << (nodeLocalDepth - diff));
    final int firstStartIndex = currentNode.itemIndex & ((levelMask << (nodeLocalDepth - diff)) & levelMask);

    final BucketPath bucketPathToFind;
    final int globalIndex = firstStartIndex + interval + currentNode.hashMapOffset;
    if (globalIndex >= maxLevelSize)
      bucketPathToFind = nextLevelUp(currentNode);
    else {
      final int hashMapSize = 1 << currentNode.nodeLocalDepth;
      final int hashMapOffset = globalIndex / hashMapSize * hashMapSize;

      final int startIndex = globalIndex - hashMapOffset;

      bucketPathToFind = new BucketPath(currentNode.parent, hashMapOffset, startIndex, currentNode.nodeIndex,
          currentNode.nodeLocalDepth, currentNode.nodeGlobalDepth);
    }

    return nextNonEmptyNode(bucketPathToFind);
  }

  private BucketPath nextNonEmptyNode(BucketPath bucketPath) {
    nextBucketLoop: while (bucketPath != null) {
      final long[] node = hashTree[bucketPath.nodeIndex];
      final int startIndex = bucketPath.itemIndex + bucketPath.hashMapOffset;
      final int endIndex = maxLevelSize;

      for (int i = startIndex; i < endIndex; i++) {
        final long position = node[i];

        if (position > 0) {
          final int hashMapSize = 1 << bucketPath.nodeLocalDepth;
          final int hashMapOffset = (i / hashMapSize) * hashMapSize;
          final int itemIndex = i - hashMapOffset;

          return new BucketPath(bucketPath.parent, hashMapOffset, itemIndex, bucketPath.nodeIndex, bucketPath.nodeLocalDepth,
              bucketPath.nodeGlobalDepth);
        }

        if (position < 0) {
          final int childNodeIndex = (int) ((position & Long.MAX_VALUE) >> 8);
          final int childItemOffset = (int) position & 0xFF;

          final BucketPath parent = new BucketPath(bucketPath.parent, 0, i, bucketPath.nodeIndex, bucketPath.nodeLocalDepth,
              bucketPath.nodeGlobalDepth);

          final int childLocalDepth = nodesMetadata[childNodeIndex].getNodeLocalDepth();
          bucketPath = new BucketPath(parent, childItemOffset, 0, childNodeIndex, childLocalDepth, bucketPath.nodeGlobalDepth
              + childLocalDepth);

          continue nextBucketLoop;
        }
      }

      bucketPath = nextLevelUp(bucketPath);
    }

    return null;
  }

  private BucketPath nextLevelUp(BucketPath bucketPath) {
    if (bucketPath.parent == null)
      return null;

    final int nodeLocalDepth = bucketPath.nodeLocalDepth;
    assert nodesMetadata[bucketPath.nodeIndex].getNodeLocalDepth() == bucketPath.nodeLocalDepth;
    final int pointersSize = 1 << (maxLevelDepth - nodeLocalDepth);

    final BucketPath parent = bucketPath.parent;

    if (parent.itemIndex < maxLevelSize / 2) {
      final int nextParentIndex = (parent.itemIndex / pointersSize + 1) * pointersSize;
      return new BucketPath(parent.parent, 0, nextParentIndex, parent.nodeIndex, parent.nodeLocalDepth, parent.nodeGlobalDepth);
    }

    final int nextParentIndex = ((parent.itemIndex - maxLevelSize / 2) / pointersSize + 1) * pointersSize + maxLevelSize / 2;
    if (nextParentIndex < maxLevelSize)
      return new BucketPath(parent.parent, 0, nextParentIndex, parent.nodeIndex, parent.nodeLocalDepth, parent.nodeGlobalDepth);

    return nextLevelUp(new BucketPath(parent.parent, 0, maxLevelSize - 1, parent.nodeIndex, parent.nodeLocalDepth,
        parent.nodeGlobalDepth));
  }

  private void saveBucket(int fileLevel, long filePosition, OEHBucket bucket) throws IOException {
    OEHFileMetadata fileMetadata = filesMetadata[fileLevel];
    if (fileMetadata == null) {
      fileMetadata = createFileMetadata(fileLevel);
      filesMetadata[fileLevel] = fileMetadata;
    }
    final OMultiFileSegment bucketFile = fileMetadata.getFile();
    bucket.toStream();
    final long[] pos = bucketFile.getRelativePosition(filePosition);
    final OFile file = bucketFile.getFile((int) pos[0]);

    file.write(pos[1], bucket.getDataBuffer());
  }

  private OEHBucket readBucket(int fileLevel, long filePosition) throws IOException {
    final OEHFileMetadata fileMetadata = filesMetadata[fileLevel];
    final OMultiFileSegment bucketFile = fileMetadata.getFile();

    final byte[] serializedFile = new byte[bucketBufferSize];

    final long[] pos = bucketFile.getRelativePosition(filePosition);
    final OFile file = bucketFile.getFile((int) pos[0]);

    file.read(pos[1], serializedFile, serializedFile.length);
    return new OEHBucket(serializedFile, 0, keySize, entreeSize, clusterPositionFactory);
  }

  private OEHBucket readBucket(BucketPath bucketPath) throws IOException {
    long[] node = hashTree[bucketPath.nodeIndex];
    long bucketPointer = node[bucketPath.itemIndex + bucketPath.hashMapOffset];

    long filePosition = getFilePosition(bucketPointer);
    int fileLevel = getFileLevel(bucketPointer);

    return readBucket(fileLevel, filePosition);
  }

  private void updateNodeAfterBucketSplit(BucketPath bucketPath, int bucketDepth, long newBucketPointer, long updatedBucketPointer) {
    int offset = bucketPath.nodeGlobalDepth - (bucketDepth - 1);
    BucketPath currentNode = bucketPath;
    int nodeLocalDepth = bucketPath.nodeLocalDepth;
    while (offset > 0) {
      offset -= nodeLocalDepth;
      if (offset > 0) {
        currentNode = bucketPath.parent;
        nodeLocalDepth = currentNode.nodeLocalDepth;
      }
    }

    final int diff = bucketDepth - 1 - (currentNode.nodeGlobalDepth - nodeLocalDepth);

    final int interval = (1 << (nodeLocalDepth - diff - 1));
    final int firstStartIndex = currentNode.itemIndex & ((levelMask << (nodeLocalDepth - diff)) & levelMask);
    final int firstEndIndex = firstStartIndex + interval;

    final int secondStartIndex = firstEndIndex;
    final int secondEndIndex = secondStartIndex + interval;

    for (int i = firstStartIndex; i < firstEndIndex; i++)
      updateBucket(currentNode.nodeIndex, i, currentNode.hashMapOffset, updatedBucketPointer);

    for (int i = secondStartIndex; i < secondEndIndex; i++)
      updateBucket(currentNode.nodeIndex, i, currentNode.hashMapOffset, newBucketPointer);
  }

  private int addNewNode(long[] newNode, int nodeLocalDepth) {
    if (hashTreeTombstone >= 0) {
      long[] tombstone = hashTree[hashTreeTombstone];

      hashTree[hashTreeTombstone] = newNode;
      nodesMetadata[hashTreeTombstone] = new OEHNodeMetadata((byte) 0, (byte) 0, (byte) nodeLocalDepth);

      final int nodeIndex = hashTreeTombstone;
      if (tombstone != null)
        hashTreeTombstone = (int) tombstone[0];
      else
        hashTreeTombstone = -1;

      return nodeIndex;
    }

    if (hashTreeSize >= hashTree.length) {
      long[][] newHashTree = new long[hashTree.length << 1][];
      System.arraycopy(hashTree, 0, newHashTree, 0, hashTree.length);
      hashTree = newHashTree;
      newHashTree = null;

      OEHNodeMetadata[] newNodeMetadata = new OEHNodeMetadata[nodesMetadata.length << 1];
      System.arraycopy(nodesMetadata, 0, newNodeMetadata, 0, nodesMetadata.length);
      nodesMetadata = newNodeMetadata;
      newNodeMetadata = null;
    }

    hashTree[hashTreeSize] = newNode;
    nodesMetadata[hashTreeSize] = new OEHNodeMetadata((byte) 0, (byte) 0, (byte) nodeLocalDepth);

    hashTreeSize++;

    return hashTreeSize - 1;
  }

  private int splitBucketContent(OEHBucket bucket, OEHBucket updatedBucket, OEHBucket newBucket, int bucketDepth) {
    assert checkBucketDepth(bucket);

    bucketDepth++;

    final Iterator<OEHBucket.BinaryEntry> binaryIterator = bucket.binaryIterator();

    while (binaryIterator.hasNext()) {
      OEHBucket.BinaryEntry binaryEntry = binaryIterator.next();

      if (((binaryEntry.key.longValueHigh() >>> (64 - bucketDepth)) & 1) == 0)
        updatedBucket.appendEntry(binaryEntry.entry);
      else
        newBucket.appendEntry(binaryEntry.entry);
    }

    updatedBucket.setDepth(bucketDepth);
    newBucket.setDepth(bucketDepth);

    assert checkBucketDepth(updatedBucket);
    assert checkBucketDepth(newBucket);

    return bucketDepth;
  }

  private BucketSplitResult splitBucket(OEHBucket bucket, int fileLevel, long filePosition) throws IOException {
    final byte[] bucketBuffer = new byte[bucketBufferSize * 2];

    final OEHBucket updatedBucket = new OEHBucket(bucket, bucketBuffer, 0, keySize, entreeSize, clusterPositionFactory);
    final OEHBucket newBucket = new OEHBucket(bucket, bucketBuffer, bucketBufferSize, keySize, entreeSize, clusterPositionFactory);

    int bucketDepth = bucket.getDepth();
    bucketDepth = splitBucketContent(bucket, updatedBucket, newBucket, bucketDepth);

    final OEHFileMetadata fileMetadata = filesMetadata[fileLevel];
    fileMetadata.setBucketsCount(fileMetadata.geBucketsCount() - 1);

    assert fileMetadata.geBucketsCount() >= 0;

    int newFileLevel = bucketDepth - maxLevelDepth;
    OEHFileMetadata newFileMetadata = filesMetadata[newFileLevel];
    if (newFileMetadata == null) {
      newFileMetadata = createFileMetadata(newFileLevel);
      filesMetadata[newFileLevel] = newFileMetadata;
    }

    final long tombstonePosition = newFileMetadata.getTombstonePosition();

    updatedBucket.setSplitHistory(fileLevel, filePosition);
    newBucket.setSplitHistory(fileLevel, filePosition);

    updatedBucket.toStream();
    newBucket.toStream();

    final long updatedFilePosition;
    if (tombstonePosition >= 0) {
      final OEHBucket tombstone = readBucket(newFileLevel, tombstonePosition);
      newFileMetadata.setTombstonePosition(tombstone.getNextRemovedBucketPair());

      final OMultiFileSegment newFile = newFileMetadata.getFile();
      final long[] pos = newFile.getRelativePosition(filePosition);
      final OFile file = newFile.getFile((int) pos[0]);

      file.write(tombstonePosition, updatedBucket.getDataBuffer());

      updatedFilePosition = tombstonePosition;
    } else {
      final OMultiFileSegment newFile = newFileMetadata.getFile();
      final long[] pos = newFile.allocateSpace(2 * bucketBufferSize);
      final OFile file = newFile.getFile((int) pos[0]);
      file.write(pos[1], updatedBucket.getDataBuffer());
      updatedFilePosition = newFile.getAbsolutePosition(pos);
    }

    final long newFilePosition = updatedFilePosition + bucketBufferSize;
    newFileMetadata.setBucketsCount(newFileMetadata.geBucketsCount() + 2);

    final long updatedBucketPointer = createBucketPointer(updatedFilePosition, newFileLevel);
    final long newBucketPointer = createBucketPointer(newFilePosition, newFileLevel);

    return new BucketSplitResult(updatedBucketPointer, newBucketPointer, bucketDepth);
  }

  private boolean checkBucketDepth(OEHBucket bucket) {
    int bucketDepth = bucket.getDepth();

    if (bucket.size() == 0)
      return true;

    final Iterator<OPhysicalPosition> positionIterator = bucket.iterator();

    long firstValue = positionIterator.next().clusterPosition.longValueHigh() >>> (64 - bucketDepth);
    while (positionIterator.hasNext()) {
      final long value = positionIterator.next().clusterPosition.longValueHigh() >>> (64 - bucketDepth);
      if (value != firstValue)
        return false;
    }

    return true;
  }

  private void updateBucket(int nodeIndex, int itemIndex, int offset, long newFilePosition) {
    final long node[] = hashTree[nodeIndex];

    final long position = node[itemIndex + offset];
    if (position >= 0)
      node[itemIndex + offset] = newFilePosition;
    else {
      final int childNodeIndex = (int) ((position & Long.MAX_VALUE) >>> 8);
      final int childOffset = (int) (position & 0xFF);
      final int childNodeDepth = nodesMetadata[childNodeIndex].getNodeLocalDepth();
      final int interval = 1 << childNodeDepth;
      for (int i = 0; i < interval; i++) {
        updateBucket(childNodeIndex, i, childOffset, newFilePosition);
      }
    }
  }

  private BucketPath getBucket(final OClusterPosition key) {
    final long hash = key.longValueHigh();

    int localNodeDepth = nodesMetadata[0].getNodeLocalDepth();
    int nodeDepth = localNodeDepth;
    BucketPath parentNode = null;
    int nodeIndex = 0;
    int offset = 0;

    int index = (int) ((hash >>> (64 - nodeDepth)) & (levelMask >>> (maxLevelDepth - localNodeDepth)));
    BucketPath currentNode = new BucketPath(parentNode, 0, index, 0, localNodeDepth, nodeDepth);
    do {
      final long position = hashTree[nodeIndex][index + offset];
      if (position >= 0)
        return currentNode;

      nodeIndex = (int) ((position & Long.MAX_VALUE) >>> 8);
      offset = (int) (position & 0xFF);

      localNodeDepth = nodesMetadata[nodeIndex].getNodeLocalDepth();
      nodeDepth += localNodeDepth;

      index = (int) ((hash >>> (64 - nodeDepth)) & (levelMask >>> (maxLevelDepth - localNodeDepth)));

      parentNode = currentNode;
      currentNode = new BucketPath(parentNode, offset, index, nodeIndex, localNodeDepth, nodeDepth);
    } while (nodeDepth <= 64);

    throw new IllegalStateException("Extendible hashing tree in corrupted state.");

  }

  private void addNewLevelNode(BucketPath bucketPath, long[] node, long newBucketPointer, long updatedBucketPointer) {
    final long[] newNode = new long[maxLevelSize];

    final int newNodeDepth;
    final int newNodeStartIndex;
    final int mapInterval;

    if (bucketPath.itemIndex < node.length / 2) {
      final int maxDepth = nodesMetadata[bucketPath.nodeIndex].getMaxLeftChildDepth();
      assert getMaxLevelDepth(node, 0, node.length / 2) == maxDepth;
      if (maxDepth > 0)
        newNodeDepth = maxDepth;
      else
        newNodeDepth = 1;

      mapInterval = 1 << (maxLevelDepth - newNodeDepth);
      newNodeStartIndex = (bucketPath.itemIndex / mapInterval) * mapInterval;
    } else {
      final int maxDepth = nodesMetadata[bucketPath.nodeIndex].getMaxRightChildDepth();
      assert getMaxLevelDepth(node, node.length / 2, node.length) == maxDepth;
      if (maxDepth > 0)
        newNodeDepth = maxDepth;
      else
        newNodeDepth = 1;

      mapInterval = 1 << (maxLevelDepth - newNodeDepth);
      newNodeStartIndex = ((bucketPath.itemIndex - node.length / 2) / mapInterval) * mapInterval + node.length / 2;
    }

    final int newNodeIndex = addNewNode(newNode, newNodeDepth);
    final int mapSize = 1 << newNodeDepth;
    for (int i = 0; i < mapInterval; i++) {
      final int nodeOffset = i + newNodeStartIndex;
      final long bucketPointer = node[nodeOffset];

      if (nodeOffset != bucketPath.itemIndex) {
        for (int n = i << newNodeDepth; n < (i + 1) << newNodeDepth; n++)
          newNode[n] = bucketPointer;
      } else {
        for (int n = i << newNodeDepth; n < (2 * i + 1) << (newNodeDepth - 1); n++)
          newNode[n] = updatedBucketPointer;

        for (int n = (2 * i + 1) << (newNodeDepth - 1); n < (i + 1) << newNodeDepth; n++)
          newNode[n] = newBucketPointer;
      }

      node[nodeOffset] = (newNodeIndex << 8) | (i * mapSize) | Long.MIN_VALUE;
    }

    updateMaxChildDepth(bucketPath, newNodeDepth);
  }

  private int getMaxLevelDepth(long node[], int start, int end) {
    int currentIndex = -1;
    int maxDepth = 0;

    for (int i = start; i < end; i++) {
      final long nodePosition = node[i];
      if (nodePosition >= 0)
        continue;

      final int index = (int) ((nodePosition & Long.MAX_VALUE) >>> 8);
      if (index == currentIndex)
        continue;

      currentIndex = index;
      if (maxDepth < nodesMetadata[index].getNodeLocalDepth())
        maxDepth = nodesMetadata[index].getNodeLocalDepth();
    }

    return maxDepth;
  }

  private void updateNodesAfterSplit(BucketPath bucketPath, long[] node, long[] newNode, int nodeLocalDepth, int hashMapSize,
      boolean allLeftHashMapEquals, boolean allRightHashMapsEquals, int newNodeIndex) {

    final int startIndex = findParentNodeStartIndex(bucketPath);

    final long[] parentNode = hashTree[bucketPath.parent.nodeIndex];
    assert assertParentNodeStartIndex(bucketPath, parentNode, startIndex);

    final int pointersSize = 1 << (maxLevelDepth - nodeLocalDepth);
    if (allLeftHashMapEquals) {
      for (int i = 0; i < pointersSize; i++) {
        final long position = node[i * hashMapSize];
        parentNode[startIndex + i] = position;
      }
    } else {
      for (int i = 0; i < pointersSize; i++)
        parentNode[startIndex + i] = (bucketPath.nodeIndex << 8) | (i * hashMapSize) | Long.MIN_VALUE;
    }

    if (allRightHashMapsEquals) {
      for (int i = 0; i < pointersSize; i++) {
        final long position = newNode[i * hashMapSize];
        parentNode[startIndex + pointersSize + i] = position;
      }
    } else {
      for (int i = 0; i < pointersSize; i++)
        parentNode[startIndex + pointersSize + i] = (newNodeIndex << 8) | (i * hashMapSize) | Long.MIN_VALUE;
    }

    updateMaxChildDepth(bucketPath.parent, bucketPath.nodeLocalDepth + 1);
  }

  private void updateMaxChildDepth(BucketPath parentPath, int childDepth) {
    if (parentPath == null)
      return;

    final OEHNodeMetadata metadata = nodesMetadata[parentPath.nodeIndex];
    if (parentPath.itemIndex < maxLevelSize / 2) {
      final int maxChildDepth = metadata.getMaxLeftChildDepth();
      if (childDepth > maxChildDepth)
        metadata.setMaxLeftChildDepth(childDepth);
    } else {
      final int maxChildDepth = metadata.getMaxRightChildDepth();
      if (childDepth + 1 > maxChildDepth)
        metadata.setMaxRightChildDepth(childDepth);
    }
  }

  private boolean assertParentNodeStartIndex(BucketPath bucketPath, long[] parentNode, int calculatedIndex) {
    int startIndex = -1;
    for (int i = 0; i < parentNode.length; i++)
      if (parentNode[i] < 0 && (parentNode[i] & Long.MAX_VALUE) >>> 8 == bucketPath.nodeIndex) {
        startIndex = i;
        break;
      }

    return startIndex == calculatedIndex;
  }

  private int findParentNodeStartIndex(BucketPath bucketPath) {
    final BucketPath parentBucketPath = bucketPath.parent;
    final int pointersSize = 1 << (maxLevelDepth - bucketPath.nodeLocalDepth);

    if (parentBucketPath.itemIndex < maxLevelSize / 2)
      return (parentBucketPath.itemIndex / pointersSize) * pointersSize;

    return ((parentBucketPath.itemIndex - maxLevelSize / 2) / pointersSize) * pointersSize + maxLevelSize / 2;
  }

  private boolean checkAllMapsContainSameBucket(long[] newNode, int hashMapSize) {
    int n = 0;
    boolean allHashMapsEquals = true;
    while (n < newNode.length) {
      boolean allHashBucketEquals = true;
      for (int i = 0; i < hashMapSize - 1; i++) {
        if (newNode[i + n] != newNode[i + n + 1]) {
          allHashBucketEquals = false;
          break;
        }
      }
      n += hashMapSize;
      if (!allHashBucketEquals) {
        allHashMapsEquals = false;
        break;
      }
    }

    assert assertAllNodesAreFilePointers(allHashMapsEquals, newNode, hashMapSize);

    return allHashMapsEquals;
  }

  private boolean assertAllNodesAreFilePointers(boolean allHashMapsEquals, long[] newNode, int hashMapSize) {
    if (allHashMapsEquals) {
      int n = 0;
      while (n < newNode.length) {
        for (int i = 0; i < hashMapSize; i++) {
          if (newNode[i] < 0) {
            return false;
          }
        }
        n += hashMapSize;
      }
    }

    return true;
  }

  private NodeSplitResult splitNode(BucketPath bucketPath, long[] node) {
    final long[] newNode = new long[maxLevelSize];
    final int hashMapSize = 1 << (bucketPath.nodeLocalDepth + 1);

    boolean hashMapItemsAreEqual = true;
    boolean allLeftItemsAreEqual;
    boolean allRightItemsAreEqual;

    int mapCounter = 0;
    long firstPosition = -1;

    for (int i = maxLevelSize / 2; i < maxLevelSize; i++) {
      final long position = node[i];
      if (hashMapItemsAreEqual && mapCounter == 0)
        firstPosition = position;

      newNode[2 * (i - maxLevelSize / 2)] = position;
      newNode[2 * (i - maxLevelSize / 2) + 1] = position;

      if (hashMapItemsAreEqual) {
        hashMapItemsAreEqual = firstPosition == position;
        mapCounter += 2;

        if (mapCounter >= hashMapSize)
          mapCounter = 0;
      }
    }

    mapCounter = 0;
    allRightItemsAreEqual = hashMapItemsAreEqual;

    hashMapItemsAreEqual = true;
    final long[] updatedNode = new long[node.length];
    for (int i = 0; i < maxLevelSize / 2; i++) {
      final long position = node[i];
      if (hashMapItemsAreEqual && mapCounter == 0)
        firstPosition = position;

      updatedNode[2 * i] = position;
      updatedNode[2 * i + 1] = position;

      if (hashMapItemsAreEqual) {
        hashMapItemsAreEqual = firstPosition == position;

        mapCounter += 2;

        if (mapCounter >= hashMapSize)
          mapCounter = 0;
      }
    }

    allLeftItemsAreEqual = hashMapItemsAreEqual;

    nodesMetadata[bucketPath.nodeIndex].incrementLocalNodeDepth();
    hashTree[bucketPath.nodeIndex] = updatedNode;

    return new NodeSplitResult(newNode, allLeftItemsAreEqual, allRightItemsAreEqual);
  }

  private long createBucketPointer(long filePosition, int fileLevel) {
    return ((filePosition + 1) << 8) | fileLevel;
  }

  private long getFilePosition(long bucketPointer) {
    return (bucketPointer >>> 8) - 1;
  }

  private int getFileLevel(long bucketPointer) {
    return (int) (bucketPointer & 0xFF);
  }

  private void deleteNode(int nodeIndex) {
    if (nodeIndex == hashTreeSize - 1) {
      hashTree[nodeIndex] = null;
      nodesMetadata[nodeIndex] = null;
      hashTreeSize--;
      return;
    }

    if (hashTreeTombstone > -1) {
      final long[] tombstone = new long[] { hashTreeTombstone };
      hashTree[nodeIndex] = tombstone;
      hashTreeTombstone = nodeIndex;
    } else {
      hashTree[nodeIndex] = null;
      hashTreeTombstone = nodeIndex;
    }

    nodesMetadata[nodeIndex] = null;
  }

  @Override
  public OPhysicalPosition getPhysicalPosition(OPhysicalPosition iPPosition) throws IOException {
    acquireSharedLock();
    try {
      BucketPath bucketPath = getBucket(iPPosition.clusterPosition);
      final long bucketPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];
      if (bucketPointer == 0)
        return null;

      final int fileLevel = getFileLevel(bucketPointer);
      final long filePosition = getFilePosition(bucketPointer);

      final OEHBucket bucket = readBucket(fileLevel, filePosition);

      return bucket.find(iPPosition.clusterPosition);
    } finally {
      releaseSharedLock();
    }

  }

  @Override
  public void updateDataSegmentPosition(OClusterPosition iPosition, int iDataSegmentId, long iDataPosition) throws IOException {
    acquireExclusiveLock();
    try {
      BucketPath bucketPath = getBucket(iPosition);
      final long bucketPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];

      final int fileLevel = getFileLevel(bucketPointer);
      final long filePosition = getFilePosition(bucketPointer);

      final OEHBucket bucket = readBucket(fileLevel, filePosition);
      final int index = bucket.getIndex(iPosition);
      if (index < 0)
        throw new IllegalStateException("Position " + iPosition + " is absent");

      final OPair<Long, OEHBucket> trackedBucket = bucketToTrack.get();
      if (trackedBucket != null && trackedBucket.getKey() == bucketPointer)
        trackedBucket.getValue().updateDataSegmentPosition(index, iDataSegmentId, iDataPosition);

      bucket.updateDataSegmentPosition(index, iDataSegmentId, iDataPosition);
      bucket.toStream();

      assert bucket.getEntry(index).dataSegmentId == iDataSegmentId;
      assert bucket.getEntry(index).dataSegmentPos == iDataPosition;

      saveBucket(fileLevel, filePosition, bucket);
    } finally {
      releaseExclusiveLock();
    }
  }

  @Override
  public void removePhysicalPosition(OClusterPosition iPosition) throws IOException {
    acquireExclusiveLock();
    try {
      final BucketPath nodePath = getBucket(iPosition);
      final long bucketPointer = hashTree[nodePath.nodeIndex][nodePath.itemIndex + nodePath.hashMapOffset];

      final int fileLevel = getFileLevel(bucketPointer);
      final long filePosition = getFilePosition(bucketPointer);

      final OEHBucket bucket = readBucket(fileLevel, filePosition);

      final int positionIndex = bucket.getIndex(iPosition);
      if (positionIndex < 0)
        return;

      bucket.deleteEntry(positionIndex);
      size--;

      if (!mergeBucketsAfterDeletion(nodePath, bucket))
        saveBucket(fileLevel, filePosition, bucket);

      if (nodePath.parent != null) {
        final int hashMapSize = 1 << nodePath.nodeLocalDepth;

        final long[] node = hashTree[nodePath.nodeIndex];
        final boolean allMapsContainSameBucket = checkAllMapsContainSameBucket(node, hashMapSize);
        if (allMapsContainSameBucket)
          mergeNodeToParent(node, nodePath);
      }
    } finally {
      releaseExclusiveLock();
    }
  }

  @Override
  public void updateRecordType(OClusterPosition iPosition, byte iRecordType) throws IOException {
    acquireExclusiveLock();
    try {
      BucketPath bucketPath = getBucket(iPosition);
      final long bucketPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];

      final int fileLevel = getFileLevel(bucketPointer);
      final long filePosition = getFilePosition(bucketPointer);

      final OEHBucket bucket = readBucket(fileLevel, filePosition);
      final int index = bucket.getIndex(iPosition);
      if (index < 0)
        throw new IllegalStateException("Position " + iPosition + " is absent");

      bucket.updateRecordType(index, iRecordType);
      bucket.toStream();

      assert bucket.getEntry(index).recordType == iRecordType;

      saveBucket(fileLevel, filePosition, bucket);
    } finally {
      releaseExclusiveLock();
    }
  }

  @Override
  public void updateVersion(OClusterPosition iPosition, ORecordVersion iVersion) throws IOException {
    acquireExclusiveLock();
    try {
      BucketPath bucketPath = getBucket(iPosition);
      final long bucketPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];

      final int fileLevel = getFileLevel(bucketPointer);
      final long filePosition = getFilePosition(bucketPointer);

      final OEHBucket bucket = readBucket(fileLevel, filePosition);
      final int index = bucket.getIndex(iPosition);
      if (index < 0)
        throw new IllegalStateException("Position " + iPosition + " is absent");

      bucket.updateVersion(index, iVersion);
      bucket.toStream();

      assert bucket.getEntry(index).recordVersion.equals(iVersion);

      saveBucket(fileLevel, filePosition, bucket);
    } finally {
      releaseExclusiveLock();
    }
  }

  @Override
  public long getEntries() {
    acquireSharedLock();
    try {
      return size;
    } finally {
      releaseSharedLock();
    }

  }

  @Override
  public OClusterPosition getFirstPosition() throws IOException {
    acquireSharedLock();
    try {
      BucketPath bucketPath = getBucket(zeroKey);
      long bucketPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];

      int fileLevel = getFileLevel(bucketPointer);
      long filePosition = getFilePosition(bucketPointer);

      OEHBucket bucket = readBucket(fileLevel, filePosition);
      while (bucket.size() == 0) {
        bucketPath = nextBucketToFind(bucketPath, bucket.getDepth());
        if (bucketPath == null)
          return clusterPositionFactory.valueOf(-1);

        final long nextPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];

        fileLevel = getFileLevel(nextPointer);
        filePosition = getFilePosition(nextPointer);

        bucket = readBucket(fileLevel, filePosition);
      }

      return bucket.getKey(0);
    } finally {
      releaseSharedLock();
    }
  }

  @Override
  public OClusterPosition getLastPosition() throws IOException {
    acquireSharedLock();
    try {
      BucketPath bucketPath = getBucket(clusterPositionFactory.getMaxValue());
      long bucketPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];

      int fileLevel = getFileLevel(bucketPointer);
      long filePosition = getFilePosition(bucketPointer);

      OEHBucket bucket = readBucket(fileLevel, filePosition);
      while (bucket.size() == 0) {
        final BucketPath prevBucketPath = prevBucketToFind(bucketPath, bucket.getDepth());
        if (prevBucketPath == null)
          return clusterPositionFactory.valueOf(-1);

        final long prevPointer = hashTree[prevBucketPath.nodeIndex][prevBucketPath.itemIndex + prevBucketPath.hashMapOffset];

        fileLevel = getFileLevel(prevPointer);
        filePosition = getFilePosition(prevPointer);

        bucket = readBucket(fileLevel, filePosition);

        bucketPath = prevBucketPath;
      }

      return bucket.getKey(bucket.size() - 1);
    } finally {
      releaseSharedLock();
    }
  }

  @Override
  public String toString() {
    return name + " (id=" + id + ")";
  }

  @Override
  public void lock() {
    acquireSharedLock();
  }

  @Override
  public void unlock() {
    releaseSharedLock();
  }

  @Override
  public int getId() {
    acquireSharedLock();
    try {
      return id;
    } finally {
      releaseSharedLock();
    }

  }

  @Override
  public void synch() throws IOException {
    acquireExclusiveLock();
    try {
      storeMetadata();
      storeHashTree();

      for (OEHFileMetadata metadata : filesMetadata)
        if (metadata != null)
          metadata.getFile().synch();

      metadataStore.synch();
      treeStateStore.synch();
    } finally {
      releaseExclusiveLock();
    }
  }

  @Override
  public void setSoftlyClosed(boolean softlyClosed) throws IOException {
    acquireExclusiveLock();
    try {
      for (OEHFileMetadata metadata : filesMetadata)
        metadata.getFile().setSoftlyClosed(softlyClosed);

      metadataStore.setSoftlyClosed(softlyClosed);
      treeStateStore.setSoftlyClosed(softlyClosed);
    } finally {
      releaseExclusiveLock();
    }
  }

  @Override
  public String getName() {
    acquireSharedLock();
    try {
      return name;
    } finally {
      releaseSharedLock();
    }

  }

  @Override
  public long getRecordsSize() throws IOException {
    acquireSharedLock();
    try {
      long size = 0;
      for (OEHFileMetadata fileMetadata : filesMetadata)
        if (fileMetadata != null)
          size += fileMetadata.getFile().getFilledUpTo();

      BucketPath bucketPath = getBucket(zeroKey);
      while (bucketPath != null) {
        OEHBucket bucket = readBucket(bucketPath);
        for (OPhysicalPosition position : bucket) {
          if (position.dataSegmentPos > -1 && !position.recordVersion.isTombstone())
            size += storage.getDataSegmentById(position.dataSegmentId).getRecordSize(position.dataSegmentPos);
        }

        bucketPath = nextBucketToFind(bucketPath, bucket.getDepth());
      }

      return size;
    } finally {
      releaseSharedLock();
    }
  }

  @Override
  public boolean isHashBased() {
    return true;
  }

  @Override
  public OClusterEntryIterator absoluteIterator() {
    acquireSharedLock();
    try {
      return new OClusterEntryIterator(this);
    } finally {
      releaseSharedLock();
    }

  }

  @Override
  public OPhysicalPosition[] higherPositions(OPhysicalPosition position) throws IOException {
    acquireSharedLock();
    try {
      BucketPath bucketPath = getBucket(position.clusterPosition);
      long bucketPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];

      int fileLevel = getFileLevel(bucketPointer);
      long filePosition = getFilePosition(bucketPointer);

      OEHBucket bucket = readBucket(fileLevel, filePosition);
      while (bucket.size() == 0 || bucket.getKey(bucket.size() - 1).compareTo(position.clusterPosition) <= 0) {
        bucketPath = nextBucketToFind(bucketPath, bucket.getDepth());
        if (bucketPath == null)
          return new OPhysicalPosition[0];

        final long nextPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];

        fileLevel = getFileLevel(nextPointer);
        filePosition = getFilePosition(nextPointer);

        bucket = readBucket(fileLevel, filePosition);
      }

      final int index = bucket.getIndex(position.clusterPosition);
      final int startIndex;
      if (index >= 0)
        startIndex = index + 1;
      else
        startIndex = -index - 1;

      final int endIndex = bucket.size();
      return convertBucketToPositions(bucket, startIndex, endIndex);
    } finally {
      releaseSharedLock();
    }
  }

  private OPhysicalPosition[] convertBucketToPositions(final OEHBucket bucket, int startIndex, int endIndex) {
    final OPhysicalPosition[] entries = new OPhysicalPosition[endIndex - startIndex];
    final Iterator<OPhysicalPosition> iterator = bucket.iterator(startIndex);
    for (int i = 0, k = startIndex; k < endIndex; i++, k++)
      entries[i] = iterator.next();

    return entries;
  }

  @Override
  public OPhysicalPosition[] ceilingPositions(OPhysicalPosition position) throws IOException {
    acquireSharedLock();
    try {
      BucketPath bucketPath = getBucket(position.clusterPosition);
      long bucketPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];

      int fileLevel = getFileLevel(bucketPointer);
      long filePosition = getFilePosition(bucketPointer);

      OEHBucket bucket = readBucket(fileLevel, filePosition);
      while (bucket.size() == 0) {
        bucketPath = nextBucketToFind(bucketPath, bucket.getDepth());
        if (bucketPath == null)
          return new OPhysicalPosition[0];

        final long nextPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];

        fileLevel = getFileLevel(nextPointer);
        filePosition = getFilePosition(nextPointer);

        bucket = readBucket(fileLevel, filePosition);
      }

      final int index = bucket.getIndex(position.clusterPosition);
      final int startIndex;
      if (index >= 0)
        startIndex = index;
      else
        startIndex = -index - 1;

      final int endIndex = bucket.size();
      return convertBucketToPositions(bucket, startIndex, endIndex);
    } finally {
      releaseSharedLock();
    }
  }

  @Override
  public OPhysicalPosition[] lowerPositions(OPhysicalPosition position) throws IOException {
    acquireSharedLock();
    try {
      BucketPath bucketPath = getBucket(position.clusterPosition);
      long bucketPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];

      int fileLevel = getFileLevel(bucketPointer);
      long filePosition = getFilePosition(bucketPointer);

      OEHBucket bucket = readBucket(fileLevel, filePosition);
      while (bucket.size() == 0 || bucket.getKey(0).compareTo(position.clusterPosition) >= 0) {
        final BucketPath prevBucketPath = prevBucketToFind(bucketPath, bucket.getDepth());
        if (prevBucketPath == null)
          return new OPhysicalPosition[0];

        final long prevPointer = hashTree[prevBucketPath.nodeIndex][prevBucketPath.itemIndex + prevBucketPath.hashMapOffset];

        fileLevel = getFileLevel(prevPointer);
        filePosition = getFilePosition(prevPointer);

        bucket = readBucket(fileLevel, filePosition);

        bucketPath = prevBucketPath;
      }

      final int startIndex = 0;
      final int index = bucket.getIndex(position.clusterPosition);

      final int endIndex;
      if (index >= 0)
        endIndex = index;
      else
        endIndex = -index - 1;

      return convertBucketToPositions(bucket, startIndex, endIndex);
    } finally {
      releaseSharedLock();
    }
  }

  @Override
  public OPhysicalPosition[] floorPositions(OPhysicalPosition position) throws IOException {
    acquireSharedLock();
    try {
      BucketPath bucketPath = getBucket(position.clusterPosition);
      long bucketPointer = hashTree[bucketPath.nodeIndex][bucketPath.itemIndex + bucketPath.hashMapOffset];

      int fileLevel = getFileLevel(bucketPointer);
      long filePosition = getFilePosition(bucketPointer);

      OEHBucket bucket = readBucket(fileLevel, filePosition);
      while (bucket.size() == 0) {
        final BucketPath prevBucketPath = prevBucketToFind(bucketPath, bucket.getDepth());
        if (prevBucketPath == null)
          return new OPhysicalPosition[0];

        final long prevPointer = hashTree[prevBucketPath.nodeIndex][prevBucketPath.itemIndex + prevBucketPath.hashMapOffset];

        fileLevel = getFileLevel(prevPointer);
        filePosition = getFilePosition(prevPointer);

        bucket = readBucket(fileLevel, filePosition);

        bucketPath = prevBucketPath;
      }

      final int startIndex = 0;
      final int index = bucket.getIndex(position.clusterPosition);

      final int endIndex;
      if (index >= 0)
        endIndex = index + 1;
      else
        endIndex = -index - 1;

      return convertBucketToPositions(bucket, startIndex, endIndex);
    } finally {
      releaseSharedLock();
    }
  }

  private BucketPath prevBucketToFind(final BucketPath bucketPath, int bucketDepth) {
    int offset = bucketPath.nodeGlobalDepth - bucketDepth;

    BucketPath currentBucket = bucketPath;
    int nodeLocalDepth = bucketPath.nodeLocalDepth;
    while (offset > 0) {
      offset -= nodeLocalDepth;
      if (offset > 0) {
        currentBucket = bucketPath.parent;
        nodeLocalDepth = currentBucket.nodeLocalDepth;
      }
    }

    final int diff = bucketDepth - (currentBucket.nodeGlobalDepth - nodeLocalDepth);
    final int firstStartIndex = currentBucket.itemIndex & ((levelMask << (nodeLocalDepth - diff)) & levelMask);
    final int globalIndex = firstStartIndex + currentBucket.hashMapOffset - 1;

    final BucketPath bucketPathToFind;
    if (globalIndex < 0)
      bucketPathToFind = prevLevelUp(bucketPath);
    else {
      final int hashMapSize = 1 << currentBucket.nodeLocalDepth;
      final int hashMapOffset = globalIndex / hashMapSize * hashMapSize;

      final int startIndex = globalIndex - hashMapOffset;

      bucketPathToFind = new BucketPath(currentBucket.parent, hashMapOffset, startIndex, currentBucket.nodeIndex,
          currentBucket.nodeLocalDepth, currentBucket.nodeGlobalDepth);
    }

    return prevNonEmptyNode(bucketPathToFind);
  }

  private BucketPath prevNonEmptyNode(BucketPath nodePath) {
    prevBucketLoop: while (nodePath != null) {
      final long[] node = hashTree[nodePath.nodeIndex];
      final int startIndex = 0;
      final int endIndex = nodePath.itemIndex + nodePath.hashMapOffset;

      for (int i = endIndex; i >= startIndex; i--) {
        final long position = node[i];
        if (position > 0) {
          final int hashMapSize = 1 << nodePath.nodeLocalDepth;
          final int hashMapOffset = (i / hashMapSize) * hashMapSize;
          final int itemIndex = i - hashMapOffset;

          return new BucketPath(nodePath.parent, hashMapOffset, itemIndex, nodePath.nodeIndex, nodePath.nodeLocalDepth,
              nodePath.nodeGlobalDepth);
        }

        if (position < 0) {
          final int childNodeIndex = (int) ((position & Long.MAX_VALUE) >> 8);
          final int childItemOffset = (int) position & 0xFF;
          final int nodeLocalDepth = nodesMetadata[childNodeIndex].getNodeLocalDepth();
          final int endChildIndex = (1 << nodeLocalDepth) - 1;

          final BucketPath parent = new BucketPath(nodePath.parent, 0, i, nodePath.nodeIndex, nodePath.nodeLocalDepth,
              nodePath.nodeGlobalDepth);
          nodePath = new BucketPath(parent, childItemOffset, endChildIndex, childNodeIndex, nodeLocalDepth, parent.nodeGlobalDepth
              + nodeLocalDepth);
          continue prevBucketLoop;
        }
      }

      nodePath = prevLevelUp(nodePath);
    }

    return null;
  }

  private BucketPath prevLevelUp(BucketPath bucketPath) {
    if (bucketPath.parent == null)
      return null;

    final int nodeLocalDepth = bucketPath.nodeLocalDepth;
    final int pointersSize = 1 << (maxLevelDepth - nodeLocalDepth);

    final BucketPath parent = bucketPath.parent;

    if (parent.itemIndex > maxLevelSize / 2) {
      final int prevParentIndex = ((parent.itemIndex - maxLevelSize / 2) / pointersSize) * pointersSize + maxLevelSize / 2 - 1;
      return new BucketPath(parent.parent, 0, prevParentIndex, parent.nodeIndex, parent.nodeLocalDepth, parent.nodeGlobalDepth);
    }

    final int prevParentIndex = (parent.itemIndex / pointersSize) * pointersSize - 1;
    if (prevParentIndex >= 0)
      return new BucketPath(parent.parent, 0, prevParentIndex, parent.nodeIndex, parent.nodeLocalDepth, parent.nodeGlobalDepth);

    return prevLevelUp(new BucketPath(parent.parent, 0, 0, parent.nodeIndex, parent.nodeLocalDepth, -1));
  }

  private static final class BucketPath {
    private final BucketPath parent;
    private final int        hashMapOffset;
    private final int        itemIndex;
    private final int        nodeIndex;
    private final int        nodeGlobalDepth;
    private final int        nodeLocalDepth;

    private BucketPath(BucketPath parent, int hashMapOffset, int itemIndex, int nodeIndex, int nodeLocalDepth, int nodeGlobalDepth) {
      this.parent = parent;
      this.hashMapOffset = hashMapOffset;
      this.itemIndex = itemIndex;
      this.nodeIndex = nodeIndex;
      this.nodeGlobalDepth = nodeGlobalDepth;
      this.nodeLocalDepth = nodeLocalDepth;
    }
  }

  private static final class BucketSplitResult {
    private final long updatedBucketPointer;
    private final long newBucketPointer;
    private final int  newDepth;

    private BucketSplitResult(long updatedBucketPointer, long newBucketPointer, int newDepth) {
      this.updatedBucketPointer = updatedBucketPointer;
      this.newBucketPointer = newBucketPointer;
      this.newDepth = newDepth;
    }
  }

  private static final class NodeSplitResult {
    private final long[]  newNode;
    private final boolean allLeftHashMapsEqual;
    private final boolean allRightHashMapsEqual;

    private NodeSplitResult(long[] newNode, boolean allLeftHashMapsEqual, boolean allRightHashMapsEqual) {
      this.newNode = newNode;
      this.allLeftHashMapsEqual = allLeftHashMapsEqual;
      this.allRightHashMapsEqual = allRightHashMapsEqual;
    }
  }
}
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
package com.orientechnologies.orient.core.index.hashindex.local.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import com.orientechnologies.common.directmemory.ODirectMemory;
import com.orientechnologies.common.serialization.types.OIntegerSerializer;
import com.orientechnologies.common.serialization.types.OLongSerializer;
import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.config.OStorageSegmentConfiguration;
import com.orientechnologies.orient.core.storage.impl.local.OMultiFileSegment;
import com.orientechnologies.orient.core.storage.impl.local.OStorageLocalAbstract;

/**
 * @author Artem Loginov
 * @since 14.03.13
 */
public class O2QCache implements ODiskCache {
  public static final long                   MAGIC_NUMBER   = 0xFACB03FEL;

  private static final CRC32                 CRC_CALCULATOR = new CRC32();

  private final int                          maxSize;
  private final int                          K_IN;
  private final int                          K_OUT;

  private final int                          pageSize;

  private LRUList                            am;
  private LRUList                            a1out;
  private LRUList                            a1in;

  private final ODirectMemory                directMemory;

  private final Map<Long, OMultiFileSegment> files;
  private final Map<FileLockKey, Long>       evictedPages;
  private final Map<Long, Set<Long>>         filesPages;

  private final Object                       syncObject;
  private final OStorageLocalAbstract        storageLocal;

  private final boolean                      syncOnPageFlush;
  private long                               fileCounter    = 1;

  public O2QCache(long maxMemory, ODirectMemory directMemory, int pageSize, OStorageLocalAbstract storageLocal,
      boolean syncOnPageFlush) {
    this.directMemory = directMemory;
    this.pageSize = pageSize;
    this.storageLocal = storageLocal;
    this.syncOnPageFlush = syncOnPageFlush;
    this.files = new HashMap<Long, OMultiFileSegment>();
    this.filesPages = new HashMap<Long, Set<Long>>();

    this.evictedPages = new HashMap<FileLockKey, Long>();

    long tmpMaxSize = maxMemory / pageSize;
    if (tmpMaxSize >= Integer.MAX_VALUE) {
      maxSize = Integer.MAX_VALUE;
    } else {
      maxSize = (int) tmpMaxSize;
    }

    K_IN = maxSize >> 2;
    K_OUT = maxSize >> 1;

    am = new LRUList();
    a1out = new LRUList();
    a1in = new LRUList();

    syncObject = new Object();
  }

  LRUList getAm() {
    return am;
  }

  LRUList getA1out() {
    return a1out;
  }

  LRUList getA1in() {
    return a1in;
  }

  Map<FileLockKey, Long> getEvictedPages() {
    return evictedPages;
  }

  @Override
  public long openFile(OStorageSegmentConfiguration fileConfiguration, String fileExtension) throws IOException {
    synchronized (syncObject) {
      long fileId = fileCounter++;

      final OMultiFileSegment multiFileSegment = new OMultiFileSegment(storageLocal, fileConfiguration, fileExtension, pageSize);
      if (multiFileSegment.getFile(0).exists())
        multiFileSegment.open();
      else
        multiFileSegment.create(pageSize);

      files.put(fileId, multiFileSegment);

      filesPages.put(fileId, new HashSet<Long>());

      return fileId;
    }
  }

  @Override
  public void markDirty(long fileId, long pageIndex) {
    synchronized (syncObject) {
      LRUEntry lruEntry = a1in.get(fileId, pageIndex);

      if (lruEntry != null) {
        lruEntry.isDirty = true;
        return;
      }

      lruEntry = am.get(fileId, pageIndex);
      if (lruEntry != null)
        lruEntry.isDirty = true;
      else
        throw new IllegalStateException("Requested page number " + pageIndex + " for file " + files.get(fileId).getName()
            + " is not in cache");
    }
  }

  @Override
  public long load(long fileId, long pageIndex) throws IOException {
    synchronized (syncObject) {
      final LRUEntry lruEntry = updateCache(fileId, pageIndex);
      lruEntry.usageCounter++;
      return lruEntry.dataPointer;
    }
  }

  @Override
  public void release(long fileId, long pageIndex) {
    synchronized (syncObject) {
      LRUEntry lruEntry = get(fileId, pageIndex);
      if (lruEntry != null)
        lruEntry.usageCounter--;
      else
        throw new IllegalStateException("record should be released is already free!");
    }
  }

  @Override
  public long getFilledUpTo(long fileId) throws IOException {
    synchronized (syncObject) {
      return files.get(fileId).getFilledUpTo() / pageSize;
    }
  }

  @Override
  public void flushFile(long fileId) throws IOException {
    synchronized (syncObject) {
      final Set<Long> pageIndexes = filesPages.get(fileId);

      Long[] sortedPageIndexes = new Long[pageIndexes.size()];
      sortedPageIndexes = pageIndexes.toArray(sortedPageIndexes);
      Arrays.sort(sortedPageIndexes);

      for (Long pageIndex : sortedPageIndexes) {
        LRUEntry lruEntry = get(fileId, pageIndex);
        if (lruEntry != null) {
          if (lruEntry.isDirty && lruEntry.usageCounter == 0) {
            flushData(fileId, lruEntry.pageIndex, lruEntry.dataPointer);
            lruEntry.isDirty = false;
          }
        } else {
          final Long dataPointer = evictedPages.remove(new FileLockKey(fileId, pageIndex));
          if (dataPointer != null)
            flushData(fileId, pageIndex, dataPointer);
        }
      }

      files.get(fileId).synch();
    }
  }

  @Override
  public void closeFile(long fileId) throws IOException {
    synchronized (syncObject) {
      if (!files.containsKey(fileId))
        return;

      final Set<Long> pageIndexes = filesPages.get(fileId);
      Long[] sortedPageIndexes = new Long[pageIndexes.size()];
      sortedPageIndexes = pageIndexes.toArray(sortedPageIndexes);
      Arrays.sort(sortedPageIndexes);

      for (Long pageIndex : sortedPageIndexes) {
        LRUEntry lruEntry = get(fileId, pageIndex);
        if (lruEntry != null) {
          if (lruEntry.usageCounter == 0) {
            lruEntry = remove(fileId, pageIndex);
            flushData(fileId, pageIndex, lruEntry.dataPointer);
            directMemory.free(lruEntry.dataPointer);
          }
        } else {
          Long dataPointer = evictedPages.remove(new FileLockKey(fileId, pageIndex));
          if (dataPointer != null)
            flushData(fileId, pageIndex, dataPointer);
        }

      }

      pageIndexes.clear();

      files.get(fileId).close();
    }
  }

  @Override
  public void deleteFile(long fileId) throws IOException {
    synchronized (syncObject) {
      if (!files.containsKey(fileId))
        return;

      truncateFile(fileId);
      files.get(fileId).delete();

      files.remove(fileId);
      filesPages.remove(fileId);
    }

  }

  @Override
  public void truncateFile(long fileId) throws IOException {
    synchronized (syncObject) {
      if (!files.containsKey(fileId))
        return;

      final Set<Long> pageEntries = filesPages.get(fileId);
      for (Long pageIndex : pageEntries) {
        LRUEntry lruEntry = get(fileId, pageIndex);
        if (lruEntry != null) {
          if (lruEntry.usageCounter == 0) {
            lruEntry = remove(fileId, pageIndex);
            if (lruEntry.dataPointer != ODirectMemory.NULL_POINTER)
              directMemory.free(lruEntry.dataPointer);
          }
        } else {
          Long dataPointer = evictedPages.remove(new FileLockKey(fileId, pageIndex));
          if (dataPointer != null)
            directMemory.free(dataPointer);
        }
      }

      pageEntries.clear();
      files.get(fileId).truncate();
    }
  }

  @Override
  public void renameFile(long fileId, String oldFileName, String newFileName) throws IOException {
    synchronized (syncObject) {
      if (!files.containsKey(fileId))
        return;

      files.get(fileId).rename(oldFileName, newFileName);
    }

  }

  @Override
  public void flushBuffer() throws IOException {
    synchronized (syncObject) {
      for (LRUEntry entry : am) {
        if (entry.isDirty && entry.usageCounter == 0) {
          flushData(entry.fileId, entry.pageIndex, entry.dataPointer);
          entry.isDirty = false;
        }
      }

      for (LRUEntry entry : a1in) {
        if (entry.isDirty && entry.usageCounter == 0) {
          flushData(entry.fileId, entry.pageIndex, entry.dataPointer);
          entry.isDirty = false;
        }
      }

      flushEvictedPages();

      for (OMultiFileSegment multiFileSegment : files.values())
        multiFileSegment.synch();
    }
  }

  @Override
  public void clear() throws IOException {
    synchronized (syncObject) {
      flushBuffer();

      am.clear();
      a1in.clear();
      a1out.clear();
      for (Set<Long> fileEntries : filesPages.values())
        fileEntries.clear();
    }
  }

  @Override
  public void close() throws IOException {
    synchronized (syncObject) {
      clear();
      for (OMultiFileSegment multiFileSegment : files.values()) {
        multiFileSegment.synch();
        multiFileSegment.close();
      }
    }

  }

  @Override
  public boolean wasSoftlyClosed(long fileId) throws IOException {
    synchronized (syncObject) {
      OMultiFileSegment multiFileSegment = files.get(fileId);
      if (multiFileSegment == null)
        return false;

      return multiFileSegment.wasSoftlyClosedAtPreviousTime();
    }

  }

  @Override
  public void setSoftlyClosed(long fileId, boolean softlyClosed) throws IOException {
    synchronized (syncObject) {
      OMultiFileSegment multiFileSegment = files.get(fileId);
      if (multiFileSegment != null)
        multiFileSegment.setSoftlyClosed(softlyClosed);
    }
  }

  private LRUEntry updateCache(long fileId, long pageIndex) throws IOException {
    LRUEntry lruEntry = am.get(fileId, pageIndex);
    if (lruEntry != null) {
      lruEntry = am.putToMRU(fileId, pageIndex, lruEntry.dataPointer, lruEntry.isDirty);

      return lruEntry;
    }

    lruEntry = a1out.get(fileId, pageIndex);
    if (lruEntry != null) {
      removeColdestPageIfNeeded();

      CacheResult cacheResult = cacheFileContent(fileId, pageIndex);
      lruEntry.dataPointer = cacheResult.dataPointer;
      lruEntry.isDirty = cacheResult.isDirty;

      lruEntry = am.putToMRU(fileId, pageIndex, lruEntry.dataPointer, lruEntry.isDirty);
      return lruEntry;
    }

    lruEntry = a1in.get(fileId, pageIndex);
    if (lruEntry != null)
      return lruEntry;

    removeColdestPageIfNeeded();

    CacheResult cacheResult = cacheFileContent(fileId, pageIndex);
    lruEntry = a1in.putToMRU(fileId, pageIndex, cacheResult.dataPointer, cacheResult.isDirty);

    filesPages.get(fileId).add(pageIndex);

    return lruEntry;
  }

  private void removeColdestPageIfNeeded() throws IOException {
    if (am.size() + a1in.size() >= maxSize) {
      if (a1in.size() > K_IN) {
        LRUEntry removedFromAInEntry = a1in.removeLRU();
        assert removedFromAInEntry.usageCounter == 0;
        evictFileContent(removedFromAInEntry.fileId, removedFromAInEntry.pageIndex, removedFromAInEntry.dataPointer,
            removedFromAInEntry.isDirty);
        assert removedFromAInEntry.usageCounter == 0;
        a1out.putToMRU(removedFromAInEntry.fileId, removedFromAInEntry.pageIndex, ODirectMemory.NULL_POINTER, false);
        if (a1out.size() > K_OUT) {
          LRUEntry removedEntry = a1out.removeLRU();
          assert removedEntry.usageCounter == 0;
          Set<Long> pageEntries = filesPages.get(removedEntry.fileId);
          pageEntries.remove(removedEntry.pageIndex);
        }
      } else {
        LRUEntry removedEntry = am.removeLRU();
        assert removedEntry.usageCounter == 0;
        evictFileContent(removedEntry.fileId, removedEntry.pageIndex, removedEntry.dataPointer, removedEntry.isDirty);
        Set<Long> pageEntries = filesPages.get(removedEntry.fileId);
        pageEntries.remove(removedEntry.pageIndex);
      }
    }
  }

  private CacheResult cacheFileContent(long fileId, long pageIndex) throws IOException {
    FileLockKey key = new FileLockKey(fileId, pageIndex);
    if (evictedPages.containsKey(key))
      return new CacheResult(true, evictedPages.remove(key));

    final OMultiFileSegment multiFileSegment = files.get(fileId);
    final long startPosition = pageIndex * pageSize;
    final long endPosition = startPosition + pageSize;

    byte[] content = new byte[pageSize];
    if (multiFileSegment.getFilledUpTo() >= endPosition) {
      multiFileSegment.readContinuously(startPosition, content, content.length);
    } else {
      multiFileSegment.allocateSpaceContinuously((int) (endPosition - multiFileSegment.getFilledUpTo()));
    }

    long dataPointer;
    dataPointer = directMemory.allocate(content);

    return new CacheResult(false, dataPointer);
  }

  private void evictFileContent(long fileId, long pageIndex, long dataPointer, boolean isDirty) throws IOException {
    if (isDirty) {
      if (evictedPages.size() >= OGlobalConfiguration.DISK_CACHE_WRITE_QUEUE_LENGTH.getValueAsInteger()) {
        flushEvictedPages();

        flushData(fileId, pageIndex, dataPointer);
        directMemory.free(dataPointer);
      } else {
        evictedPages.put(new FileLockKey(fileId, pageIndex), dataPointer);
      }
    } else {
      directMemory.free(dataPointer);
    }

  }

  private void flushData(final long fileId, final long pageIndex, final long dataPointer) throws IOException {
    final byte[] content = directMemory.get(dataPointer, pageSize);
    OLongSerializer.INSTANCE.serializeNative(MAGIC_NUMBER, content, 0);

    final int crc32 = calculatePageCrc(content);
    OIntegerSerializer.INSTANCE.serializeNative(crc32, content, OLongSerializer.LONG_SIZE);

    final OMultiFileSegment multiFileSegment = files.get(fileId);

    multiFileSegment.writeContinuously(pageIndex * pageSize, content);

    if (syncOnPageFlush)
      multiFileSegment.synch();
  }

  @Override
  public OPageDataVerificationError[] checkStoredPages(OCommandOutputListener commandOutputListener) {
    final int notificationTimeOut = 5000;
    final List<OPageDataVerificationError> errors = new ArrayList<OPageDataVerificationError>();

    synchronized (syncObject) {
      for (long fileId : files.keySet()) {

        OMultiFileSegment multiFileSegment = files.get(fileId);

        boolean fileIsCorrect;
        try {

          if (commandOutputListener != null)
            commandOutputListener.onMessage("Flashing file " + multiFileSegment.getName() + "... ");

          flushFile(fileId);

          if (commandOutputListener != null)
            commandOutputListener.onMessage("Start verification of content of " + multiFileSegment.getName() + "file ...");

          long time = System.currentTimeMillis();

          long filledUpTo = multiFileSegment.getFilledUpTo();
          fileIsCorrect = true;

          for (long pos = 0; pos < filledUpTo; pos += pageSize) {
            boolean checkSumIncorrect = false;
            boolean magicNumberIncorrect = false;

            byte[] data = new byte[pageSize];

            multiFileSegment.readContinuously(pos, data, data.length);

            long magicNumber = OLongSerializer.INSTANCE.deserializeNative(data, 0);

            if (magicNumber != MAGIC_NUMBER) {
              magicNumberIncorrect = true;
              if (commandOutputListener != null)
                commandOutputListener.onMessage("Error: Magic number for page " + (pos / pageSize) + " in file "
                    + multiFileSegment.getName() + " does not much !!!");
              fileIsCorrect = false;
            }

            final int storedCRC32 = OIntegerSerializer.INSTANCE.deserializeNative(data, OLongSerializer.LONG_SIZE);

            final int calculatedCRC32 = calculatePageCrc(data);
            if (storedCRC32 != calculatedCRC32) {
              checkSumIncorrect = true;
              if (commandOutputListener != null)
                commandOutputListener.onMessage("Error: Checksum for page " + (pos / pageSize) + " in file "
                    + multiFileSegment.getName() + " is incorrect !!!");
              fileIsCorrect = false;
            }

            if (magicNumberIncorrect || checkSumIncorrect)
              errors.add(new OPageDataVerificationError(magicNumberIncorrect, checkSumIncorrect, pos / pageSize, multiFileSegment
                  .getName()));

            if (commandOutputListener != null && System.currentTimeMillis() - time > notificationTimeOut) {
              time = notificationTimeOut;
              commandOutputListener.onMessage((pos / pageSize) + " pages were processed ...");
            }
          }
        } catch (IOException ioe) {
          if (commandOutputListener != null)
            commandOutputListener.onMessage("Error: Error during processing of file " + multiFileSegment.getName() + ". "
                + ioe.getMessage());

          fileIsCorrect = false;
        }

        if (!fileIsCorrect) {
          if (commandOutputListener != null)
            commandOutputListener.onMessage("Verification of file " + multiFileSegment.getName() + " is finished with errors.");
        } else {
          if (commandOutputListener != null)
            commandOutputListener.onMessage("Verification of file " + multiFileSegment.getName() + " is successfully finished.");
        }
      }

      return errors.toArray(new OPageDataVerificationError[errors.size()]);
    }
  }

  private void flushEvictedPages() throws IOException {
    Map.Entry<FileLockKey, Long>[] sortedPages = evictedPages.entrySet().toArray(new Map.Entry[evictedPages.size()]);
    Arrays.sort(sortedPages, new Comparator<Map.Entry>() {
      @Override
      public int compare(Map.Entry entryOne, Map.Entry entryTwo) {
        FileLockKey fileLockKeyOne = (FileLockKey) entryOne.getKey();
        FileLockKey fileLockKeyTwo = (FileLockKey) entryTwo.getKey();
        return fileLockKeyOne.compareTo(fileLockKeyTwo);
      }
    });

    for (Map.Entry<FileLockKey, Long> entry : sortedPages) {
      long evictedDataPointer = entry.getValue();
      FileLockKey fileLockKey = entry.getKey();

      flushData(fileLockKey.fileId, fileLockKey.pageIndex, evictedDataPointer);

      directMemory.free(evictedDataPointer);
    }

    evictedPages.clear();
  }

  private static class CacheResult {
    private final boolean isDirty;
    private final long    dataPointer;

    private CacheResult(boolean dirty, long dataPointer) {
      isDirty = dirty;
      this.dataPointer = dataPointer;
    }
  }

  private LRUEntry get(long fileId, long pageIndex) {
    LRUEntry lruEntry = am.get(fileId, pageIndex);

    if (lruEntry != null) {
      return lruEntry;
    }

    lruEntry = a1in.get(fileId, pageIndex);
    return lruEntry;
  }

  private LRUEntry remove(long fileId, long pageIndex) {
    LRUEntry lruEntry = am.remove(fileId, pageIndex);
    if (lruEntry != null) {
      if (lruEntry.usageCounter > 1)
        throw new IllegalStateException("Record cannot be removed because it is used!");
      return lruEntry;
    }
    lruEntry = a1out.remove(fileId, pageIndex);
    if (lruEntry != null) {
      return lruEntry;
    }
    lruEntry = a1in.remove(fileId, pageIndex);
    if (lruEntry != null && lruEntry.usageCounter > 1)
      throw new IllegalStateException("Record cannot be removed because it is used!");
    return lruEntry;
  }

  private int calculatePageCrc(byte[] pageData) {
    int systemSize = OLongSerializer.LONG_SIZE + OIntegerSerializer.INT_SIZE;

    CRC_CALCULATOR.reset();
    CRC_CALCULATOR.update(pageData, systemSize, pageData.length - systemSize);

    return (int) CRC_CALCULATOR.getValue();
  }

  private final class FileLockKey implements Comparable<FileLockKey> {
    private final long fileId;
    private final long pageIndex;

    private FileLockKey(long fileId, long pageIndex) {
      this.fileId = fileId;
      this.pageIndex = pageIndex;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      FileLockKey that = (FileLockKey) o;

      if (fileId != that.fileId)
        return false;
      if (pageIndex != that.pageIndex)
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = (int) (fileId ^ (fileId >>> 32));
      result = 31 * result + (int) (pageIndex ^ (pageIndex >>> 32));
      return result;
    }

    @Override
    public int compareTo(FileLockKey otherKey) {
      if (fileId > otherKey.fileId)
        return 1;
      if (fileId < otherKey.fileId)
        return -1;

      if (pageIndex > otherKey.pageIndex)
        return 1;
      if (pageIndex < otherKey.pageIndex)
        return -1;

      return 0;
    }
  }
}
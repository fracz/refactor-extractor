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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

import com.orientechnologies.common.concur.lock.OLockManager;
import com.orientechnologies.common.directmemory.ODirectMemory;
import com.orientechnologies.common.directmemory.ODirectMemoryFactory;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.common.serialization.types.OIntegerSerializer;
import com.orientechnologies.common.serialization.types.OLongSerializer;
import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.exception.OAllCacheEntriesAreUsedException;
import com.orientechnologies.orient.core.memory.OMemoryWatchDog;
import com.orientechnologies.orient.core.storage.fs.OFileClassic;
import com.orientechnologies.orient.core.storage.impl.local.OStorageLocalAbstract;
import com.orientechnologies.orient.core.storage.impl.local.paginated.OLocalPage;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.OLogSequenceNumber;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.OWriteAheadLog;

/**
 * @author Andrey Lomakin
 * @since 7/23/13
 */
public class OWOWCache {
  public static final int                                   MIN_CACHE_SIZE = 16;

  public static final long                                  MAGIC_NUMBER   = 0xFACB03FEL;

  private final ConcurrentSkipListMap<GroupKey, WriteGroup> writeGroups    = new ConcurrentSkipListMap<GroupKey, WriteGroup>();

  private final Map<Long, OFileClassic>                     files;

  private final OLockManager<PageKey, Thread>               pageFlushLockManger;

  private final ODirectMemory                               directMemory   = ODirectMemoryFactory.INSTANCE.directMemory();

  private final boolean                                     syncOnPageFlush;
  private final int                                         pageSize;
  private final long                                        groupTTL;
  private final OWriteAheadLog                              writeAheadLog;

  private final AtomicInteger                               cacheSize      = new AtomicInteger();
  private final OLockManager<GroupKey, Thread>              lockManager    = new OLockManager<GroupKey, Thread>(true, 60000);

  private volatile int                                      cacheMaxSize;

  private final OStorageLocalAbstract                       storageLocal;

  private final Object                                      syncObject     = new Object();
  private long                                              fileCounter    = 1;

  private GroupKey                                          lastGroupKey   = new GroupKey(0, -1);

  private final ScheduledExecutorService                    commitExecutor = Executors
                                                                               .newSingleThreadScheduledExecutor(new ThreadFactory() {
                                                                                 @Override
                                                                                 public Thread newThread(Runnable r) {
                                                                                   Thread thread = new Thread(r);
                                                                                   thread.setDaemon(true);
                                                                                   thread.setName("Write Cache Flush Task");
                                                                                   return thread;
                                                                                 }
                                                                               });

  public OWOWCache(boolean syncOnPageFlush, int pageSize, long groupTTL, OWriteAheadLog writeAheadLog, long pageFlushInterval,
      int cacheMaxSize, OStorageLocalAbstract storageLocal, boolean checkMinSize) {
    this.files = new HashMap<Long, OFileClassic>();
    this.syncOnPageFlush = syncOnPageFlush;
    this.pageSize = pageSize;
    this.groupTTL = groupTTL;
    this.writeAheadLog = writeAheadLog;
    this.cacheMaxSize = cacheMaxSize;
    this.storageLocal = storageLocal;
    this.pageFlushLockManger = new OLockManager<PageKey, Thread>(true,
        OGlobalConfiguration.DISK_WRITE_CACHE_FLUSH_LOCK_TIMEOUT.getValueAsInteger());

    if (checkMinSize && this.cacheMaxSize < MIN_CACHE_SIZE)
      this.cacheMaxSize = MIN_CACHE_SIZE;

    if (pageFlushInterval > 0)
      commitExecutor.scheduleWithFixedDelay(new PeriodicFlushTask(), pageFlushInterval, pageFlushInterval, TimeUnit.MILLISECONDS);
  }

  public long openFile(String fileName) throws IOException {
    synchronized (syncObject) {
      long fileId = fileCounter++;

      OFileClassic fileClassic = new OFileClassic();
      String path = storageLocal.getVariableParser().resolveVariables(storageLocal.getStoragePath() + File.separator + fileName);
      fileClassic.init(path, storageLocal.getMode());

      if (fileClassic.exists())
        fileClassic.open();
      else
        fileClassic.create(-1);

      files.put(fileId, fileClassic);

      return fileId;
    }
  }

  public void put(final long fileId, final long pageIndex, final OCachePointer dataPointer) {
    synchronized (syncObject) {
      final GroupKey groupKey = new GroupKey(fileId, pageIndex >>> 4);
      lockManager.acquireLock(Thread.currentThread(), groupKey, OLockManager.LOCK.EXCLUSIVE);
      try {
        WriteGroup writeGroup = writeGroups.get(groupKey);
        if (writeGroup == null) {
          writeGroup = new WriteGroup(System.currentTimeMillis());
          writeGroups.put(groupKey, writeGroup);
        }

        int entryIndex = (int) (pageIndex & 15);

        if (writeGroup.pages[entryIndex] == null) {
          dataPointer.incrementReferrer();
          writeGroup.pages[entryIndex] = dataPointer;

          cacheSize.incrementAndGet();
        } else {
          if (!writeGroup.pages[entryIndex].equals(dataPointer)) {
            writeGroup.pages[entryIndex].decrementReferrer();
            dataPointer.incrementReferrer();

            writeGroup.pages[entryIndex] = dataPointer;
          }
        }

        writeGroup.recencyBit = true;
      } finally {
        lockManager.releaseLock(Thread.currentThread(), groupKey, OLockManager.LOCK.EXCLUSIVE);
      }

      if (cacheSize.get() > cacheMaxSize) {
        Future future = commitExecutor.submit(new PeriodicFlushTask());
        try {
          future.get();
        } catch (InterruptedException e) {
          Thread.interrupted();
          throw new OException("File flush was interrupted", e);
        } catch (Exception e) {
          throw new OException("File flush was abnormally terminated", e);
        }
      }
    }
  }

  public OCachePointer get(long fileId, long pageIndex) throws IOException {
    synchronized (syncObject) {
      final GroupKey groupKey = new GroupKey(fileId, pageIndex >>> 4);
      lockManager.acquireLock(Thread.currentThread(), groupKey, OLockManager.LOCK.SHARED);
      try {
        final WriteGroup writeGroup = writeGroups.get(groupKey);

        if (writeGroup == null)
          return cacheFileContent(fileId, pageIndex);

        final int entryIndex = (int) (pageIndex & 15);
        OCachePointer pagePointer = writeGroup.pages[entryIndex];
        if (pagePointer == null)
          return cacheFileContent(fileId, pageIndex);

        return pagePointer;
      } finally {
        lockManager.releaseLock(Thread.currentThread(), groupKey, OLockManager.LOCK.SHARED);
      }
    }
  }

  public void acquireFlushLock(long fileId, long pageIndex) {
    pageFlushLockManger.acquireLock(Thread.currentThread(), new PageKey(fileId, pageIndex), OLockManager.LOCK.EXCLUSIVE);
  }

  public void releaseFlushLock(long fileId, long pageIndex) {
    pageFlushLockManger.releaseLock(Thread.currentThread(), new PageKey(fileId, pageIndex), OLockManager.LOCK.EXCLUSIVE);
  }

  public void flush(long fileId) {
    synchronized (syncObject) {
      Future<Void> future = commitExecutor.submit(new FileFlushTask(fileId));
      try {
        future.get();
      } catch (InterruptedException e) {
        Thread.interrupted();
        throw new OException("File flush was interrupted", e);
      } catch (Exception e) {
        throw new OException("File flush was abnormally terminated", e);
      }
    }
  }

  public void flush() {
    synchronized (syncObject) {
      for (long fileId : files.keySet())
        flush(fileId);
    }
  }

  public long getFilledUpTo(long fileId) throws IOException {
    synchronized (syncObject) {
      return files.get(fileId).getFilledUpTo() / pageSize;
    }
  }

  public void forceSyncStoredChanges() throws IOException {
    synchronized (syncObject) {
      for (OFileClassic fileClassic : files.values())
        fileClassic.synch();
    }
  }

  public boolean isOpen(long fileId) {
    synchronized (syncObject) {
      OFileClassic fileClassic = files.get(fileId);
      if (fileClassic != null)
        return fileClassic.isOpen();

      return false;
    }
  }

  public void setSoftlyClosed(long fileId, boolean softlyClosed) throws IOException {
    synchronized (syncObject) {
      OFileClassic fileClassic = files.get(fileId);
      if (fileClassic != null)
        fileClassic.setSoftlyClosed(softlyClosed);
    }
  }

  public boolean wasSoftlyClosed(long fileId) throws IOException {
    synchronized (syncObject) {
      OFileClassic fileClassic = files.get(fileId);
      if (fileClassic == null)
        return false;

      return fileClassic.wasSoftlyClosed();
    }
  }

  public void deleteFile(long fileId) throws IOException {
    synchronized (syncObject) {
      if (isOpen(fileId))
        truncateFile(fileId);

      files.get(fileId).delete();
      files.remove(fileId);
    }
  }

  public void truncateFile(long fileId) throws IOException {
    synchronized (syncObject) {
      removeCachedPages(fileId);
      files.get(fileId).shrink(0);
    }
  }

  public void renameFile(long fileId, String oldFileName, String newFileName) throws IOException {
    synchronized (syncObject) {
      if (!files.containsKey(fileId))
        return;

      final OFileClassic file = files.get(fileId);
      final String osFileName = file.getName();
      if (osFileName.startsWith(oldFileName)) {
        final File newFile = new File(storageLocal.getStoragePath() + File.separator + newFileName
            + osFileName.substring(osFileName.lastIndexOf(oldFileName) + oldFileName.length()));
        boolean renamed = file.renameTo(newFile);
        while (!renamed) {
          OMemoryWatchDog.freeMemoryForResourceCleanup(100);
          renamed = file.renameTo(newFile);
        }
      }
    }
  }

  public void close() throws IOException {
    synchronized (syncObject) {
      flush();

      if (!commitExecutor.isShutdown()) {
        commitExecutor.shutdown();
        try {
          if (!commitExecutor.awaitTermination(5, TimeUnit.MINUTES))
            throw new OException("Background data flush task can not be stopped.");
        } catch (InterruptedException e) {
          OLogManager.instance().error(this, "Data flush thread was interrupted");

          Thread.interrupted();
          throw new OException("Data flush thread was interrupted", e);
        }
      }

      for (OFileClassic fileClassic : files.values()) {
        if (fileClassic.isOpen())
          fileClassic.close();
      }
    }
  }

  private void removeCachedPages(long fileId) {
    Future<Void> future = commitExecutor.submit(new RemoveFilePagesTask(fileId));
    try {
      future.get();
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new OException("File data removal was interrupted", e);
    } catch (Exception e) {
      throw new OException("File data removal was abnormally terminated", e);
    }
  }

  private OCachePointer cacheFileContent(long fileId, long pageIndex) throws IOException {
    final OFileClassic fileClassic = files.get(fileId);
    final long startPosition = pageIndex * pageSize;
    final long endPosition = startPosition + pageSize;

    byte[] content = new byte[pageSize];
    OCachePointer dataPointer;
    if (fileClassic.getFilledUpTo() >= endPosition) {
      fileClassic.read(startPosition, content, content.length);
      dataPointer = new OCachePointer(content);
    } else {
      fileClassic.allocateSpace((int) (endPosition - fileClassic.getFilledUpTo()));
      dataPointer = new OCachePointer(content);
    }

    return dataPointer;
  }

  private void flushPage(long fileId, long pageIndex, long dataPointer) throws IOException {
    if (writeAheadLog != null) {
      OLogSequenceNumber lsn = OLocalPage.getLogSequenceNumberFromPage(directMemory, dataPointer);
      OLogSequenceNumber flushedLSN = writeAheadLog.getFlushedLSN();
      if (flushedLSN == null || flushedLSN.compareTo(lsn) < 0)
        writeAheadLog.flush();
    }

    final byte[] content = directMemory.get(dataPointer, pageSize);
    OLongSerializer.INSTANCE.serializeNative(MAGIC_NUMBER, content, 0);

    final int crc32 = calculatePageCrc(content);
    OIntegerSerializer.INSTANCE.serializeNative(crc32, content, OLongSerializer.LONG_SIZE);
    final OFileClassic fileClassic = files.get(fileId);
    final long startPosition = pageIndex * pageSize;
    final long endPosition = startPosition + pageSize;

    if (fileClassic.getFilledUpTo() < endPosition)
      fileClassic.allocateSpace((int) (endPosition - fileClassic.getFilledUpTo()));

    fileClassic.write(pageIndex * pageSize, content);

    if (syncOnPageFlush)
      fileClassic.synch();
  }

  private static int calculatePageCrc(byte[] pageData) {
    int systemSize = OLongSerializer.LONG_SIZE + OIntegerSerializer.INT_SIZE;

    final CRC32 crc32 = new CRC32();
    crc32.update(pageData, systemSize, pageData.length - systemSize);

    return (int) crc32.getValue();
  }

  public void close(long fileId, boolean flush) throws IOException {
    synchronized (syncObject) {
      if (flush)
        flush(fileId);
      else
        removeCachedPages(fileId);

      files.get(fileId).close();
    }
  }

  public OPageDataVerificationError[] checkStoredPages(OCommandOutputListener commandOutputListener) {
    final int notificationTimeOut = 5000;
    final List<OPageDataVerificationError> errors = new ArrayList<OPageDataVerificationError>();

    synchronized (syncObject) {
      for (long fileId : files.keySet()) {

        OFileClassic fileClassic = files.get(fileId);

        boolean fileIsCorrect;
        try {

          if (commandOutputListener != null)
            commandOutputListener.onMessage("Flashing file " + fileClassic.getName() + "... ");

          flush(fileId);

          if (commandOutputListener != null)
            commandOutputListener.onMessage("Start verification of content of " + fileClassic.getName() + "file ...");

          long time = System.currentTimeMillis();

          long filledUpTo = fileClassic.getFilledUpTo();
          fileIsCorrect = true;

          for (long pos = 0; pos < filledUpTo; pos += pageSize) {
            boolean checkSumIncorrect = false;
            boolean magicNumberIncorrect = false;

            byte[] data = new byte[pageSize];

            fileClassic.read(pos, data, data.length);

            long magicNumber = OLongSerializer.INSTANCE.deserializeNative(data, 0);

            if (magicNumber != MAGIC_NUMBER) {
              magicNumberIncorrect = true;
              if (commandOutputListener != null)
                commandOutputListener.onMessage("Error: Magic number for page " + (pos / pageSize) + " in file "
                    + fileClassic.getName() + " does not much !!!");
              fileIsCorrect = false;
            }

            final int storedCRC32 = OIntegerSerializer.INSTANCE.deserializeNative(data, OLongSerializer.LONG_SIZE);

            final int calculatedCRC32 = calculatePageCrc(data);
            if (storedCRC32 != calculatedCRC32) {
              checkSumIncorrect = true;
              if (commandOutputListener != null)
                commandOutputListener.onMessage("Error: Checksum for page " + (pos / pageSize) + " in file "
                    + fileClassic.getName() + " is incorrect !!!");
              fileIsCorrect = false;
            }

            if (magicNumberIncorrect || checkSumIncorrect)
              errors.add(new OPageDataVerificationError(magicNumberIncorrect, checkSumIncorrect, pos / pageSize, fileClassic
                  .getName()));

            if (commandOutputListener != null && System.currentTimeMillis() - time > notificationTimeOut) {
              time = notificationTimeOut;
              commandOutputListener.onMessage((pos / pageSize) + " pages were processed ...");
            }
          }
        } catch (IOException ioe) {
          if (commandOutputListener != null)
            commandOutputListener.onMessage("Error: Error during processing of file " + fileClassic.getName() + ". "
                + ioe.getMessage());

          fileIsCorrect = false;
        }

        if (!fileIsCorrect) {
          if (commandOutputListener != null)
            commandOutputListener.onMessage("Verification of file " + fileClassic.getName() + " is finished with errors.");
        } else {
          if (commandOutputListener != null)
            commandOutputListener.onMessage("Verification of file " + fileClassic.getName() + " is successfully finished.");
        }
      }

      return errors.toArray(new OPageDataVerificationError[errors.size()]);
    }
  }

  private final class GroupKey implements Comparable<GroupKey> {
    private final long fileId;
    private final long groupIndex;

    private GroupKey(long fileId, long groupIndex) {
      this.fileId = fileId;
      this.groupIndex = groupIndex;
    }

    @Override
    public int compareTo(GroupKey other) {
      if (fileId > other.fileId)
        return 1;
      if (fileId < other.fileId)
        return -1;

      if (groupIndex > other.groupIndex)
        return 1;
      if (groupIndex < other.groupIndex)
        return -1;

      return 0;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      GroupKey groupKey = (GroupKey) o;

      if (fileId != groupKey.fileId)
        return false;
      if (groupIndex != groupKey.groupIndex)
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = (int) (fileId ^ (fileId >>> 32));
      result = 31 * result + (int) (groupIndex ^ (groupIndex >>> 32));
      return result;
    }

    @Override
    public String toString() {
      return "GroupKey{" + "fileId=" + fileId + ", groupIndex=" + groupIndex + '}';
    }
  }

  private final class PeriodicFlushTask implements Runnable {

    @Override
    public void run() {
      try {
        if (writeGroups.isEmpty())
          return;

        int writeGroupsToFlush;
        boolean useForceSync = false;
        double threshold = ((double) cacheSize.get()) / cacheMaxSize;
        if (threshold > 0.8) {
          writeGroupsToFlush = (int) (0.2 * writeGroups.size());
          useForceSync = true;
        } else if (threshold > 0.9) {
          writeGroupsToFlush = (int) (0.4 * writeGroups.size());
          useForceSync = true;
        } else
          writeGroupsToFlush = 1;

        if (writeGroupsToFlush < 1)
          writeGroupsToFlush = 1;

        int flushedGroups = 0;

        flushedGroups = flushRing(writeGroupsToFlush, flushedGroups, false);

        if (flushedGroups < writeGroupsToFlush && useForceSync)
          flushedGroups = flushRing(writeGroupsToFlush, flushedGroups, true);

        if (flushedGroups < writeGroupsToFlush && cacheSize.get() > cacheMaxSize) {
          if (OGlobalConfiguration.SERVER_CACHE_INCREASE_ON_DEMAND.getValueAsBoolean()) {
            final long oldCacheMaxSize = cacheMaxSize;

            cacheMaxSize = (int) Math.ceil(cacheMaxSize * (1 + OGlobalConfiguration.SERVER_CACHE_INCREASE_STEP.getValueAsFloat()));
            OLogManager.instance().warn(this, "Write cache size is increased from %d to %d", oldCacheMaxSize, cacheMaxSize);
          } else {
            throw new OAllCacheEntriesAreUsedException("All records in write cache are used!");
          }
        }
      } catch (Exception e) {
        OLogManager.instance().error(this, "Exception during data flush.", e);
      }
    }

    private int flushRing(int writeGroupsToFlush, int flushedGroups, boolean forceFlush) throws IOException {
      NavigableMap<GroupKey, WriteGroup> subMap = writeGroups.tailMap(lastGroupKey, false);

      if (!subMap.isEmpty()) {
        flushedGroups = iterateBySubRing(subMap, writeGroupsToFlush, 0, forceFlush);
        if (flushedGroups < writeGroupsToFlush) {
          if (!subMap.isEmpty()) {
            subMap = writeGroups.headMap(subMap.firstKey(), false);
            flushedGroups = iterateBySubRing(subMap, writeGroupsToFlush, flushedGroups, forceFlush);
          }
        }
      } else
        flushedGroups = iterateBySubRing(writeGroups, writeGroupsToFlush, flushedGroups, forceFlush);

      return flushedGroups;
    }

    private int iterateBySubRing(NavigableMap<GroupKey, WriteGroup> subMap, int writeGroupsToFlush, int flushedWriteGroups,
        boolean forceFlush) throws IOException {
      Iterator<Map.Entry<GroupKey, WriteGroup>> entriesIterator = subMap.entrySet().iterator();
      long currentTime = System.currentTimeMillis();

      groupsLoop: while (entriesIterator.hasNext() && flushedWriteGroups < writeGroupsToFlush) {
        Map.Entry<GroupKey, WriteGroup> entry = entriesIterator.next();
        final WriteGroup group = entry.getValue();
        final GroupKey groupKey = entry.getKey();

        if (group.recencyBit && group.creationTime - currentTime < groupTTL && !forceFlush) {
          group.recencyBit = false;
          continue;
        }

        lockManager.acquireLock(Thread.currentThread(), entry.getKey(), OLockManager.LOCK.EXCLUSIVE);
        try {
          if (group.recencyBit && group.creationTime - currentTime < groupTTL && !forceFlush)
            group.recencyBit = false;
          else {
            group.recencyBit = false;

            List<PageKey> lockedPages = new ArrayList<PageKey>();
            for (int i = 0; i < 16; i++) {
              final PageKey pageKey = new PageKey(groupKey.fileId, groupKey.groupIndex << 4 + i);
              pageFlushLockManger.acquireLock(Thread.currentThread(), pageKey, OLockManager.LOCK.SHARED);
              lockedPages.add(pageKey);
            }

            try {
              for (OCachePointer pagePointer : group.pages) {
                if (pagePointer != null && pagePointer.getUsagesCount() > 0)
                  continue groupsLoop;
              }

              for (int i = 0; i < 16; i++) {
                final OCachePointer pagePointer = group.pages[i];

                if (pagePointer != null) {
                  flushPage(groupKey.fileId, groupKey.groupIndex * 16 + i, pagePointer.getDataPointer());
                  pagePointer.decrementReferrer();

                  cacheSize.decrementAndGet();
                }
              }

              lastGroupKey = entry.getKey();
              entriesIterator.remove();

              flushedWriteGroups++;
            } finally {
              for (PageKey pageKey : lockedPages)
                pageFlushLockManger.releaseLock(Thread.currentThread(), pageKey, OLockManager.LOCK.SHARED);
            }
          }
        } finally {
          lockManager.releaseLock(Thread.currentThread(), entry.getKey(), OLockManager.LOCK.EXCLUSIVE);
        }
      }

      return flushedWriteGroups;
    }
  }

  private final class FileFlushTask implements Callable<Void> {
    private final long fileId;

    private FileFlushTask(long fileId) {
      this.fileId = fileId;
    }

    @Override
    public Void call() throws Exception {
      final GroupKey firstKey = new GroupKey(fileId, 0);
      final GroupKey lastKey = new GroupKey(fileId, Long.MAX_VALUE);

      NavigableMap<GroupKey, WriteGroup> subMap = writeGroups.subMap(firstKey, true, lastKey, true);
      Iterator<Map.Entry<GroupKey, WriteGroup>> entryIterator = subMap.entrySet().iterator();

      while (entryIterator.hasNext()) {
        Map.Entry<GroupKey, WriteGroup> entry = entryIterator.next();
        lockManager.acquireLock(Thread.currentThread(), entry.getKey(), OLockManager.LOCK.EXCLUSIVE);
        try {
          WriteGroup writeGroup = entry.getValue();
          GroupKey groupKey = entry.getKey();
          List<PageKey> lockedPages = new ArrayList<PageKey>();
          for (int i = 0; i < 16; i++) {
            final PageKey pageKey = new PageKey(groupKey.fileId, groupKey.groupIndex << 4 + i);
            pageFlushLockManger.acquireLock(Thread.currentThread(), pageKey, OLockManager.LOCK.SHARED);
            lockedPages.add(pageKey);
          }

          try {
            for (OCachePointer pagePointer : writeGroup.pages) {
              if (pagePointer != null && pagePointer.getUsagesCount() > 0)
                throw new OException("Pages in write group with index " + groupKey.groupIndex + " are used and can not be flushed.");
            }

            for (int i = 0; i < 16; i++) {
              OCachePointer pagePointer = writeGroup.pages[i];

              if (pagePointer != null) {
                flushPage(groupKey.fileId, groupKey.groupIndex * 16 + i, pagePointer.getDataPointer());
                pagePointer.decrementReferrer();

                cacheSize.decrementAndGet();
              }
            }
            entryIterator.remove();
          } finally {
            for (PageKey pageKey : lockedPages)
              pageFlushLockManger.releaseLock(Thread.currentThread(), pageKey, OLockManager.LOCK.SHARED);
          }
        } finally {
          lockManager.releaseLock(Thread.currentThread(), entry.getKey(), OLockManager.LOCK.EXCLUSIVE);
        }
      }

      files.get(fileId).synch();
      return null;
    }
  }

  private final class RemoveFilePagesTask implements Callable<Void> {
    private final long fileId;

    private RemoveFilePagesTask(long fileId) {
      this.fileId = fileId;
    }

    @Override
    public Void call() throws Exception {
      final GroupKey firstKey = new GroupKey(fileId, 0);
      final GroupKey lastKey = new GroupKey(fileId, Long.MAX_VALUE);

      NavigableMap<GroupKey, WriteGroup> subMap = writeGroups.subMap(firstKey, true, lastKey, true);
      Iterator<Map.Entry<GroupKey, WriteGroup>> entryIterator = subMap.entrySet().iterator();

      while (entryIterator.hasNext()) {
        Map.Entry<GroupKey, WriteGroup> entry = entryIterator.next();
        lockManager.acquireLock(Thread.currentThread(), entry.getKey(), OLockManager.LOCK.EXCLUSIVE);
        try {
          WriteGroup writeGroup = entry.getValue();
          GroupKey groupKey = entry.getKey();

          List<PageKey> lockedPages = new ArrayList<PageKey>();
          for (int i = 0; i < 16; i++) {
            final PageKey pageKey = new PageKey(groupKey.fileId, groupKey.groupIndex << 4 + i);
            pageFlushLockManger.acquireLock(Thread.currentThread(), pageKey, OLockManager.LOCK.SHARED);
            lockedPages.add(pageKey);
          }

          try {

            for (OCachePointer pagePointer : writeGroup.pages) {
              if (pagePointer != null && pagePointer.getUsagesCount() > 0)
                throw new OException("Pages in write group with index " + groupKey.groupIndex + " are used and can not be removed.");
            }

            for (OCachePointer pagePointer : writeGroup.pages) {
              if (pagePointer != null) {
                pagePointer.decrementReferrer();

                cacheSize.decrementAndGet();
              }
            }
          } finally {
            for (PageKey pageKey : lockedPages)
              pageFlushLockManger.releaseLock(Thread.currentThread(), pageKey, OLockManager.LOCK.SHARED);
          }

          entryIterator.remove();
        } finally {
          lockManager.releaseLock(Thread.currentThread(), entry.getKey(), OLockManager.LOCK.EXCLUSIVE);
        }
      }

      return null;
    }
  }

  private static final class PageKey {
    private final long fileId;
    private final long pageIndex;

    public PageKey(long fileId, long pageIndex) {
      this.fileId = fileId;
      this.pageIndex = pageIndex;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      PageKey pageKey = (PageKey) o;

      if (fileId != pageKey.fileId)
        return false;
      if (pageIndex != pageKey.pageIndex)
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
    public String toString() {
      return "PageKey{" + "fileId=" + fileId + ", pageIndex=" + pageIndex + '}';
    }
  }
}
package com.intellij.openapi.vcs.changes.committed;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.update.FileGroup;
import com.intellij.openapi.vcs.update.UpdatedFiles;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;

import java.io.*;
import java.util.*;

/**
 * @author yole
 */
public class ChangesCacheFile {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.vcs.changes.committed.ChangesCacheFile");
  private static final int VERSION = 4;

  private File myPath;
  private File myIndexPath;
  private RandomAccessFile myStream;
  private RandomAccessFile myIndexStream;
  private boolean myStreamsOpen;
  private Project myProject;
  private AbstractVcs myVcs;
  private CachingCommittedChangesProvider myChangesProvider;
  private FilePath myRootPath;
  private RepositoryLocation myLocation;
  private Date myFirstCachedDate;
  private Date myLastCachedDate;
  private long myFirstCachedChangelist = Long.MAX_VALUE;
  private int myIncomingCount = 0;
  private boolean myHaveCompleteHistory = false;
  private boolean myHeaderLoaded = false;
  @NonNls private static final String INDEX_EXTENSION = ".index";
  private static final int INDEX_ENTRY_SIZE = 3*8+2;
  private static final int HEADER_SIZE = 34;

  public ChangesCacheFile(Project project, File path, AbstractVcs vcs, VirtualFile root, RepositoryLocation location) {
    final Calendar date = Calendar.getInstance();
    date.set(2020, 1, 2);
    myFirstCachedDate = date.getTime();
    date.set(1970, 1, 2);
    myLastCachedDate = date.getTime();
    myProject = project;
    myPath = path;
    myIndexPath = new File(myPath.toString() + INDEX_EXTENSION);
    myVcs = vcs;
    myChangesProvider = (CachingCommittedChangesProvider) vcs.getCommittedChangesProvider();
    myRootPath = new FilePathImpl(root);
    myLocation = location;
  }

  public RepositoryLocation getLocation() {
    return myLocation;
  }

  public CachingCommittedChangesProvider getProvider() {
    return myChangesProvider;
  }

  public boolean isEmpty() throws IOException {
    if (!myPath.exists()) {
      return true;
    }
    try {
      loadHeader();
    }
    catch(VersionMismatchException ex) {
      myPath.delete();
      myIndexPath.delete();
      return true;
    }
    catch(EOFException ex) {
      myPath.delete();
      myIndexPath.delete();
      return true;
    }

    return false;
  }

  public List<CommittedChangeList> writeChanges(final List<CommittedChangeList> changes, boolean assumeCompletelyDownloaded) throws IOException {
    List<CommittedChangeList> result = new ArrayList<CommittedChangeList>(changes.size());
    boolean wasEmpty = isEmpty();
    openStreams();
    try {
      if (wasEmpty) {
        writeHeader();
      }
      myStream.seek(myStream.length());
      IndexEntry[] entries = readLastIndexEntries(0, 1);
      // the list and index are sorted in direct chronological order
      Collections.sort(changes, new Comparator<CommittedChangeList>() {
        public int compare(final CommittedChangeList o1, final CommittedChangeList o2) {
          return o1.getCommitDate().compareTo(o2.getCommitDate());
        }
      });
      for(CommittedChangeList list: changes) {
        boolean duplicate = false;
        for(IndexEntry entry: entries) {
          if (list.getCommitDate().getTime() == entry.date && list.getNumber() == entry.number) {
            duplicate = true;
            break;
          }
        }
        if (duplicate) continue;
        result.add(list);
        long position = myStream.getFilePointer();
        //noinspection unchecked
        myChangesProvider.writeChangeList(myStream, list);
        if (list.getCommitDate().getTime() > myLastCachedDate.getTime()) {
          myLastCachedDate = list.getCommitDate();
        }
        if (list.getCommitDate().getTime() < myFirstCachedDate.getTime()) {
          myFirstCachedDate = list.getCommitDate();
        }
        if (list.getNumber() < myFirstCachedChangelist) {
          myFirstCachedChangelist = list.getNumber();
        }
        writeIndexEntry(list.getNumber(), list.getCommitDate().getTime(), position, assumeCompletelyDownloaded);
        if (!assumeCompletelyDownloaded) {
          myIncomingCount++;
        }
      }
      writeHeader();
      myHeaderLoaded = true;
    }
    finally {
      closeStreams();
    }
    return result;
  }

  private void writeIndexEntry(long number, long date, long offset, boolean completelyDownloaded) throws IOException {
    myIndexStream.writeLong(number);
    myIndexStream.writeLong(date);
    myIndexStream.writeLong(offset);
    myIndexStream.writeShort(completelyDownloaded ? 1 : 0);
  }

  private void openStreams() throws FileNotFoundException {
    myStream = new RandomAccessFile(myPath, "rw");
    myIndexStream = new RandomAccessFile(myIndexPath, "rw");
    myStreamsOpen = true;
  }

  private void closeStreams() throws IOException {
    myStreamsOpen = false;
    try {
      myStream.close();
    }
    finally {
      myIndexStream.close();
    }
  }

  private void writeHeader() throws IOException {
    assert myStreamsOpen;
    myStream.seek(0);
    myStream.writeInt(VERSION);
    myStream.writeLong(myLastCachedDate.getTime());
    myStream.writeLong(myFirstCachedDate.getTime());
    myStream.writeLong(myFirstCachedChangelist);
    myStream.writeShort(myHaveCompleteHistory ? 1 : 0);
    myStream.writeInt(myIncomingCount);
  }

  private IndexEntry[] readLastIndexEntries(int offset, int count) throws IOException {
    if (!myIndexPath.exists()) {
      return NO_ENTRIES;
    }
    long totalCount = myIndexStream.length() / INDEX_ENTRY_SIZE;
    if (count > totalCount - offset) {
      count = (int)totalCount - offset;
    }
    if (count == 0) {
      return NO_ENTRIES;
    }
    myIndexStream.seek(myIndexStream.length() - INDEX_ENTRY_SIZE * (count + offset));
    IndexEntry[] result = new IndexEntry[count];
    for(int i=0; i<count; i++) {
      result [i] = new IndexEntry();
      readIndexEntry(result [i]);
    }
    return result;
  }

  private void readIndexEntry(final IndexEntry result) throws IOException {
    result.number = myIndexStream.readLong();
    result.date = myIndexStream.readLong();
    result.offset = myIndexStream.readLong();
    result.completelyDownloaded = (myIndexStream.readShort() != 0);
  }

  public Date getLastCachedDate() throws IOException {
    loadHeader();
    return myLastCachedDate;
  }

  public Date getFirstCachedDate() throws IOException {
    loadHeader();
    return myFirstCachedDate;
  }

  public long getFirstCachedChangelist() throws IOException {
    loadHeader();
    return myFirstCachedChangelist;
  }

  private void loadHeader() throws IOException {
    if (!myHeaderLoaded) {
      RandomAccessFile stream = new RandomAccessFile(myPath, "r");
      try {
        int version = stream.readInt();
        if (version != VERSION) {
          throw new VersionMismatchException();
        }
        myLastCachedDate = new Date(stream.readLong());
        myFirstCachedDate = new Date(stream.readLong());
        myFirstCachedChangelist = stream.readLong();
        myHaveCompleteHistory = (stream.readShort() != 0);
        myIncomingCount = stream.readInt();
        assert stream.getFilePointer() == HEADER_SIZE;
      }
      finally {
        stream.close();
      }
      myHeaderLoaded = true;
    }
  }

  public List<CommittedChangeList> readChanges(final ChangeBrowserSettings settings, final int maxCount) throws IOException {
    final List<CommittedChangeList> result = new ArrayList<CommittedChangeList>();
    final ChangeBrowserSettings.Filter filter = settings.createFilter();
    openStreams();
    try {
      if (maxCount == 0) {
        myStream.seek(HEADER_SIZE);  // skip header
        while(myStream.getFilePointer() < myStream.length()) {
          CommittedChangeList changeList = myChangesProvider.readChangeList(myLocation, myStream);
          if (filter.accepts(changeList)) {
            result.add(changeList);
          }
        }
      }
      else if (!settings.isAnyFilterSpecified()) {
        IndexEntry[] entries = readLastIndexEntries(0, maxCount);
        for(IndexEntry entry: entries) {
          myStream.seek(entry.offset);
          result.add(myChangesProvider.readChangeList(myLocation, myStream));
        }
      }
      else {
        int offset = 0;
        while(result.size() < maxCount) {
          IndexEntry[] entries = readLastIndexEntries(offset, 1);
          if (entries.length == 0) {
            break;
          }
          CommittedChangeList changeList = loadChangeListAt(entries [0].offset);
          if (filter.accepts(changeList)) {
            result.add(0, changeList);
          }
          offset++;
        }
      }
      return result;
    }
    finally {
      closeStreams();
    }
  }

  public boolean hasCompleteHistory() {
    return myHaveCompleteHistory;
  }

  public void setHaveCompleteHistory(final boolean haveCompleteHistory) {
    if (myHaveCompleteHistory != haveCompleteHistory) {
      myHaveCompleteHistory = haveCompleteHistory;
      try {
        openStreams();
        try {
          writeHeader();
        }
        finally {
          closeStreams();
        }
      }
      catch(IOException ex) {
        LOG.error(ex);
      }
    }
  }

  public Collection<? extends CommittedChangeList> loadIncomingChanges() throws IOException {
    final List<CommittedChangeList> result = new ArrayList<CommittedChangeList>();
    int offset = 0;
    openStreams();
    try {
      while(true) {
        IndexEntry[] entries = readLastIndexEntries(offset, 1);
        if (entries.length == 0) {
          break;
        }
        if (!entries [0].completelyDownloaded) {
          result.add(loadChangeListAt(entries[0].offset));
          if (result.size() == myIncomingCount) break;
        }
        offset++;
      }
      LOG.info("Loaded " + result.size() + " incoming changelists");
      return result;
    }
    finally {
      closeStreams();
    }
  }

  private CommittedChangeList loadChangeListAt(final long clOffset) throws IOException {
    myStream.seek(clOffset);
    return myChangesProvider.readChangeList(myLocation, myStream);
  }

  public boolean processUpdatedFiles(final UpdatedFiles updatedFiles) throws IOException {
    boolean haveUnaccountedUpdatedFiles = false;
    openStreams();
    try {
      final List<IncomingChangeListData> incomingData = loadIncomingChangeListData();
      for(FileGroup group: updatedFiles.getTopLevelGroups()) {
        haveUnaccountedUpdatedFiles |= processGroup(group, incomingData);
      }
      if (!haveUnaccountedUpdatedFiles) {
        for(IncomingChangeListData data: incomingData) {
          saveIncoming(data);
        }
        writeHeader();
      }
    }
    finally {
      closeStreams();
    }
    return haveUnaccountedUpdatedFiles;
  }

  private void saveIncoming(final IncomingChangeListData data) throws IOException {
    writePartial(data);
    if (data.accountedChanges.size() == data.changeList.getChanges().size()) {
      LOG.info("Removing changelist " + data.changeList.getNumber() + " from incoming changelists");
      myIndexStream.seek(data.indexOffset);
      writeIndexEntry(data.indexEntry.number, data.indexEntry.date, data.indexEntry.offset, true);
      myIncomingCount--;
    }
  }

  private boolean processGroup(final FileGroup group, final List<IncomingChangeListData> incomingData) {
    boolean haveUnaccountedUpdatedFiles = false;
    final List<Pair<String,VcsRevisionNumber>> list = group.getFilesAndRevisions(myProject);
    for(Pair<String, VcsRevisionNumber> pair: list) {
      haveUnaccountedUpdatedFiles |= processFile(pair.first, pair.second, incomingData);
    }
    for(FileGroup childGroup: group.getChildren()) {
      haveUnaccountedUpdatedFiles |= processGroup(childGroup, incomingData);
    }
    return haveUnaccountedUpdatedFiles;
  }

  private boolean processFile(final String file, final VcsRevisionNumber number, final List<IncomingChangeListData> incomingData) {
    FilePath path = new FilePathImpl(new File(file), false);
    if (!path.isUnder(myRootPath, false) || number == null) {
      return false;
    }
    boolean foundRevision = false;
    for(IncomingChangeListData data: incomingData) {
      for(Change change: data.changeList.getChanges()) {
        ContentRevision afterRevision = change.getAfterRevision();
        if (afterRevision != null && afterRevision.getFile().equals(path)) {
          int rc = number.compareTo(afterRevision.getRevisionNumber());
          if (rc == 0) {
            foundRevision = true;
          }
          if (rc >= 0) {
            data.accountedChanges.add(change);
          }
        }
      }
    }
    return !foundRevision;
  }

  private List<IncomingChangeListData> loadIncomingChangeListData() throws IOException {
    final long length = myIndexStream.length();
    long totalCount = length / INDEX_ENTRY_SIZE;
    List<IncomingChangeListData> incomingData = new ArrayList<IncomingChangeListData>();
    for(int i=0; i<totalCount; i++) {
      final long indexOffset = length - (i + 1) * INDEX_ENTRY_SIZE;
      myIndexStream.seek(indexOffset);
      IndexEntry e = new IndexEntry();
      readIndexEntry(e);
      if (!e.completelyDownloaded) {
        IncomingChangeListData data = new IncomingChangeListData();
        data.indexOffset = indexOffset;
        data.indexEntry = e;
        data.changeList = loadChangeListAt(e.offset);
        readPartial(data);
        incomingData.add(data);
        if (incomingData.size() == myIncomingCount) {
          break;
        }
      }
    }
    LOG.info("Loaded " + incomingData.size() + "incoming changelist pointers");
    return incomingData;
  }

  private void writePartial(final IncomingChangeListData data) throws IOException {
    File partialFile = getPartialPath(data.indexEntry.offset);
    final int accounted = data.accountedChanges.size();
    if (accounted == data.changeList.getChanges().size()) {
      partialFile.delete();
    }
    else if (accounted > 0) {
      RandomAccessFile file = new RandomAccessFile(partialFile, "rw");
      try {
        file.writeInt(accounted);
        for(Change c: data.accountedChanges) {
          boolean isAfterRevision = true;
          ContentRevision revision = c.getAfterRevision();
          if (revision == null) {
            isAfterRevision = false;
            revision = c.getBeforeRevision();
            assert revision != null;
          }
          file.writeByte(isAfterRevision ? 1 : 0);
          file.writeUTF(revision.getFile().getIOFile().toString());
        }
      }
      finally {
        file.close();
      }
    }
  }

  private void readPartial(IncomingChangeListData data) {
    HashSet<Change> result = new HashSet<Change>();
    try {
      File partialFile = getPartialPath(data.indexEntry.offset);
      if (partialFile.exists()) {
        RandomAccessFile file = new RandomAccessFile(partialFile, "r");
        try {
          int count = file.readInt();
          for(int i=0; i<count; i++) {
            boolean isAfterRevision = (file.readByte() != 0);
            String path = file.readUTF();
            for(Change c: data.changeList.getChanges()) {
              final ContentRevision afterRevision = isAfterRevision ? c.getAfterRevision() : c.getBeforeRevision();
              if (afterRevision != null && afterRevision.getFile().getIOFile().toString().equals(path)) {
                result.add(c);
              }
            }
          }
        }
        finally {
          file.close();
        }
      }
    }
    catch(IOException ex) {
      LOG.error(ex);
    }
    data.accountedChanges = result;
  }

  @NonNls
  private File getPartialPath(final long offset) {
    return new File(myPath + "." + offset + ".partial");
  }

  public boolean refreshIncomingChanges() throws IOException {
    DiffProvider diffProvider = myVcs.getDiffProvider();
    if (diffProvider == null) return false;
    boolean anyChanges = false;
    openStreams();
    try {
      final List<IncomingChangeListData> list = loadIncomingChangeListData();
      for(IncomingChangeListData data: list) {
        LOG.info("Checking incoming changelist " + data.changeList.getNumber());
        boolean updated = false;
        for(Change change: data.changeList.getChanges()) {
          if (data.accountedChanges.contains(change)) continue;
          ContentRevision afterRevision = change.getAfterRevision();
          if (afterRevision != null) {
            afterRevision.getFile().refresh();
            VirtualFile file = afterRevision.getFile().getVirtualFile();
            if (file != null) {
              VcsRevisionNumber revision = diffProvider.getCurrentRevision(file);
              if (revision != null) {
                int rc = revision.compareTo(afterRevision.getRevisionNumber());
                if (rc >= 0) {
                  data.accountedChanges.add(change);
                  updated = true;
                }
              }
            }
          }
        }
        if (updated) {
          anyChanges = true;
          saveIncoming(data);
        }
      }
      if (anyChanges) {
        writeHeader();
      }
    }
    finally {
      closeStreams();
    }
    return anyChanges;
  }

  private static class IndexEntry {
    long number;
    long date;
    long offset;
    boolean completelyDownloaded;
  }

  private static class IncomingChangeListData {
    public long indexOffset;
    public IndexEntry indexEntry;
    public CommittedChangeList changeList;
    public Set<Change> accountedChanges;
  }

  private static final IndexEntry[] NO_ENTRIES = new IndexEntry[0];

  private static class VersionMismatchException extends RuntimeException {
  }
}
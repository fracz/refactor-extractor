package com.intellij.openapi.vcs.changes;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;

/**
 * @author max
 */
public class Change {
  public enum Type {
    MODIFICATION,
    NEW,
    DELETED,
    MOVED
  }

  private final ContentRevision myBeforeRevision;
  private final ContentRevision myAfterRevision;
  private final FileStatus myFileStatus;


  public Change(final ContentRevision beforeRevision, final ContentRevision afterRevision) {
    this(beforeRevision, afterRevision, convertStatus(beforeRevision, afterRevision));
  }

  public Change(final ContentRevision beforeRevision, final ContentRevision afterRevision, FileStatus fileStatus) {
    myBeforeRevision = beforeRevision;
    myAfterRevision = afterRevision;
    myFileStatus = fileStatus;
  }

  private static FileStatus convertStatus(ContentRevision beforeRevision, ContentRevision afterRevision) {
    if (beforeRevision == null) return FileStatus.ADDED;
    if (afterRevision == null) return FileStatus.DELETED;
    return FileStatus.MODIFIED;
  }

  public Type getType() {
    if (myBeforeRevision == null) {
      return Type.NEW;
    }

    if (myAfterRevision == null) {
      return Type.DELETED;
    }

    if (!Comparing.equal(myBeforeRevision.getFile().getPath(), myAfterRevision.getFile().getPath())) {
      return Type.MOVED;
    }

    return Type.MODIFICATION;
  }

  public ContentRevision getBeforeRevision() {
    return myBeforeRevision;
  }

  public ContentRevision getAfterRevision() {
    return myAfterRevision;
  }

  public FileStatus getFileStatus() {
    return myFileStatus;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final ContentRevision br1 = getBeforeRevision();
    final ContentRevision br2 = ((Change)o).getBeforeRevision();
    final ContentRevision ar1 = getAfterRevision();
    final ContentRevision ar2 = ((Change)o).getAfterRevision();

    FilePath fbr1 = br1 != null ? br1.getFile() : null;
    FilePath fbr2 = br2 != null ? br2.getFile() : null;

    FilePath far1 = ar1 != null ? ar1.getFile() : null;
    FilePath far2 = ar2 != null ? ar2.getFile() : null;

    return Comparing.equal(fbr1, fbr2) && Comparing.equal(far1, far2);
  }

  public int hashCode() {
    return revisionHashCode(getBeforeRevision()) * 27 + revisionHashCode(getAfterRevision());
  }

  private static int revisionHashCode(ContentRevision rev) {
    if (rev == null) return 0;
    return rev.getFile().hashCode();
  }
}
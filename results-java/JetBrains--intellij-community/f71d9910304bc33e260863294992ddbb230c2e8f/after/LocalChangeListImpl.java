/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.intellij.openapi.vcs.changes;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.impl.ExcludedFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author yole
 */
public class LocalChangeListImpl extends LocalChangeList {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.vcs.changes.ChangeList");

  private Project myProject;
  private Collection<Change> myChanges = new HashSet<Change>();
  private Collection<Change> myReadChangesCache = null;
  @NotNull private String myName;
  private String myComment = "";

  private boolean myIsDefault = false;
  private boolean myIsReadOnly = false;
  private Collection<Change> myOutdatedChanges;
  private boolean myIsInUpdate = false;
  private ChangeHashSet myChangesBeforeUpdate;

  public static LocalChangeListImpl createEmptyChangeListImpl(Project project, String name) {
    return new LocalChangeListImpl(project, name);
  }

  private LocalChangeListImpl(Project project, final String name) {
    myProject = project;
    myName = name;
  }

  public synchronized Collection<Change> getChanges() {
    createReadChangesCache();
    return myReadChangesCache;
  }

  private void createReadChangesCache() {
    if (myReadChangesCache == null) {
      final HashSet<Change> changes = new HashSet<Change>(myChanges);
      if (myOutdatedChanges != null) {
        changes.addAll(myOutdatedChanges);
      }
      myReadChangesCache = Collections.unmodifiableCollection(changes);
    }
  }

  @NotNull
  public String getName() {
    return myName;
  }

  // seems that here we can do "rename list with the same name inside ChangeListManagerImpl"
  // because it's likely that usages of this method is like that.
  // but it is not very good... it can mix everything up
  public void setName(@NotNull final String name) {
    if (! myName.equals(name)) {

      String oldName = myName;
      ChangeListManagerImpl.getInstanceImpl(myProject).editName(oldName, name);
      myName = name;
    }
  }

  public String getComment() {
    return myComment;
  }

  // same as for setName()
  public void setComment(final String comment) {
    if (! Comparing.equal(comment, myComment)) {
      String oldComment = myComment;
      ChangeListManagerImpl.getInstanceImpl(myProject).editComment(oldComment, comment);
      myComment = comment != null ? comment : "";
    }
  }

  void setNameImpl(@NotNull final String name) {
    myName = name;
  }

  void setCommentImpl(final String comment) {
    myComment = comment;
  }

  public boolean isDefault() {
    return myIsDefault;
  }

  public synchronized boolean isInUpdate() {
    return myIsInUpdate;
  }

  void setDefault(final boolean isDefault) {
    myIsDefault = isDefault;
  }

  public boolean isReadOnly() {
    return myIsReadOnly;
  }

  public void setReadOnly(final boolean isReadOnly) {
    myIsReadOnly = isReadOnly;
  }

  synchronized void addChange(Change change) {
    if (!myIsInUpdate) myReadChangesCache = null;
    myChanges.add(change);
  }

  synchronized Change removeChange(Change change) {
    for (Change localChange : myChanges) {
      if (localChange.equals(change)) {
        myChanges.remove(localChange);
        if (! myIsInUpdate) {
          myReadChangesCache = null;
        }
        return localChange;
      }
    }
    return null;
  }

  synchronized Collection<Change> startProcessingChanges(final Project project, @Nullable final VcsDirtyScope scope) {
    createReadChangesCache();
    final Collection<Change> result = new ArrayList<Change>();
    myChangesBeforeUpdate = new ChangeHashSet(myChanges);
    myOutdatedChanges = new ArrayList<Change>();
    final ExcludedFileIndex fileIndex = ExcludedFileIndex.getInstance(project);
    for (Change oldBoy : myChangesBeforeUpdate) {
      final ContentRevision before = oldBoy.getBeforeRevision();
      final ContentRevision after = oldBoy.getAfterRevision();
      if (scope == null || before != null && scope.belongsTo(before.getFile()) || after != null && scope.belongsTo(after.getFile())
        || isIgnoredChange(oldBoy, fileIndex)) {
        result.add(oldBoy);
        myIsInUpdate = true;
        removeChange(oldBoy);
        myOutdatedChanges.add(oldBoy);
      }
    }
    if (isDefault()) {
      myIsInUpdate = true;
    }
    return result;
  }

  private static boolean isIgnoredChange(final Change change, final ExcludedFileIndex fileIndex) {
    boolean beforeRevIgnored = change.getBeforeRevision() == null || isIgnoredRevision(change.getBeforeRevision(), fileIndex);
    boolean afterRevIgnored = change.getAfterRevision() == null || isIgnoredRevision(change.getAfterRevision(), fileIndex);
    return beforeRevIgnored && afterRevIgnored;
  }

  private static boolean isIgnoredRevision(final ContentRevision revision, final ExcludedFileIndex fileIndex) {
    VirtualFile vFile = revision.getFile().getVirtualFile();
    return vFile != null && fileIndex.isExcludedFile(vFile);
  }

  synchronized boolean processChange(Change change) {
    if (myIsDefault) {
      addChange(change);
      return true;
    }

    for (Change oldChange : myOutdatedChanges) {
      if (Comparing.equal(oldChange, change)) {
        addChange(change);
        return true;
      }
    }
    return false;
  }

  synchronized boolean doneProcessingChanges() {
    boolean changesDetected = (myChanges.size() != myChangesBeforeUpdate.size());

    Change[] newChanges = myChanges.toArray(new Change[myChanges.size()]);
    for (int i = 0; i < newChanges.length; i++) {
      Change oldChange = findOldChange(newChanges[i]);
      if (oldChange != null) {
        newChanges[i] = oldChange;
      }
      else {
        changesDetected = true;
      }
    }

    myChanges = new HashSet<Change>(Arrays.asList(newChanges));
    myOutdatedChanges = null;
    myReadChangesCache = null;
    myIsInUpdate = false;
    return changesDetected;
  }

  @Nullable
  private Change findOldChange(final Change newChange) {
    Change oldChange = myChangesBeforeUpdate.getEqualChange(newChange);
    if (oldChange != null && sameBeforeRevision(oldChange, newChange) &&
        newChange.getFileStatus().equals(oldChange.getFileStatus())) {
      return oldChange;
    }
    return null;
  }

  private static boolean sameBeforeRevision(final Change change1, final Change change2) {
    final ContentRevision b1 = change1.getBeforeRevision();
    final ContentRevision b2 = change2.getBeforeRevision();
    if (b1 != null && b2 != null) {
      final VcsRevisionNumber rn1 = b1.getRevisionNumber();
      final VcsRevisionNumber rn2 = b2.getRevisionNumber();
      final boolean isBinary1 = (b1 instanceof BinaryContentRevision);
      final boolean isBinary2 = (b2 instanceof BinaryContentRevision);
      return rn1 != VcsRevisionNumber.NULL && rn2 != VcsRevisionNumber.NULL && rn1.compareTo(rn2) == 0 && isBinary1 == isBinary2;
    }
    return false;
  }

  public synchronized boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final LocalChangeListImpl list = (LocalChangeListImpl)o;

    if (myIsDefault != list.myIsDefault) return false;
    if (!myName.equals(list.myName)) return false;
    if (myIsReadOnly != list.myIsReadOnly) return false;

    return true;
  }

  public int hashCode() {
    return myName.hashCode();
  }

  @Override
  public String toString() {
    return myName.trim();
  }

  public synchronized LocalChangeList copy() {
    final LocalChangeListImpl copy = new LocalChangeListImpl(myProject, myName);
    copy.myComment = myComment;
    copy.myIsDefault = myIsDefault;
    copy.myIsInUpdate = myIsInUpdate;
    copy.myIsReadOnly = myIsReadOnly;

    if (myChanges != null) {
      copy.myChanges = new HashSet<Change>(myChanges);
    }

    if (myChangesBeforeUpdate != null) {
      copy.myChangesBeforeUpdate = new ChangeHashSet(myChangesBeforeUpdate);
    }

    if (myOutdatedChanges != null) {
      copy.myOutdatedChanges = new ArrayList<Change>(myOutdatedChanges);
    }

    if (myReadChangesCache != null) {
      copy.myReadChangesCache = new HashSet<Change>(myReadChangesCache);
    }

    return copy;
  }

  private static class ChangeHashSet extends THashSet<Change> {
    public ChangeHashSet(final Collection<? extends Change> changes) {
      super(changes);
    }

    @Nullable
    Change getEqualChange(Change other) {
      int aIndex = index(other);
      if (aIndex >= 0) return (Change)_set [aIndex];
      return null;
    }
  }
}
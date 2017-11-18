package com.intellij.openapi.vcs.changes;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.util.EventDispatcher;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.io.File;

/** should work under _external_ lock
* just logic here: do modifications to group of change lists
*/
public class ChangeListWorker implements ChangeListsWriteOperations {
  private final static Logger LOG = Logger.getInstance("#com.intellij.openapi.vcs.changes.ChangeListWorker");

  private final Project myProject;
  private final Map<String, LocalChangeList> myMap;
  private LocalChangeList myDefault;

  private ChangeListsIndexes myIdx;

  public ChangeListWorker(final Project project) {
    myProject = project;
    myMap = new HashMap<String, LocalChangeList>();
    myIdx = new ChangeListsIndexes();
  }

  private ChangeListWorker(final ChangeListWorker worker) {
    myProject = worker.myProject;
    myMap = new HashMap<String, LocalChangeList>();
    myIdx = new ChangeListsIndexes(worker.myIdx);

    LocalChangeList defaultList = null;
    for (LocalChangeList changeList : worker.myMap.values()) {
      final LocalChangeList copy = changeList.copy();
      ((LocalChangeListImpl) copy).setLocalListener(myIdx);

      final String changeListName = copy.getName();
      myMap.put(changeListName, copy);
      if (copy.isDefault()) {
        defaultList = copy;
      }
    }
    assert defaultList != null;
    myDefault = defaultList;
  }

  public void takeData(@NotNull final ChangeListWorker worker) {
    myMap.clear();
    myMap.putAll(worker.myMap);
    myDefault = worker.myDefault;
    myIdx = new ChangeListsIndexes(worker.myIdx);
  }

  public ChangeListWorker copy() {
    return new ChangeListWorker(this);
  }

  public boolean findListByName(@NotNull final String name) {
    return myMap.containsKey(name);
  }

  @Nullable
  public LocalChangeList getCopyByName(final String name) {
    return myMap.get(name);
  }

  /**
   * @return if list with name exists, return previous default list name or null of there wasn't previous
   */
  @Nullable
  public String setDefault(final String name) {
    final LocalChangeList newDefault = myMap.get(name);
    if (newDefault == null) {
      return null;
    }
    String previousName = null;
    if (myDefault != null) {
      ((LocalChangeListImpl) myDefault).setDefault(false);
      previousName = myDefault.getName();
    }

    ((LocalChangeListImpl) newDefault).setDefault(true);
    myDefault = newDefault;

    return previousName;
  }

  public boolean setReadOnly(final String name, final boolean value) {
    final LocalChangeList list = myMap.get(name);
    if (list != null) {
      list.setReadOnly(value);
    }
    return list != null;
  }

  public void startProcessingChanges(@NotNull final String name, final VcsDirtyScope scope) {
    final LocalChangeList changeList = myMap.get(name);
    if (changeList != null) {
      ((LocalChangeListImpl) changeList).startProcessingChanges(myProject, scope);
    }
  }

  public LocalChangeList addChangeList(@NotNull final String name, @Nullable final String description) {
    return addChangeList(name, description, false);
  }

  public LocalChangeList addChangeList(@NotNull final String name, @Nullable final String description, final boolean inUpdate) {
    final boolean contains = myMap.containsKey(name);
    LOG.assertTrue(! contains, "Attempt to create duplicate changelist " + name);
    if (! contains) {
      final LocalChangeListImpl newList = (LocalChangeListImpl) LocalChangeList.createEmptyChangeList(myProject, name);
      newList.setLocalListener(myIdx);

      if (description != null) {
        newList.setComment(description);
      }
      myMap.put(name, newList);
      if (inUpdate) {
        // scope is not important: nothing had been added jet, nothing to move to "old state" members
        newList.startProcessingChanges(myProject, null);
      }
      return newList.copy();
    }
    return null;
  }

  public boolean addChangeToList(@NotNull final String name, final Change change) {
    final LocalChangeList changeList = myMap.get(name);
    if (changeList != null) {
      ((LocalChangeListImpl) changeList).addChange(change);
    }
    return changeList != null;
  }

  public void addChangeToCorrespondingList(final Change change) {
    assert myDefault != null;
    for (LocalChangeList list : myMap.values()) {
      if (list.isDefault()) continue;
      if (((LocalChangeListImpl) list).processChange(change)) return;
    }
    ((LocalChangeListImpl) myDefault).processChange(change);
  }

  public boolean removeChangeList(@NotNull String name) {
    final LocalChangeList list = myMap.get(name);
    if (list == null) {
      return false;
    }
    if (list.isDefault()) {
      throw new RuntimeException(new IncorrectOperationException("Cannot remove default changelist"));
    }
    final String listName = list.getName();

    for (Change change : list.getChanges()) {
      myIdx.changeRemoved(listName, change);
      ((LocalChangeListImpl) myDefault).addChange(change);
    }

    myMap.remove(listName);
    return true;
  }

  @Nullable
  public MultiMap<LocalChangeList, Change> moveChangesTo(final String name, final Change[] changes) {
    final LocalChangeListImpl changeList = (LocalChangeListImpl) myMap.get(name);
    if (changeList != null) {
      final MultiMap<LocalChangeList, Change> result = new MultiMap<LocalChangeList, Change>();
      for (LocalChangeList list : myMap.values()) {
        for (Change change : changes) {
          final Change removedChange = ((LocalChangeListImpl)list).removeChange(change);
          if (removedChange != null) {
            changeList.addChange(removedChange);
            result.putValue(list, removedChange);
          }
        }
      }
      return result;
    }
    return null;
  }

  public boolean editName(@NotNull final String fromName, @NotNull final String toName) {
    if (fromName.equals(toName)) return false;
    final LocalChangeList list = myMap.get(fromName);
    if (list != null) {
      ((LocalChangeListImpl) list).setNameImpl(toName);
      myMap.remove(fromName);
      myMap.put(toName, list);
      myIdx.renamed(toName, list.getChanges());
    }
    return list != null;
  }

  @Nullable
  public String editComment(@NotNull final String fromName, final String newComment) {
    final LocalChangeList list = myMap.get(fromName);
    if (list != null) {
      final String oldComment = list.getComment();
      if (! Comparing.equal(oldComment, newComment)) {
        ((LocalChangeListImpl) list).setCommentImpl(newComment);
      }
      return oldComment;
    }
    return null;
  }

  public boolean isEmpty() {
    return myMap.isEmpty();
  }

  @Nullable
  public LocalChangeList getDefaultListCopy() {
    return myDefault == null ? null : myDefault.copy();
  }

  public Project getProject() {
    return myProject;
  }

  public void notifyStartProcessingChanges(final VcsDirtyScope scope) {
    for (LocalChangeList list : myMap.values()) {
      ((LocalChangeListImpl) list).startProcessingChanges(myProject, scope);
    }
  }

  public void notifyDoneProcessingChanges(final EventDispatcher<ChangeListListener> dispatcher) {
    List<ChangeList> changedLists = new ArrayList<ChangeList>();
      for (LocalChangeList list : myMap.values()) {
        if (((LocalChangeListImpl) list).doneProcessingChanges()) {
          changedLists.add(list);
        }
      }
    for(ChangeList changeList: changedLists) {
      dispatcher.getMulticaster().changeListChanged(changeList);
    }
  }

  public List<LocalChangeList> getListsCopy() {
    final List<LocalChangeList> result = new ArrayList<LocalChangeList>();
    for (LocalChangeList list : myMap.values()) {
      result.add(list.copy());
    }
    return result;
  }

  public String getDefaultListName() {
    return myDefault == null ? null : myDefault.getName();
  }

  public interface LocalListListener {
    void changeAdded(final String listName, final Change change);
    void changeRemoved(final String listName, final Change change);
  }


  public List<File> getAffectedPaths() {
    return myIdx.getAffectedPaths();
  }

  @NotNull
  public List<VirtualFile> getAffectedFiles() {
    return myIdx.getAffectedFiles();
  }

  // should be tested
  public String getListName(@NotNull final VirtualFile file) {
    final String listName = myIdx.getListName(file);
    if (listName == null || ((listName != null) && myMap.containsKey(listName))) {
      return listName;
    }
    LOG.info("Error: index does not coinside with change lists map. list name: " + listName + " for file: " + file.getPath());
    for (LocalChangeList list : myMap.values()) {
      for (Change change : list.getChanges()) {
        if (change.getAfterRevision() != null &&
            Comparing.equal(change.getAfterRevision().getFile().getVirtualFile(), file)) {
          return listName;
        }
        if (change.getBeforeRevision() != null &&
            Comparing.equal(change.getBeforeRevision().getFile().getVirtualFile(), file)) {
          return listName;
        }
      }
    }
    return null;
  }

  public FileStatus getStatus(final VirtualFile file) {
    return myIdx.getStatus(file);
  }

  @Nullable
  public LocalChangeList listForChange(final Change change) {
    for (LocalChangeList list : myMap.values()) {
      if (list.getChanges().contains(change)) return list.copy();
    }
    return null;
  }

  @Nullable
  public String listNameIfOnlyOne(final @Nullable Change[] changes) {
    if (changes == null || changes.length == 0) {
      return null;
    }

    final Change first = changes[0];

    for (LocalChangeList list : myMap.values()) {
      final Collection<Change> listChanges = list.getChanges();
      if (listChanges.contains(first)) {
        // must contain all other
        for (int i = 1; i < changes.length; i++) {
          final Change change = changes[i];
          if (! listChanges.contains(change)) {
            return null;
          }
        }
        return list.getName();
      }
    }
    return null;
  }

  @NotNull
  public Collection<Change> getChangesIn(final FilePath dirPath) {
    List<Change> changes = new ArrayList<Change>();
    for (ChangeList list : myMap.values()) {
      for (Change change : list.getChanges()) {
        final ContentRevision afterRevision = change.getAfterRevision();
        if (afterRevision != null && afterRevision.getFile().isUnder(dirPath, false)) {
          changes.add(change);
          continue;
        }

        final ContentRevision beforeRevision = change.getBeforeRevision();
        if (beforeRevision != null && beforeRevision.getFile().isUnder(dirPath, false)) {
          changes.add(change);
        }
      }
    }
    return changes;
  }

  ChangeListManagerGate createSelfGate() {
    return new MyGate(this);
  }

  private static class MyGate implements ChangeListManagerGate {
    private final ChangeListWorker myWorker;

    private MyGate(final ChangeListWorker worker) {
      myWorker = worker;
    }

    @Nullable
    public LocalChangeList findChangeList(final String name) {
      return myWorker.getCopyByName(name);
    }

    public LocalChangeList addChangeList(final String name, final String comment) {
      return myWorker.addChangeList(name, comment, true);
    }

    public LocalChangeList findOrCreateList(final String name, final String comment) {
      LocalChangeList list = myWorker.getCopyByName(name);
      if (list == null) {
        list = addChangeList(name, comment);
      }
      return list;
    }
  }
}
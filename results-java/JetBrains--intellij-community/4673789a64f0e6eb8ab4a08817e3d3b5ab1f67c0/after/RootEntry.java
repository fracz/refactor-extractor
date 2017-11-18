package com.intellij.localvcs;

import java.io.IOException;

public class RootEntry extends DirectoryEntry {
  // todo try to crean up Entry hierarchy
  public RootEntry() {
    super(null, null);
  }

  public RootEntry(Stream s) throws IOException {
    super(s);
  }

  protected Path getPathAppendedWith(String name) {
    return new Path(name);
  }

  @Override
  protected Entry copyEntry() {
    return new RootEntry();
  }

  public boolean hasEntry(Path path) {
    return findEntry(path) != null;
  }

  protected Entry findEntry(Path path) {
    return findEntry(new PathMatcher(path));
  }

  public boolean hasEntry(Integer id) {
    return findEntry(new IdMatcher(id)) != null;
  }

  public Entry getEntry(Path path) {
    return getEntry(new PathMatcher(path));
  }

  public Entry getEntry(Integer id) {
    return getEntry(new IdMatcher(id));
  }

  private Entry getEntry(Matcher m) {
    Entry result = findEntry(m);
    if (result == null) throw new LocalVcsException();
    return result;
  }

  protected void doCreateFile(Path path, String content) {
    addEntry(path.getParent(),
             new FileEntry(getNextObjectId(), path.getName(), content));
  }

  protected void doCreateDirectory(Path path) {
    addEntry(path.getParent(),
             new DirectoryEntry(getNextObjectId(), path.getName()));
  }

  protected void doChangeFileContent(Path path, String content) {
    Entry oldEntry = getEntry(path);
    Entry newEntry = new FileEntry(oldEntry.getObjectId(),
                                   path.getName(),
                                   content);

    removeEntry(path);
    addEntry(path.getParent(), newEntry);
  }

  protected void doRename(Path path, String newName) {
    if (newName.equals(path.getName())) return;

    Entry oldEntry = getEntry(path);
    Entry newEntry = oldEntry.renamed(newName);

    removeEntry(path);
    addEntry(path.getParent(), newEntry);
  }

  public void doMove(Path path, Path parent) {
    Entry e = getEntry(path);

    removeEntry(path);
    addEntry(parent, e);
  }

  protected void doDelete(Path path) {
    removeEntry(path);
  }

  private Integer getNextObjectId() {
    return Snapshot.ourIdGenerator.getNextObjectId();
  }

  public void addEntry(Path parent, Entry entry) {
    // todo quite ugly
    if (parent == null) addChild(entry);
    else getEntry(parent).addChild(entry);
  }

  public void removeEntry(Path path) {
    Entry parent = path.isRoot() ? this : getEntry(path.getParent());
    parent.removeChild(getEntry(path));
  }

  private static class PathMatcher implements Matcher {
    // todo optimize it
    private Path myPath;

    public PathMatcher(Path p) { myPath = p; }

    public boolean matches(Entry e) { return myPath.equals(e.getPath()); }
  }

  private static class IdMatcher implements Matcher {
    private Integer myId;

    public IdMatcher(Integer id) { myId = id; }

    public boolean matches(Entry e) { return myId.equals(e.myObjectId); }
  }
}
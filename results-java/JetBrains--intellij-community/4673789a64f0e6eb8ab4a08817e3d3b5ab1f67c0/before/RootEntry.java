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
package com.intellij.localvcs.core;

import com.intellij.localvcs.core.storage.Content;
import com.intellij.localvcs.core.storage.Storage;
import com.intellij.localvcs.core.storage.StoredContent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestStorage extends Storage {
  private Map<Integer, byte[]> myContents = new HashMap<Integer, byte[]>();

  public TestStorage() {
    super(null);
  }

  @Override
  protected void initStorage() {
  }

  @Override
  public void save() {
  }

  @Override
  public LocalVcs.Memento load() {
    return new LocalVcs.Memento();
  }

  @Override
  public void store(LocalVcs.Memento m) {
  }

  @Override
  public Content storeContent(byte[] bytes) {
    int id = myContents.size();
    myContents.put(id, bytes);
    return new StoredContent(this, id);
  }

  @Override
  protected byte[] loadContentData(int id) throws IOException {
    return myContents.get(id);
  }
}
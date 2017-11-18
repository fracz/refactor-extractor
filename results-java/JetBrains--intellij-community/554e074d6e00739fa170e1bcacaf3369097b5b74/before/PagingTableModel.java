/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.debugger.array;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBViewport;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.Queue;

import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class PagingTableModel extends AbstractTableModel {
  private static final int CHUNK_COL_SIZE = 50;
  private static final int CHUNK_ROW_SIZE = 50;
  private static final int DEFAULT_MAX_CACHED_SIZE = 100;
  public static final String EMPTY_CELL_VALUE = "...";

  private HashMap<String, Object[][]> myCachedData = new HashMap<String, Object[][]>();
  private SortedSet<ArrayChunk> myPendingSet = new TreeSet<ArrayChunk>();
  private Queue<String> cachedChunkKeys = new Queue<String>(DEFAULT_MAX_CACHED_SIZE + 1);

  private ArrayChunk evaluatedChunk;

  private boolean myRendered;
  private int myRows = 0;
  private int myColumns = 0;
  private NumpyArrayValueProvider myProvider;
  private ArrayChunk myFullChunk;


  public PagingTableModel(int rows, int columns, boolean rendered, NumpyArrayValueProvider provider, ArrayChunk fullChunk) {
    myRows = rows;
    myColumns = columns;
    myRendered = rendered;
    myProvider = provider;
    myFullChunk = fullChunk;
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    if (getValueAt(row, column).equals(EMPTY_CELL_VALUE)) {
      return false;
    }
    return true;
  }

  public Object getValueAt(int row, int col) {
    //prevent data evaluation for full table
    if (row == (getRowCount() - 1) && col == (getColumnCount() - 1) && !myRendered) {
      myRendered = true;
      return EMPTY_CELL_VALUE;
    }

    if (!myRendered) {
      return EMPTY_CELL_VALUE;
    }

    String key = formMapKey(row, col);
    if (!myCachedData.containsKey(key)) {
      scheduleChunkAt(row, col);
      return EMPTY_CELL_VALUE;
    }

    return myCachedData.get(key)[row % CHUNK_ROW_SIZE][col % CHUNK_COL_SIZE];
  }

  private static String formMapKey(int row, int col) {
    return "[" + getPageRowStart(row) + "," + getPageColStart(col) + "]";
  }

  private static int getPageRowStart(int rowOffset) {
    return rowOffset - (rowOffset % CHUNK_ROW_SIZE);
  }

  private static int getPageColStart(int colOffset) {
    return colOffset - (colOffset % CHUNK_COL_SIZE);
  }

  private void scheduleChunkAt(int rOffset, int cOffset) {
    if (isPending(rOffset, cOffset)) {
      return;
    }

    int startROffset = getPageRowStart(rOffset);
    int rLength = Math.min(CHUNK_ROW_SIZE, myRows - startROffset);

    int startCOffset = getPageColStart(cOffset);
    int cLength = Math.min(CHUNK_COL_SIZE, myColumns - startCOffset);

    scheduleLoadData(startROffset, rLength, startCOffset, cLength);
  }

  private boolean isPending(int rOffset, int cOffset) {
    int sz = myPendingSet.size();
    if (sz == 0) return false;
    if (sz == 1) {
      // special case (for speed)
      ArrayChunk seg = myPendingSet.first();
      return seg.contains(rOffset, cOffset);
    }

    ArrayChunk lo = createChunk(0, 0, getPageRowStart(rOffset), getPageColStart(cOffset));
    ArrayChunk hi = createChunk(0, 0, getPageRowStart(rOffset + CHUNK_ROW_SIZE), getPageColStart(cOffset + CHUNK_COL_SIZE));

    for (ArrayChunk seg : myPendingSet.subSet(lo, hi)) {
      if (seg.contains(rOffset, cOffset)) return true;
    }
    return false;
  }

  protected abstract ArrayChunk createChunk(int rows, int columns, int rOffset, int cOffset);

  protected abstract Runnable getDataEvaluator(final ArrayChunk chunk);

  public void runNextLoadingTask() {
    if (evaluatedChunk != null) {
      myPendingSet.remove(evaluatedChunk);
      evaluatedChunk = null;
    }

    if (myPendingSet.size() > 0) {
      evaluatedChunk = myPendingSet.first();
      ApplicationManager.getApplication().executeOnPooledThread(getDataEvaluator(evaluatedChunk));
      myProvider.setBusy(true);
    }
    else {
      myProvider.completeCurrentLoad();
    }
  }

  private void scheduleLoadData(final int rOffset, final int rLength, final int cOffset, final int cLength) {
    final ArrayChunk segment = createChunk(rLength, cLength, rOffset, cOffset);
    myPendingSet.add(segment);

    if (evaluatedChunk == null) {
      runNextLoadingTask();
    }
  }

  protected void addDataInCache(int rOffset, int cOffset, Object[][] newData) {
    String key = formMapKey(rOffset, cOffset);
    myCachedData.put(key, newData);
    cachedChunkKeys.addLast(key);

    if (myCachedData.size() == DEFAULT_MAX_CACHED_SIZE) {
      String old = cachedChunkKeys.pullFirst();
      myCachedData.remove(old);
    }
  }

  public int getColumnCount() {
    return myColumns;
  }

  public String getColumnName(int col) {
    return String.valueOf(col);
  }

  public int getRowCount() {
    return myRows;
  }

  public void clearCached() {
    myCachedData = new HashMap<String, Object[][]>();
    myPendingSet = new TreeSet<ArrayChunk>();
    cachedChunkKeys = new Queue<String>(DEFAULT_MAX_CACHED_SIZE + 1);
    evaluatedChunk = null;
  }

  public static class LazyViewport extends JBViewport {
    public static JBScrollPane createLazyScrollPaneFor(Component view) {
      LazyViewport vp = new LazyViewport();
      vp.setView(view);
      JBScrollPane scrollpane = new JBScrollPane();
      scrollpane.setViewport(vp);
      return scrollpane;
    }

    public void setViewPosition(Point p) {
      Component parent = getParent();
      if (parent instanceof JBScrollPane &&
          (((JBScrollPane)parent).getVerticalScrollBar().getValueIsAdjusting() ||
           ((JBScrollPane)parent).getHorizontalScrollBar().getValueIsAdjusting())) {
        return;
      }
      super.setViewPosition(p);
    }
  }

  public void forcedChange(int row, int col, Object value) {
    String key = formMapKey(row, col);
    if (myCachedData.containsKey(key)) {
      Object[][] cachedData = myCachedData.get(key);
      cachedData[row - getPageRowStart(row)][col - getPageColStart(col)] = value;
      myCachedData.put(key, cachedData);
    }
    else {
      throw new IllegalArgumentException("Forced to change empty cell in " + row + " row and " + col + "column.");
    }
  }

  public ArrayChunk getFullChunk() {
    return myFullChunk;
  }
}
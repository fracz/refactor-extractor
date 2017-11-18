/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 15.11.2006
 * Time: 20:20:15
 */
package com.intellij.openapi.diff.impl.patch;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class PatchHunk {
  private int myStartLineBefore;
  private int myEndLineBefore;
  private int myStartLineAfter;
  private int myEndLineAfter;
  private List<PatchLine> myLines = new ArrayList<PatchLine>();

  public PatchHunk(final int startLineBefore, final int endLineBefore, final int startLineAfter, final int endLineAfter) {
    myStartLineBefore = startLineBefore;
    myEndLineBefore = endLineBefore;
    myStartLineAfter = startLineAfter;
    myEndLineAfter = endLineAfter;
  }

  public int getStartLineBefore() {
    return myStartLineBefore;
  }

  public int getEndLineBefore() {
    return myEndLineBefore;
  }

  public int getStartLineAfter() {
    return myStartLineAfter;
  }

  public int getEndLineAfter() {
    return myEndLineAfter;
  }

  public void addLine(final PatchLine line) {
    myLines.add(line);
  }

  public List<PatchLine> getLines() {
    return Collections.unmodifiableList(myLines);
  }

  public ApplyPatchStatus apply(final List<String> lines) throws ApplyPatchException {
    ApplyPatchStatus result = null;
    int curLine = findStartLine(lines);
    for(PatchLine line: myLines) {
      switch (line.getType()) {
        case CONTEXT:
          if (!line.getText().equals(lines.get(curLine))) {
            throw new ApplyPatchException("Context mismatch");
          }
          curLine++;
          break;

        case ADD:
          if (curLine < lines.size() && lines.get(curLine).equals(line.getText())) {
            result = ApplyPatchStatus.and(result, ApplyPatchStatus.ALREADY_APPLIED);
          }
          else {
            lines.add(curLine, line.getText());
            result = ApplyPatchStatus.and(result, ApplyPatchStatus.SUCCESS);
          }
          curLine++;
          break;

        case REMOVE:
          if (!line.getText().equals(lines.get(curLine))) {
            // we'll get a context mismatch exception later if it's actually a conflict and not an already applied line
            result = ApplyPatchStatus.and(result, ApplyPatchStatus.ALREADY_APPLIED);
          }
          else {
            lines.remove(curLine);
            result = ApplyPatchStatus.and(result, ApplyPatchStatus.SUCCESS);
          }
          break;
      }
    }
    if (result != null) {
      return result;
    }
    return ApplyPatchStatus.SUCCESS;
  }

  private int findStartLine(final List<String> lines) throws ApplyPatchException {
    int totalContextLines = countContextLines();
    if (getLinesMatchingContext(lines, myStartLineBefore-1) == totalContextLines) {
      return myStartLineBefore-1;
    }
    int maxContextStartLine = -1;
    int maxContextLines = 0;
    for(int i=0;i< lines.size(); i++) {
      int contextLines = getLinesMatchingContext(lines, i);
      if (contextLines == totalContextLines) {
        return i;
      }
      if (contextLines > maxContextLines) {
        maxContextLines = contextLines;
        maxContextStartLine = i;
      }
    }
    if (maxContextLines < 2) {
      throw new ApplyPatchException("couldn't find context");
    }
    return maxContextStartLine;
  }

  private int countContextLines() {
    int count = 0;
    for(PatchLine line: myLines) {
      if (line.getType() == PatchLine.Type.CONTEXT || line.getType() == PatchLine.Type.REMOVE) {
        count++;
      }
    }
    return count;
  }

  private int getLinesMatchingContext(final List<String> lines, int startLine) {
    int count = 0;
    for(PatchLine line: myLines) {
      PatchLine.Type type = line.getType();
      if (type == PatchLine.Type.REMOVE || type == PatchLine.Type.CONTEXT) {
        // TODO: smarter algorithm (search outward from non-context lines)
        if (startLine >= lines.size() || !line.getText().equals(lines.get(startLine))) {
          return count;
        }
        count++;
        startLine++;
      }
    }
    return count;
  }

  public boolean isNewContent() {
    return myStartLineBefore == 0 && myEndLineBefore == 0;
  }

  public boolean isDeletedContent() {
    return myStartLineAfter == 0 && myEndLineAfter == 0;
  }

  public String getText() {
    StringBuilder builder = new StringBuilder();
    for(PatchLine line: myLines) {
      builder.append(line.getText()).append("\n");
    }
    return builder.toString();
  }

  public boolean isNoNewLineAtEnd() {
    if (myLines.size() == 0) {
      return false;
    }
    return myLines.get(myLines.size()-1).isSuppressNewLine();
  }
}
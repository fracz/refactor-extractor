/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 15.11.2006
 * Time: 17:53:24
 */
package com.intellij.openapi.diff.impl.patch;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.util.text.LineTokenizer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilePatch {
  private String myBeforeName;
  private String myAfterName;
  private String myBeforeVersionId;
  private String myAfterVersionId;
  private List<PatchHunk> myHunks = new ArrayList<PatchHunk>();

  public String getBeforeName() {
    return myBeforeName;
  }

  public String getAfterName() {
    return myAfterName;
  }

  public String getBeforeFileName() {
    String[] pathNameComponents = myBeforeName.split("/");
    return pathNameComponents [pathNameComponents.length-1];
  }

  public void setBeforeName(final String fileName) {
    myBeforeName = fileName;
  }

  public void setAfterName(final String fileName) {
    myAfterName = fileName;
  }

  public String getBeforeVersionId() {
    return myBeforeVersionId;
  }

  public void setBeforeVersionId(final String beforeVersionId) {
    myBeforeVersionId = beforeVersionId;
  }

  public String getAfterVersionId() {
    return myAfterVersionId;
  }

  public void setAfterVersionId(final String afterVersionId) {
    myAfterVersionId = afterVersionId;
  }

  public void addHunk(final PatchHunk hunk) {
    myHunks.add(hunk);
  }

  public List<PatchHunk> getHunks() {
    return Collections.unmodifiableList(myHunks);
  }

  public ApplyPatchStatus apply(ApplyPatchContext context)
    throws ApplyPatchException, IOException {
    VirtualFile fileToPatch = findFileToPatch(context);

    if (fileToPatch == null) {
      throw new ApplyPatchException("Cannot find file to patch: " + myBeforeName);
    }

    return apply(fileToPatch);
  }

  public ApplyPatchStatus apply(final VirtualFile fileToPatch) throws IOException, ApplyPatchException {
    if (isNewFile()) {
      if (fileToPatch.findChild(getBeforeFileName()) != null) {
        throw new ApplyPatchException("File " + getBeforeFileName() + " already exists");
      }
      VirtualFile newFile = fileToPatch.createChildData(this, getBeforeFileName());
      final Document document = FileDocumentManager.getInstance().getDocument(newFile);
      document.setText(getNewFileText());
      FileDocumentManager.getInstance().saveDocument(document);
    }
    else if (isDeletedFile()) {
      fileToPatch.delete(this);
    }
    else {
      byte[] fileContents = fileToPatch.contentsToByteArray();
      CharSequence text = LoadTextUtil.getTextByBinaryPresentation(fileContents, fileToPatch);
      StringBuilder newText = new StringBuilder();
      ApplyPatchStatus status = applyModifications(text, newText);
      if (status != ApplyPatchStatus.ALREADY_APPLIED) {
        final Document document = FileDocumentManager.getInstance().getDocument(fileToPatch);
        document.setText(newText.toString());
        FileDocumentManager.getInstance().saveDocument(document);
      }
      return status;
    }
    return ApplyPatchStatus.SUCCESS;
  }

  public ApplyPatchStatus applyModifications(final CharSequence text, final StringBuilder newText) throws ApplyPatchException {
    if (myHunks.size() == 0) {
      return ApplyPatchStatus.SUCCESS;
    }
    List<String> lines = new ArrayList<String>();
    Collections.addAll(lines, LineTokenizer.tokenize(text, false));
    ApplyPatchStatus result = null;
    for(PatchHunk hunk: myHunks) {
      result = ApplyPatchStatus.and(result, hunk.apply(lines));
    }
    for(int i=0; i<lines.size(); i++) {
      newText.append(lines.get(i));
      if (i < lines.size()-1 || !myHunks.get(myHunks.size()-1).isNoNewLineAtEnd()) {
        newText.append("\n");
      }
    }
    return result;
  }

  @Nullable
  public VirtualFile findFileToPatch(@NotNull ApplyPatchContext context) throws IOException {
    return findPatchTarget(context, myBeforeName, myAfterName, isNewFile());
  }

  @Nullable
  public static VirtualFile findPatchTarget(final ApplyPatchContext context, final String beforeName, final String afterName,
                                            final boolean isNewFile) throws IOException {
    VirtualFile file = null;
    if (beforeName != null) {
      file = findFileToPatchByName(context, beforeName, isNewFile);
    }
    if (file == null) {
      file = findFileToPatchByName(context, afterName, isNewFile);
    }
    else if (context.isAllowRename() && !beforeName.equals(afterName)) {
      String[] beforeNameComponents = beforeName.split("/");
      String[] afterNameComponents = afterName.split("/");
      if (!beforeNameComponents [beforeNameComponents.length-1].equals(afterNameComponents [afterNameComponents.length-1])) {
        file.rename(null, afterNameComponents [afterNameComponents.length-1]);
      }
      boolean pathChanged = (beforeNameComponents.length != afterNameComponents.length);
      if (!pathChanged) {
        for(int i=context.getSkipTopDirs(); i<afterNameComponents.length-1; i++) {
          if (!beforeNameComponents [i].equals(afterNameComponents [i])) {
            pathChanged = true;
            break;
          }
        }
      }
      if (pathChanged) {
        VirtualFile moveTarget = findFileToPatchByComponents(context, afterNameComponents, afterNameComponents.length-1);
        if (moveTarget == null) {
          return null;
        }
        file.move(null, moveTarget);
      }
    }
    return file;
  }

  @Nullable
  private static VirtualFile findFileToPatchByName(@NotNull ApplyPatchContext context, final String fileName,
                                                   boolean isNewFile) {
    String[] pathNameComponents = fileName.split("/");
    int lastComponentToFind = isNewFile ? pathNameComponents.length-1 : pathNameComponents.length;
    return findFileToPatchByComponents(context, pathNameComponents, lastComponentToFind);
  }

  @Nullable
  private static VirtualFile findFileToPatchByComponents(ApplyPatchContext context,
                                                         final String[] pathNameComponents,
                                                         final int lastComponentToFind) {
    VirtualFile patchedDir = context.getBaseDir();
    for(int i=context.getSkipTopDirs(); i<lastComponentToFind; i++) {
      VirtualFile nextChild;
      if (pathNameComponents [i].equals("..")) {
        nextChild = patchedDir.getParent();
      }
      else {
        nextChild = patchedDir.findChild(pathNameComponents [i]);
      }
      if (nextChild == null) {
        if (context.isCreateDirectories()) {
          try {
            nextChild = patchedDir.createChildDirectory(null, pathNameComponents [i]);
          }
          catch (IOException e) {
            return null;
          }
        }
        else {
          return null;
        }
      }
      patchedDir = nextChild;
    }
    return patchedDir;
  }

  public boolean isNewFile() {
    return myHunks.size() == 1 && myHunks.get(0).isNewContent();
  }

  public String getNewFileText() {
    return myHunks.get(0).getText();
  }

  public boolean isDeletedFile() {
    return myHunks.size() == 1 && myHunks.get(0).isDeletedContent();
  }
}
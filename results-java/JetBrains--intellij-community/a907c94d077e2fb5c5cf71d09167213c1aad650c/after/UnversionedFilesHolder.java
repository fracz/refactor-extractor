package com.intellij.openapi.vcs.changes;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.peer.PeerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author max
 */
public class UnversionedFilesHolder {
  private List<VirtualFile> myFiles = new ArrayList<VirtualFile>();
  private Project myProject;

  public UnversionedFilesHolder(Project project) {
    myProject = project;
  }

  public synchronized void cleanScope(final VcsDirtyScope scope) {
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        final List<VirtualFile> currentFiles = new ArrayList<VirtualFile>(myFiles);
        for (VirtualFile file : currentFiles) {
          if (fileDropped(file) || scope.belongsTo(PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(file))) {
            myFiles.remove(file);
          }
        }
      }
    });
  }

  private boolean fileDropped(final VirtualFile file) {
    return !file.isValid() || !ProjectRootManager.getInstance(myProject).getFileIndex().isInContent(file);
  }

  public synchronized void addFile(VirtualFile file) {
    myFiles.add(file);
  }

  public synchronized List<VirtualFile> getFiles() {
    return Collections.unmodifiableList(myFiles);
  }

  public synchronized UnversionedFilesHolder copy() {
    final UnversionedFilesHolder copyHolder = new UnversionedFilesHolder(myProject);
    copyHolder.myFiles.addAll(myFiles);
    return copyHolder;
  }

  public synchronized boolean containsFile(final VirtualFile file) {
    return myFiles.contains(file);
  }
}
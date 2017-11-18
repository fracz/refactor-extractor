package org.jetbrains.idea.maven.project.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenAction;

import java.util.Arrays;

public class RemoveManagedFilesAction extends MavenAction {
  @Override
  public void update(AnActionEvent e) {
    Project p = e.getData(PlatformDataKeys.PROJECT);
    VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);

    boolean enabled = files != null && files.length != 0;
    if (enabled) {
      boolean hasManagedFiles = false;
      for (VirtualFile each : files) {
        if (MavenProjectsManager.getInstance(p).isManagedFile(each)) {
          hasManagedFiles = true;
          break;
        }
      }
      enabled = hasManagedFiles;
    }
    e.getPresentation().setEnabled(enabled);
  }

  public void actionPerformed(AnActionEvent e) {
    Project p = e.getData(PlatformDataKeys.PROJECT);
    VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
    MavenProjectsManager.getInstance(p).removeManagedFiles(Arrays.asList(files));
  }
}
package com.intellij.openapi.vcs.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectEx;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.DirtBuilder;
import com.intellij.openapi.vcs.ex.ProjectLevelVcsManagerEx;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.io.File;

/**
 * @author yole
 */
public class ModuleDefaultVcsRootPolicy extends DefaultVcsRootPolicy {
  private final Project myProject;
  private final VirtualFile myBaseDir;
  private final ModuleManager myModuleManager;

  public ModuleDefaultVcsRootPolicy(final Project project) {
    myProject = project;
    myBaseDir = project.getBaseDir();
    myModuleManager = ModuleManager.getInstance(myProject);
  }

  public void addDefaultVcsRoots(final VcsDirectoryMappingList mappingList, final AbstractVcs vcs, final List<VirtualFile> result) {
    if (myBaseDir != null && vcs.getName().equals(mappingList.getVcsFor(myBaseDir)) && vcs.fileIsUnderVcs(new FilePathImpl(myBaseDir))) {
      result.add(myBaseDir);
    }
    final StorageScheme storageScheme = ((ProjectEx) myProject).getStateStore().getStorageScheme();
    if (StorageScheme.DIRECTORY_BASED.equals(storageScheme)) {
      final VirtualFile ideaDir = myBaseDir.findChild(Project.DIRECTORY_STORE_FOLDER);
      if (ideaDir != null && ideaDir.isValid() && ideaDir.isDirectory()) {
        result.add(ideaDir);
      }
    }
    // assertion for read access inside
    final Module[] modules = ApplicationManager.getApplication().runReadAction(new Computable<Module[]>() {
      public Module[] compute() {
        return myModuleManager.getModules();
      }
    });
    for(Module module: modules) {
      final VirtualFile[] files = ModuleRootManager.getInstanceChecked(module).getContentRoots();
      for(VirtualFile file: files) {
        // if we're currently processing moduleAdded notification, getModuleForFile() will return null, so we pass the module
        // explicitly (we know it anyway)
        VcsDirectoryMapping mapping = mappingList.getMappingFor(file, module);
        final String mappingVcs = mapping != null ? mapping.getVcs() : null;
        if (vcs.getName().equals(mappingVcs)) {
          result.add(file);
        }
      }
    }
  }

  public boolean matchesDefaultMapping(final VirtualFile file, final Object matchContext) {
    if (matchContext != null) {
      return true;
    }
    if (myBaseDir != null && VfsUtil.isAncestor(myBaseDir, file, false)) {
      return !ProjectRootManager.getInstance(myProject).getFileIndex().isIgnored(file);
    }
    return false;
  }

  @Nullable
  public Object getMatchContext(final VirtualFile file) {
    return ModuleUtil.findModuleForFile(file, myProject);
  }

  @Nullable
  public VirtualFile getVcsRootFor(final VirtualFile file) {
    if (myBaseDir != null && ExcludedFileIndex.getInstance(myProject).isValidAncestor(myBaseDir, file)) {
      return myBaseDir;
    }
    final VirtualFile contentRoot = ProjectRootManager.getInstance(myProject).getFileIndex().getContentRootForFile(file);
    if (contentRoot != null) {
      return contentRoot;
    }
    final StorageScheme storageScheme = ((ProjectEx) myProject).getStateStore().getStorageScheme();
    if (StorageScheme.DIRECTORY_BASED.equals(storageScheme)) {
      final VirtualFile ideaDir = myBaseDir.findChild(Project.DIRECTORY_STORE_FOLDER);
      if (ideaDir != null && ideaDir.isValid() && ideaDir.isDirectory()) {
        if (VfsUtil.isAncestor(ideaDir, file, false)) {
          return ideaDir;
        }
      }
    }
    return null;
  }

  public void markDefaultRootsDirty(final DirtBuilder builder) {
    final VirtualFile baseDir = myProject.getBaseDir();

    final Module[] modules = myModuleManager.getModules();
    final StorageScheme storageScheme = ((ProjectEx) myProject).getStateStore().getStorageScheme();
    if (StorageScheme.DIRECTORY_BASED.equals(storageScheme)) {
      builder.addDirtyDirRecursively(new FilePathImpl(new File(PathUtil.toPresentableUrl(baseDir.getPath()), Project.DIRECTORY_STORE_FOLDER), true));
    }

    for(Module module: modules) {
      final VirtualFile[] files = ModuleRootManager.getInstanceChecked(module).getContentRoots();
      for(VirtualFile file: files) {
        builder.addDirtyDirRecursively(new FilePathImpl(file));
      }
    }

    final ProjectLevelVcsManager plVcsManager = ProjectLevelVcsManager.getInstance(myProject);
    final boolean haveDefaultMapping = ((ProjectLevelVcsManagerEx)plVcsManager).haveDefaultMapping();

    final VcsRoot[] vcsRoots = plVcsManager.getAllVcsRoots();

    for (VcsRoot root : vcsRoots) {
      if (haveDefaultMapping && root.path.equals(baseDir)) {
        builder.addDirtyFile(root);
      }
      else {
        builder.addDirtyDirRecursively(root);
      }
    }
  }
}
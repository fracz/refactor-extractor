
package com.intellij.packageDependencies.ui;

import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.DependencyUISettings;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Icons;

import javax.swing.*;
import java.io.File;
import java.util.Set;

public class DirectoryNode extends PackageDependenciesNode {

  private String myDirName;
  private PsiDirectory myDirectory;

  private DirectoryNode myCompactedDirNode;
  private DirectoryNode myWrapper;

  private boolean myCompactPackages = true;
  private String myFQName = null;

  public DirectoryNode(PsiDirectory aDirectory, boolean compactPackages, boolean showFQName) {
    myDirectory = aDirectory;
    final boolean showModules = DependencyUISettings.getInstance().UI_SHOW_MODULES;
    myDirName = showModules ? aDirectory.getName() : myDirectory.getVirtualFile().getPresentableUrl();
    myCompactPackages = compactPackages;
    if (showFQName) {
      if (!showModules) {
        myFQName = myDirName;
      } else {
        final ProjectFileIndex index = ProjectRootManager.getInstance(myDirectory.getProject()).getFileIndex();
        final VirtualFile directory = myDirectory.getVirtualFile();
        final VirtualFile contentRoot = index.getContentRootForFile(directory);
        if (contentRoot != null) {
          myFQName = VfsUtil.getRelativePath(directory, contentRoot.getParent(), File.separatorChar);
        }
      }
    }
    setUserObject(toString());
  }

  public void fillFiles(Set<PsiFile> set, boolean recursively) {
    super.fillFiles(set, recursively);
    int count = getChildCount();
    for (int i = 0; i < count; i++) {
      PackageDependenciesNode child = (PackageDependenciesNode)getChildAt(i);
      if (child instanceof FileNode || recursively) {
        child.fillFiles(set, true);
      }
    }
  }

  public String toString() {
    if (myFQName != null) return myFQName;
    if (myCompactPackages && myCompactedDirNode != null){
      return myDirName + File.separator + myCompactedDirNode.getDirName();
    }
    return myDirName;
  }

  public String getDirName(){
    if (myDirectory == null || !myDirectory.isValid()) return "";
    if (myCompactPackages && myCompactedDirNode != null){
      return myDirectory.getName() + File.separator + myCompactedDirNode.getDirName();
    }
    return myDirectory.getName();
  }

  public PsiElement getPsiElement() {
    return myDirectory;
  }

  public int getWeight() {
    return 3;
  }

  public boolean equals(Object o) {
    if (isEquals()){
      return super.equals(o);
    }
    if (this == o) return true;
    if (!(o instanceof DirectoryNode)) return false;

    final DirectoryNode packageNode = (DirectoryNode)o;

    if (!toString().equals(packageNode.toString())) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = toString().hashCode();
    return result;
  }

  public Icon getOpenIcon() {
    return Icons.PACKAGE_OPEN_ICON;
  }

  public Icon getClosedIcon() {
    return Icons.PACKAGE_ICON;
  }

  public void setCompactedDirNode(final DirectoryNode compactedDirNode) {
    myCompactedDirNode = compactedDirNode;
    if (myCompactedDirNode != null) {
      myCompactedDirNode.myWrapper = this;
    }
  }

  public DirectoryNode getWrapper() {
    return myWrapper;
  }

  public DirectoryNode getCompactedDirNode() {
    return myCompactPackages ? myCompactedDirNode : null;
  }
}
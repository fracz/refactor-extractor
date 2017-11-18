package com.intellij.lang.properties.psi;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.i18n.DefaultResourceBundleManager;
import com.intellij.lang.properties.PropertiesFilesManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ResourceBundleManager {
  public static final ExtensionPointName<ResourceBundleManager> RESOURCE_BUNDLE_MANAGER = ExtensionPointName.create("com.intellij.resourceBundleManager");
  protected Project myProject;

  protected ResourceBundleManager(final Project project) {
    myProject = project;
  }

  /**
   * By default returns java.util.ResourceBundle class in context JDK
   */
  @Nullable
  public abstract PsiClass getResourceBundle();

  public List<String> suggestPropertiesFiles(){
    Collection<VirtualFile> allPropertiesFiles = PropertiesFilesManager.getInstance().getAllPropertiesFiles();
    List<String> paths = new ArrayList<String>();
    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
    for (VirtualFile virtualFile : allPropertiesFiles) {
      if (projectFileIndex.isInContent(virtualFile)) {
        String path = FileUtil.toSystemDependentName(virtualFile.getPath());
        paths.add(path);
      }
    }
    return paths;
  }

  public abstract String getTemplateName();

  public abstract String getConcatenationTemplateName();

  public abstract boolean isActive(PsiFile context) throws ResourceBundleNotFoundException;

  public abstract boolean canShowJavaCodeInfo();

  @Nullable
  public static ResourceBundleManager getManager(PsiFile context) throws ResourceBundleNotFoundException {
    final Project project = context.getProject();
    final ResourceBundleManager[] managers = project.getExtensions(RESOURCE_BUNDLE_MANAGER);
    for (ResourceBundleManager manager : managers) {
      if (manager.isActive(context)) {
        return manager;
      }
    }
    final DefaultResourceBundleManager manager = new DefaultResourceBundleManager(project);
    return manager.isActive(context) ? manager : null;
  }

  public static class ResourceBundleNotFoundException extends Exception {
    private IntentionAction myFix;

    public ResourceBundleNotFoundException(final String message, IntentionAction setupResourceBundle) {
      super(message);
      myFix = setupResourceBundle;
    }

    public IntentionAction getFix() {
      return myFix;
    }
  }
}
package com.intellij.lang.ant.config.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactProperties;
import com.intellij.packaging.ui.ArtifactPropertiesEditor;
import com.intellij.packaging.ui.PackagingEditorContext;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.lang.ant.config.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author nik
*/
public class AntArtifactPostprocessingProperties extends ArtifactProperties<AntArtifactPostprocessingProperties> {
  private String myFileUrl;
  private String myTargetName;
  private boolean myEnabled;

  public ArtifactPropertiesEditor createEditor(@NotNull PackagingEditorContext context) {
    return new AntArtifactPostprocessingPropertiesEditor(this, context.getProject());
  }

  public AntArtifactPostprocessingProperties getState() {
    return this;
  }

  @Override
  public void onBuildFinished(@NotNull final Project project, @NotNull Artifact artifact) {
    if (myEnabled) {
      final AntBuildTarget target = findTarget(AntConfiguration.getInstance(project));
      if (target != null) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            target.run(MapDataContext.singleData(DataKeys.PROJECT.getName(), project), AntBuildListener.NULL);
          }
        });
      }
    }
  }

  public void loadState(AntArtifactPostprocessingProperties state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @Tag("file")
  public String getFileUrl() {
    return myFileUrl;
  }

  @Tag("target")
  public String getTargetName() {
    return myTargetName;
  }

  @Attribute("enabled")
  public boolean isEnabled() {
    return myEnabled;
  }

  public void setEnabled(boolean enabled) {
    myEnabled = enabled;
  }

  public void setFileUrl(String fileUrl) {
    myFileUrl = fileUrl;
  }

  public void setTargetName(String targetName) {
    myTargetName = targetName;
  }

  @Nullable
  public AntBuildTarget findTarget(final AntConfiguration antConfiguration) {
    if (myFileUrl == null || myTargetName == null) return null;

    final AntBuildFile[] buildFiles = antConfiguration.getBuildFiles();
    for (AntBuildFile buildFile : buildFiles) {
      final VirtualFile file = buildFile.getVirtualFile();
      if (file != null && file.getUrl().equals(myFileUrl)) {
        final AntBuildModel buildModel = buildFile.getModel();
        return buildModel != null ? buildModel.findTarget(myTargetName) : null;
      }
    }
    return null;
  }
}
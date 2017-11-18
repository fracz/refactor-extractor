package com.intellij.packaging.ui;

import com.intellij.packaging.artifacts.ArtifactType;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.elements.PackagingElementResolvingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author nik
 */
public interface ArtifactEditorContext extends PackagingElementResolvingContext {

  void queueValidation();

  @NotNull
  ArtifactType getArtifactType();

  @NotNull
  ModifiableArtifactModel getModifiableArtifactModel();

  @NotNull
  ManifestFileConfiguration getManifestFile(CompositePackagingElement<?> element, ArtifactType artifactType);

  CompositePackagingElement<?> getRootElement(@NotNull Artifact originalArtifact);

  void ensureRootIsWritable(@NotNull Artifact originalArtifact);

  ArtifactEditor getOrCreateEditor(Artifact originalArtifact);
}
package com.intellij.openapi.roots.ui.configuration.artifacts.sourceItems;

import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactPointerManager;
import com.intellij.packaging.artifacts.ArtifactPointer;
import com.intellij.packaging.elements.PackagingElement;
import com.intellij.packaging.elements.PackagingElementFactory;
import com.intellij.packaging.impl.ui.ArtifactElementPresentation;
import com.intellij.packaging.ui.PackagingEditorContext;
import com.intellij.packaging.ui.PackagingSourceItem;
import com.intellij.packaging.ui.SourceItemPresentation;
import com.intellij.packaging.ui.SourceItemWeights;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author nik
 */
public class ArtifactSourceItem extends PackagingSourceItem {
  private final Artifact myArtifact;

  public ArtifactSourceItem(@NotNull Artifact artifact) {
    myArtifact = artifact;
  }

  public SourceItemPresentation createPresentation(@NotNull PackagingEditorContext context) {
    final ArtifactPointer pointer = ArtifactPointerManager.getInstance(context.getProject()).create(myArtifact);
    return new DelegatedSourceItemPresentation(new ArtifactElementPresentation(pointer, context)) {
      @Override
      public int getWeight() {
        return SourceItemWeights.ARTIFACT_WEIGHT;
      }
    };
  }

  @NotNull
  public List<? extends PackagingElement<?>> createElements(@NotNull PackagingEditorContext context) {
    return Collections.singletonList(PackagingElementFactory.getInstance().createArtifactElement(myArtifact, context.getProject()));
  }

  public boolean equals(Object obj) {
    return obj instanceof ArtifactSourceItem && myArtifact.equals(((ArtifactSourceItem)obj).myArtifact);
  }

  public int hashCode() {
    return myArtifact.hashCode();
  }
}
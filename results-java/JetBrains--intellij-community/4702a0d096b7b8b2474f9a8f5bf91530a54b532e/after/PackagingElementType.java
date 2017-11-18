package com.intellij.packaging.elements;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.ui.PackagingEditorContext;
import com.intellij.packaging.ui.PackagingElementPropertiesPanel;
import com.intellij.packaging.ui.ArtifactEditorContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * @author nik
 */
public abstract class PackagingElementType<E extends PackagingElement<?>> {
  public static final ExtensionPointName<PackagingElementType> EP_NAME = ExtensionPointName.create("com.intellij.packaging.elementType");
  private final String myId;
  private final String myPresentableName;

  protected PackagingElementType(@NotNull @NonNls String id, @NotNull String presentableName) {
    myId = id;
    myPresentableName = presentableName;
  }

  public final String getId() {
    return myId;
  }

  public String getPresentableName() {
    return myPresentableName;
  }

  @Nullable
  public Icon getCreateElementIcon() {
    return null;
  }

  @NotNull
  public abstract List<? extends E> createWithDialog(@NotNull PackagingEditorContext context, Artifact artifact,
                                                     CompositePackagingElement<?> parent);

  @NotNull
  public abstract E createEmpty();

  protected static <T extends PackagingElementType<?>> T getInstance(final Class<T> aClass) {
    for (PackagingElementType type : Extensions.getExtensions(EP_NAME)) {
      if (aClass.isInstance(type)) {
        return aClass.cast(type);
      }
    }
    throw new AssertionError();
  }

  @Nullable
  public PackagingElementPropertiesPanel<E> createElementPropertiesPanel(ArtifactEditorContext context) {
    return null;
  }
}
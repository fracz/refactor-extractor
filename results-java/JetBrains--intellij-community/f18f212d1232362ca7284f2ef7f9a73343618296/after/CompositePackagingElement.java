package com.intellij.packaging.elements;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author nik
 */
public abstract class CompositePackagingElement<S> extends PackagingElement<S> {
  private final List<PackagingElement<?>> myChildren = new ArrayList<PackagingElement<?>>();

  protected CompositePackagingElement(PackagingElementType type) {
    super(type);
  }

  public void addChild(@NotNull PackagingElement<?> child) {
    myChildren.add(child);
  }

  public void addChildren(Collection<? extends PackagingElement<?>> children) {
    myChildren.addAll(children);
  }

  public void removeChild(@NotNull PackagingElement<?> child) {
    myChildren.remove(child);
  }

  public List<PackagingElement<?>> getChildren() {
    return myChildren;
  }

  public abstract String getName();

  public abstract void rename(@NotNull String newName);

  public boolean canBeMergedWith(@NotNull PackagingElement<?> element) {
    return false;
  }
}
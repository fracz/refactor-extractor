/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.intellij.util.xml;

import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.reflect.DomGenericInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * @author peter
 */
public abstract class DomManager {

  public static DomManager getDomManager(Project project) {
    return project.getComponent(DomManager.class);
  }

  public abstract Project getProject();

  @NotNull
  public abstract <T extends DomElement> DomFileElement<T> getFileElement(XmlFile file, Class<T> aClass, String rootTagName);

  public abstract void addDomEventListener(DomEventListener listener);

  public abstract void removeDomEventListener(DomEventListener listener);

  public abstract DomGenericInfo getGenericInfo(Type type);

  public abstract <T extends DomElement> void registerImplementation(Class<T> domElementClass, Class<? extends T> implementationClass);

  @Nullable
  public abstract DomElement getDomElement(final XmlTag tag);

  public abstract void registerPresentationFactory(DomElementPresentationFactory factory);

  @NotNull
  public abstract DomElementPresentation getDomElementPresentation(final DomElement element);

}
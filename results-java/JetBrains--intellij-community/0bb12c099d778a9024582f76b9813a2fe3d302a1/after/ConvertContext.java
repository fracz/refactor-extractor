/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.intellij.util.xml;

import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.PsiClass;

/**
 * @author peter
 */
public class ConvertContext {
  private final XmlTag myTag;
  private final XmlFile myFile;

  public ConvertContext(final XmlFile file, final XmlTag tag) {
    myFile = file;
    myTag = tag;
  }

  public final PsiClass findClass(String name) {
    final XmlFile file = getFile();
    return file.getManager().findClass(name, file.getResolveScope());
  }

  public final XmlTag getTag() {
    return myTag;
  }

  public final XmlFile getFile() {
    return myFile;
  }


}
package com.intellij.lang.ant;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import com.intellij.pom.xml.events.XmlChange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

public class AntLanguageExtension implements LanguageExtension {

  public boolean isRelevantForFile(final PsiFile psi) {
      if(!(psi instanceof XmlFile)) return false;
      if(!AntFileType.DEFAULT_EXTENSION.equals(psi.getViewProvider().getVirtualFile().getExtension())) return false;
      final XmlFile xmlFile = (XmlFile)psi;
      final XmlTag tag = xmlFile.getDocument().getRootTag();
      if(tag == null) return false;
      if ("project".equalsIgnoreCase(tag.getName()) && tag.getContext() instanceof XmlDocument) {
        return true;
      }
      return false;
    }

    public Language getLanguage() {
      return AntSupport.getLanguage();
    }

    public boolean isAffectedByChange(final XmlChange xmlChange) {
      return true;
    }
  }
package com.intellij.structuralsearch.duplicates;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene.Kudelevsky
 */
public class XmlDuplicatesProfile extends SSRDuplicatesProfile {
  private static final TokenSet LITERALS = TokenSet.create(XmlTokenType.XML_DATA_CHARACTERS, XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN);

  @Override
  protected boolean isMyLanguage(@NotNull Language language) {
    return language instanceof XMLLanguage;
  }

  @Override
  public int getNodeCost(@NotNull PsiElement element) {
    if (element instanceof XmlTag) {
      return 2;
    }
    else if (element instanceof XmlAttribute) {
      return 1;
    }
    return 0;
  }

  @Override
  public TokenSet getLiterals() {
    return LITERALS;
  }
}
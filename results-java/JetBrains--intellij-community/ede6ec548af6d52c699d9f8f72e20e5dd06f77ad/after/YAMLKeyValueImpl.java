package org.jetbrains.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import javax.swing.*;

/**
 * @author oleg
 */
public class YAMLKeyValueImpl extends YAMLPsiElementImpl implements YAMLKeyValue {
  public YAMLKeyValueImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "YAML key value";
  }

  public PsiElement getKey() {
    return getNode().getChildren(TokenSet.create(YAMLTokenTypes.SCALAR_KEY))[0].getPsi();
  }

  @Override
  public String getName() {
    return getKeyText();
  }

  public String getKeyText() {
    final String text = getKey().getText();
    return text.substring(0, text.length()-1);
  }

  public PsiElement getValue() {
    PsiElement element = getKey().getNextSibling();
    while (element != null &&
           !(element instanceof YAMLCompoundValue || YAMLTokenTypes.SCALAR_VALUES.contains(element.getNode().getElementType()))){
      element = element.getNextSibling();
    }
    return element;
  }

  public String getValueText() {
    final PsiElement value = getValue();
    if (value == null){
      return "";
    }
    String text = value.getText();
    if (text.startsWith("'") || text.startsWith("\"")){
      text = text.substring(1);
    }
    if (text.endsWith("'") || text.endsWith("\"")){
      text = text.substring(0, text.length() - 1);
    }
    return text;
  }

  @Override
  public ItemPresentation getPresentation() {
    final YAMLFile yamlFile = (YAMLFile)getContainingFile();
    final PsiElement value = getValue();
    return new ItemPresentation() {
      public String getPresentableText() {
        if (YAMLUtil.isScalarValue(value)){
          return getValueText();
        }
        return getName();
      }

      public String getLocationString() {
        return "[" + yamlFile.getName() + "]";
      }

      public Icon getIcon(boolean open) {
        return Icons.PROPERTY_ICON;
      }

      public TextAttributesKey getTextAttributesKey() {
        return null;
      }
    };
  }

  public PsiElement setName(@NonNls @NotNull String newName) throws IncorrectOperationException {
    return YAMLUtil.rename(this, newName);
  }

  public String getValueIndent() {
    final PsiElement value = getValue();
    if (value != null){
      @SuppressWarnings({"ConstantConditions"})
      final ASTNode node = value.getNode().getTreePrev();
      final IElementType type = node.getElementType();
      if (type == YAMLTokenTypes.WHITESPACE || type == YAMLTokenTypes.INDENT){
        return node.getText();
      }
    }
    return "";
  }
}
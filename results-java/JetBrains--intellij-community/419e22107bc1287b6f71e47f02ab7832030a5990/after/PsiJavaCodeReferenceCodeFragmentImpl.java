package com.intellij.psi.impl.source;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaCodeReferenceCodeFragment;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.impl.source.parsing.ChameleonTransforming;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.ElementType;

/**
 * @author ven
 */
public class PsiJavaCodeReferenceCodeFragmentImpl extends PsiCodeFragmentImpl implements PsiJavaCodeReferenceCodeFragment {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.PsiJavaCodeReferenceCodeFragmentImpl");

  public PsiJavaCodeReferenceCodeFragmentImpl(final Project project,
                                              final boolean isPhysical,
                                              final String name,
                                              final CharSequence text) {
    super(project, REFERENCE_TEXT, isPhysical, name, text);
  }

  public PsiJavaCodeReferenceElement getReferenceElement() {
    final CompositeElement treeElement = calcTreeElement();
    ChameleonTransforming.transformChildren(treeElement);
    LOG.assertTrue (treeElement.getFirstChildNode().getElementType() == ElementType.JAVA_CODE_REFERENCE);
    return (PsiJavaCodeReferenceElement)SourceTreeToPsiMap.treeElementToPsi(treeElement.getFirstChildNode());
  }
}
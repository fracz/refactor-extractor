package com.jetbrains.python.validation;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
import com.intellij.psi.ResolveResult;
import com.jetbrains.python.PyHighlighter;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyResolveUtil;
import com.jetbrains.python.psi.impl.PyBuiltinCache;

/**
 * Marks built-in names.
 * User: dcheryasov
 * Date: Jan 10, 2009 12:17:15 PM
 */
public class PyBuiltinAnnotator extends PyAnnotator {
  @Override
  public void visitPyReferenceExpression(PyReferenceExpression node) {
    if (PyNames.UnderscoredNames.contains(node.getName())) {
      // things like __len__
      if (
        (node.getQualifier() != null) // foo.__len__
        || (PyResolveUtil.getConcealingParent(node) instanceof PyClass) // class Foo: ... __len__ = myLenImpl
      ) {
        final ASTNode astNode = node.getNode();
        if (astNode != null) {
          ASTNode tgt = astNode.findChildByType(PyTokenTypes.IDENTIFIER); // only the id, not all qualifiers subtree
          if (tgt != null) {
            Annotation ann = getHolder().createInfoAnnotation(tgt, null);
            ann.setTextAttributes(PyHighlighter.PY_PREDEFINED_USAGE);
          }
        }
      }
    }
    else if (node.getQualifier() == null) {
      // things like len()
      for (ResolveResult resolved : node.multiResolve(false)) { // things like constructors give multiple results
        if (PyBuiltinCache.hasInBuiltins(resolved.getElement())) {
          Annotation ann = getHolder().createInfoAnnotation(node, null);
          ann.setTextAttributes(PyHighlighter.PY_BUILTIN_NAME);
          break;
        }
      }
    }
  }

}
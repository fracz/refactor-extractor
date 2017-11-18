package com.jetbrains.python.psi.impl;

import com.google.common.collect.Lists;
import com.intellij.extapi.psi.ASTDelegatePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author max
 */
public class PyPsiUtils {
  public static final Key<Pair<PsiElement, TextRange>> SELECTION_BREAKS_AST_NODE =
    new Key<Pair<PsiElement, TextRange>>("python.selection.breaks.ast.node");
  private static final Logger LOG = Logger.getInstance(PyPsiUtils.class.getName());

  private PyPsiUtils() {
  }

  @NotNull
  protected static <T extends PyElement> T[] nodesToPsi(ASTNode[] nodes, T[] array) {
    T[] psiElements = (T[])Array.newInstance(array.getClass().getComponentType(), nodes.length);
    for (int i = 0; i < nodes.length; i++) {
      //noinspection unchecked
      psiElements[i] = (T)nodes[i].getPsi();
    }
    return psiElements;
  }

  @Nullable
  protected static ASTNode getPrevComma(ASTNode after) {
    ASTNode node = after;
    PyElementType comma = PyTokenTypes.COMMA;
    do {
      node = node.getTreePrev();
    }
    while (node != null && !node.getElementType().equals(comma));
    return node;
  }

  @Nullable
  public static ASTNode getNextComma(ASTNode after) {
    ASTNode node = after;
    PyElementType comma = PyTokenTypes.COMMA;
    do {
      node = node.getTreeNext();
    }
    while (node != null && !node.getElementType().equals(comma));
    return node;
  }

  public static PsiElement replaceExpression(@NotNull final PsiElement oldExpression,
                                             @NotNull final PsiElement newExpression) {
    final Pair<PsiElement, TextRange> data = oldExpression.getUserData(SELECTION_BREAKS_AST_NODE);
    if (data != null) {
      final PsiElement parent = data.first;
      final TextRange textRange = data.second;
      final String parentText = parent.getText();
      final String prefix = parentText.substring(0, textRange.getStartOffset());
      final String suffix = parentText.substring(textRange.getEndOffset(), parent.getTextLength());
      final PyElementGenerator generator = PyElementGenerator.getInstance(oldExpression.getProject());
      final LanguageLevel languageLevel = LanguageLevel.forElement(oldExpression);
      final PsiElement expression = generator.createFromText(languageLevel, parent.getClass(), prefix + newExpression.getText() + suffix);
      return parent.replace(expression);
    }
    else {
      return oldExpression.replace(newExpression);
    }
  }

  public static void addBeforeInParent(@NotNull final PsiElement anchor, @NotNull final PsiElement... newElements) {
    final ASTNode anchorNode = anchor.getNode();
    LOG.assertTrue(anchorNode != null);
    for (PsiElement newElement : newElements) {
      anchorNode.getTreeParent().addChild(newElement.getNode(), anchorNode);
    }
  }

  public static void removeElements(@NotNull final PsiElement... elements) {
    final ASTNode parentNode = elements[0].getParent().getNode();
    LOG.assertTrue(parentNode != null);
    for (PsiElement element : elements) {
      //noinspection ConstantConditions
      parentNode.removeChild(element.getNode());
    }
  }

  @Nullable
  public static PsiElement getStatement(@NotNull final PsiElement element) {
    final PyElement compStatement = getStatementList(element);
    if (compStatement == null) {
      return null;
    }
    return getStatement(compStatement, element);
  }

  public static PyElement getStatementList(final PsiElement element) {
    //noinspection ConstantConditions
    return element instanceof PyFile || element instanceof PyStatementList
           ? (PyElement)element
           : PsiTreeUtil.getParentOfType(element, PyFile.class, PyStatementList.class);
  }

  @Nullable
  public static PsiElement getStatement(final PsiElement compStatement, PsiElement element) {
    PsiElement parent = element.getParent();
    while (parent != null && parent != compStatement) {
      element = parent;
      parent = element.getParent();
    }
    return parent != null ? element : null;
  }

  public static List<PsiElement> collectElements(final PsiElement statement1, final PsiElement statement2) {
    // Process ASTNodes here to handle all the nodes
    final ASTNode node1 = statement1.getNode();
    final ASTNode node2 = statement2.getNode();
    final ASTNode parentNode = node1.getTreeParent();

    boolean insideRange = false;
    final List<PsiElement> result = new ArrayList<PsiElement>();
    for (ASTNode node : parentNode.getChildren(null)) {
      // start
      if (node1 == node) {
        insideRange = true;
      }
      if (insideRange) {
        result.add(node.getPsi());
      }
      // stop
      if (node == node2) {
        break;
      }
    }
    return result;
  }

  public static int getElementIndentation(final PsiElement element) {
    final PsiElement compStatement = getStatementList(element);
    final PsiElement statement = getStatement(compStatement, element);
    if (statement == null) {
      return 0;
    }
    PsiElement sibling = statement.getPrevSibling();
    if (sibling == null) {
      sibling = compStatement.getPrevSibling();
    }
    final String whitespace = sibling instanceof PsiWhiteSpace ? sibling.getText() : "";
    final int i = whitespace.lastIndexOf("\n");
    return i != -1 ? whitespace.length() - i - 1 : 0;
  }

  public static void removeRedundantPass(final PyStatementList statementList) {
    final PyStatement[] statements = statementList.getStatements();
    if (statements.length > 1) {
      for (PyStatement statement : statements) {
        if (statement instanceof PyPassStatement) {
          statement.delete();
        }
      }
    }
  }

  public static boolean isMethodContext(final PsiElement element) {
    final PsiNamedElement parent = PsiTreeUtil.getParentOfType(element, PyFile.class, PyFunction.class, PyClass.class);
    // In case if element is inside method which is inside class
    if (parent instanceof PyFunction && PsiTreeUtil.getParentOfType(parent, PyFile.class, PyClass.class) instanceof PyClass) {
      return true;
    }
    return false;
  }


  @NotNull
  public static PsiElement getRealContext(@NotNull final PsiElement element) {
    if (!element.isValid()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("PyPsiUtil.getRealContext(" + element + ") called. Returned null. Element in invalid");
      }
      return element;
    }
    final PsiFile file = element.getContainingFile();
    if (file instanceof PyExpressionCodeFragment) {
      final PsiElement context = file.getContext();
      if (LOG.isDebugEnabled()) {
        LOG.debug("PyPsiUtil.getRealContext(" + element + ") is called. Returned " + context + ". Element inside code fragment");
      }
      return context != null ? context : element;
    }
    else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("PyPsiUtil.getRealContext(" + element + ") is called. Returned " + element + ".");
      }
      return element;
    }
  }

  static void deleteAdjacentComma(ASTDelegatePsiElement pyImportStatement, ASTNode child, final PyElement[] elements) {
    if (ArrayUtil.contains(child.getPsi(), elements)) {
      ASTNode next = getNextComma(child);
      if (next == null) {
        next = getPrevComma(child);
      }
      if (next != null) {
        final ASTNode prev = next.getTreePrev();
        pyImportStatement.deleteChildInternal(next);
        removeSlash(pyImportStatement, prev);
      }
    }
  }

  private static void removeSlash(final ASTDelegatePsiElement statement, ASTNode prev) {
    final List<ASTNode> toDelete = new ArrayList<ASTNode>();

    while (prev instanceof PsiWhiteSpace) {
      toDelete.add(0, prev);
      prev = prev.getTreePrev();
    }
    prev = prev.getTreeNext();

    while (prev instanceof PsiWhiteSpace) {
      toDelete.add(prev);
      prev = prev.getTreeNext();
    }
    statement.deleteChildRange(toDelete.get(0).getPsi(), toDelete.get(toDelete.size() - 1).getPsi());
  }

  static <T, U extends PsiElement> List<T> collectStubChildren(U e,
                                                               final StubElement<U> stub, final IElementType elementType,
                                                               final Class<T> itemClass) {
    final List<T> result = new ArrayList<T>();
    if (stub != null) {
      final List<StubElement> children = stub.getChildrenStubs();
      for (StubElement child : children) {
        if (child.getStubType() == elementType) {
          //noinspection unchecked
          result.add((T)child.getPsi());
        }
      }
    }
    else {
      e.acceptChildren(new TopLevelVisitor() {
        @Override
        protected void checkAddElement(PsiElement node) {
          if (node.getNode().getElementType() == elementType) {
            //noinspection unchecked
            result.add((T)node);
          }
        }
      });
    }
    return result;
  }

  static List<PsiElement> collectAllStubChildren(PsiElement e, StubElement stub) {
    final List<PsiElement> result = new ArrayList<PsiElement>();
    if (stub != null) {
      final List<StubElement> children = stub.getChildrenStubs();
      for (StubElement child : children) {
        //noinspection unchecked
        result.add(child.getPsi());
      }
    }
    else {
      e.acceptChildren(new TopLevelVisitor() {
        @Override
        protected void checkAddElement(PsiElement node) {
          result.add(node);
        }
      });
    }
    return result;
  }

  @Nullable
  public static PsiElement getSignificantToTheRight(PsiElement element, final boolean ignoreComments) {
    while (element != null && StringUtil.isEmptyOrSpaces(element.getText()) || ignoreComments && element instanceof PsiComment) {
      element = PsiTreeUtil.nextLeaf(element);
    }
    return element;
  }

  @Nullable
  public static PsiElement getSignificantToTheLeft(PsiElement element, final boolean ignoreComments) {
    while (element != null && StringUtil.isEmptyOrSpaces(element.getText()) || ignoreComments && element instanceof PsiComment) {
      element = PsiTreeUtil.prevLeaf(element);
    }
    return element;
  }

  public static int findArgumentIndex(PyCallExpression call, PsiElement argument) {
    final PyExpression[] args = call.getArguments();
    for (int i = 0; i < args.length; i++) {
      PyExpression expression = args[i];
      if (expression instanceof PyKeywordArgument) {
        expression = ((PyKeywordArgument)expression).getValueExpression();
      }
      expression = flattenParens(expression);
      if (expression == argument) {
        return i;
      }
    }
    return -1;
  }

  @Nullable
  public static PyTargetExpression getAttribute(@NotNull final PyFile file, @NotNull final String name) {
    PyTargetExpression attr = file.findTopLevelAttribute(name);
    if (attr == null) {
      for (PyFromImportStatement element : file.getFromImports()) {
        PyReferenceExpression expression = element.getImportSource();
        if (expression == null) continue;
        final PsiElement resolved = expression.getReference().resolve();
        if (resolved instanceof PyFile && resolved != file) {
          return ((PyFile)resolved).findTopLevelAttribute(name);
        }
      }
    }
    return attr;
  }

  public static List<PyExpression> getAttributeValuesFromFile(@NotNull PyFile file, @NotNull String name) {
    List<PyExpression> result = ContainerUtil.newArrayList();
    final PyTargetExpression attr = file.findTopLevelAttribute(name);
    if (attr != null) {
      sequenceToList(result, attr.findAssignedValue());
    }
    return result;
  }

  private static void sequenceToList(List<PyExpression> result, PyExpression value) {
    value = flattenParens(value);
    if (value instanceof PySequenceExpression) {
      result.addAll(ContainerUtil.newArrayList(((PySequenceExpression)value).getElements()));
    }
    else {
      result.add(value);
    }
  }

  public static List<String> getStringValues(PyExpression[] elements) {
    List<String> results = ContainerUtil.newArrayList();
    for (PyExpression element : elements) {
      if (element instanceof PyStringLiteralExpression) {
        results.add(((PyStringLiteralExpression)element).getStringValue());
      }
    }
    return results;
  }

  @Nullable
  public static PyExpression flattenParens(@Nullable PyExpression expr) {
    while (expr instanceof PyParenthesizedExpression) {
      expr = ((PyParenthesizedExpression)expr).getContainedExpression();
    }
    return expr;
  }

  @Nullable
  public static String strValue(@Nullable PyExpression expression) {
    return expression instanceof PyStringLiteralExpression ? ((PyStringLiteralExpression)expression).getStringValue() : null;
  }

  public static boolean isBefore(@NotNull final PsiElement element, @NotNull final PsiElement element2) {
    // TODO: From RubyPsiUtil, should be moved to PsiTreeUtil
    return element.getTextOffset() <= element2.getTextOffset();
  }

  public static Collection<? extends PyExpression> getAugAssignments(final @NotNull PyFile file, final @NotNull String name) {
    final List<PyExpression> result = Lists.newArrayList();
    file.accept(new TopLevelVisitor() {
      @Override
      protected void checkAddElement(PsiElement node) {
        if (node instanceof PyAugAssignmentStatement) {
          PyAugAssignmentStatement augAss = (PyAugAssignmentStatement)node;
          if (name.equals(augAss.getTarget().getName())) {
            sequenceToList(result, augAss.getValue());
          }
        }
      }
    });
    return result;
  }

  public static Collection<? extends PyExpression> getCallArguments(final @NotNull PyFile file,
                                                                    final @NotNull String name,
                                                                    final @NotNull String callName) {
    final List<PyExpression> result = Lists.newArrayList();
    file.accept(new TopLevelVisitor() {
      @Override
      protected void checkAddElement(PsiElement node) {
        if (node instanceof PyCallExpression) {
          PyCallExpression call = (PyCallExpression)node;
          if (call.getCallee() instanceof PyReferenceExpression) {
            PyReferenceExpression ref = (PyReferenceExpression)call.getCallee();
            if (callName.equals(ref.getName())) {
              PyExpression ex = ref.getQualifier();
              if (name.equals(ex.getName())) {
                for (PyExpression expr : call.getArguments()) {
                  sequenceToList(result, expr);
                }
              }
            }
          }
        }
      }
    });
    return result;
  }

  private static abstract class TopLevelVisitor extends PyRecursiveElementVisitor {
    public void visitPyElement(final PyElement node) {
      super.visitPyElement(node);
      checkAddElement(node);
    }

    public void visitPyClass(final PyClass node) {
      checkAddElement(node);  // do not recurse into functions
    }

    public void visitPyFunction(final PyFunction node) {
      checkAddElement(node);  // do not recurse into classes
    }

    protected abstract void checkAddElement(PsiElement node);
  }
}
package com.jetbrains.python.refactoring.move;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.ui.UsageViewDescriptorAdapter;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.codeInsight.imports.PyImportOptimizer;
import com.jetbrains.python.documentation.DocStringTypeReference;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyQualifiedName;
import com.jetbrains.python.psi.impl.PyReferenceExpressionImpl;
import com.jetbrains.python.psi.resolve.ResolveImportUtil;
import com.jetbrains.python.refactoring.PyRefactoringUtil;
import com.jetbrains.python.refactoring.classes.PyClassRefactoringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vlan
 */
public class PyMoveClassOrFunctionProcessor extends BaseRefactoringProcessor {
  public static final String REFACTORING_NAME = PyBundle.message("refactoring.move.class.or.function");

  private PsiNamedElement[] myElements;
  private String myDestination;

  public PyMoveClassOrFunctionProcessor(Project project, PsiNamedElement[] elements, String destination, boolean previewUsages) {
    super(project);
    assert elements.length > 0;
    myElements = elements;
    myDestination = destination;
    setPreviewUsages(previewUsages);
  }

  @NotNull
  @Override
  protected UsageViewDescriptor createUsageViewDescriptor(final UsageInfo[] usages) {
    return new UsageViewDescriptorAdapter() {
      @NotNull
      @Override
      public PsiElement[] getElements() {
        return myElements;
      }

      @Override
      public String getProcessedElementsHeader() {
        return REFACTORING_NAME;
      }
    };
  }

  @NotNull
  @Override
  protected UsageInfo[] findUsages() {
    final List<UsageInfo> usages = new ArrayList<UsageInfo>();
    for (PsiNamedElement element : myElements) {
      usages.addAll(PyRefactoringUtil.findUsages(element));
    }
    return usages.toArray(new UsageInfo[usages.size()]);
  }

  @Override
  protected void performRefactoring(final UsageInfo[] usages) {
    CommandProcessor.getInstance().executeCommand(myElements[0].getProject(), new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            final PyFile dest = PyUtil.getOrCreateFile(myDestination, myProject);
            CommonRefactoringUtil.checkReadOnlyStatus(myProject, dest);
            for (PsiNamedElement e: myElements) {
              // TODO: Check for resulting circular imports
              CommonRefactoringUtil.checkReadOnlyStatus(myProject, e);
              assert e instanceof PyClass || e instanceof PyFunction;
              if (e instanceof PyClass && dest.findTopLevelClass(e.getName()) != null) {
                throw new IncorrectOperationException(PyBundle.message("refactoring.move.class.or.function.error.destination.file.contains.class.$0", e.getName()));
              }
              if (e instanceof PyFunction && dest.findTopLevelFunction(e.getName()) != null) {
                throw new IncorrectOperationException(PyBundle.message("refactoring.move.class.or.function.error.destination.file.contains.function.$0", e.getName()));
              }
              checkValidImportableFile(dest, e.getContainingFile().getVirtualFile());
              checkValidImportableFile(e, dest.getVirtualFile());
            }
            for (PsiNamedElement oldElement: myElements) {
              final PsiFile oldFile = oldElement.getContainingFile();
              PyClassRefactoringUtil.rememberNamedReferences(oldElement);
              final PsiNamedElement newElement = (PsiNamedElement)(dest.add(oldElement));
              for (UsageInfo usage : usages) {
                final PsiElement usageElement = usage.getElement();
                // TODO: Respect the qualified import style
                if (usageElement instanceof PyQualifiedExpression) {
                  PyQualifiedExpression qexpr = (PyQualifiedExpression)usageElement;
                  if (oldElement instanceof PyClass && PyNames.INIT.equals(qexpr.getName())) {
                    continue;
                  }
                  if (qexpr.getQualifier() != null) {
                    final PsiElement newExpr = qexpr.replace(new PyReferenceExpressionImpl(qexpr.getNameElement()));
                    PyClassRefactoringUtil.insertImport(newExpr, newElement, null, true);
                  }
                }
                if (usageElement instanceof PyStringLiteralExpression) {
                  for (PsiReference ref : usageElement.getReferences()) {
                    if (ref instanceof DocStringTypeReference && ref.isReferenceTo(oldElement)) {
                      ref.bindToElement(newElement);
                    }
                  }
                }
                else {
                  final PyImportStatementBase importStmt = PsiTreeUtil.getParentOfType(usageElement, PyImportStatementBase.class);
                  if (importStmt != null) {
                    PyClassRefactoringUtil.updateImportOfElement(importStmt, newElement);
                  }
                  if (usage.getFile() == oldFile && (usageElement == null || !PsiTreeUtil.isAncestor(oldElement, usageElement, false))) {
                    PyClassRefactoringUtil.insertImport(oldElement, newElement);
                  }
                  if (usageElement != null && resolvesToLocalStarImport(usageElement)) {
                    PyClassRefactoringUtil.insertImport(usageElement, newElement);
                    new PyImportOptimizer().processFile(usageElement.getContainingFile()).run();
                  }
                }
              }
              PyClassRefactoringUtil.restoreNamedReferences(newElement, oldElement);
              // TODO: Remove extra empty lines after the removed element
              oldElement.delete();
              new PyImportOptimizer().processFile(oldFile).run();
            }
          }
        });
      }
    }, REFACTORING_NAME, null);
  }

  @Override
  protected String getCommandName() {
    return REFACTORING_NAME;
  }

  private boolean resolvesToLocalStarImport(@NotNull PsiElement element) {
    final PsiReference ref = element.getReference();
    final List<PsiElement> resolvedElements = new ArrayList<PsiElement>();
    if (ref instanceof PsiPolyVariantReference) {
      for (ResolveResult result : ((PsiPolyVariantReference)ref).multiResolve(false)) {
        resolvedElements.add(result.getElement());
      }
    }
    else if (ref != null) {
      resolvedElements.add(ref.resolve());
    }
    final PsiFile containingFile = element.getContainingFile();
    if (containingFile != null) {
      for (PsiElement resolved : resolvedElements) {
        if (resolved instanceof PyStarImportElement && resolved.getContainingFile() == containingFile) {
          return true;
        }
      }
    }
    return false;
  }

  private static void checkValidImportableFile(PsiElement anchor, VirtualFile file) {
    final PyQualifiedName qName = ResolveImportUtil.findShortestImportableQName(anchor, file);
    if (!PyClassRefactoringUtil.isValidQualifiedName(qName)) {
      throw new IncorrectOperationException(PyBundle.message("refactoring.move.class.or.function.error.cannot.use.module.name.$0", qName));
    }
  }
}

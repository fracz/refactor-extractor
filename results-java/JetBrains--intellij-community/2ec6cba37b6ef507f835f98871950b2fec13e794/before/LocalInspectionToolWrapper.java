package com.intellij.codeInspection.ex;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.codeInspection.reference.RefManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.*;
import com.intellij.psi.jsp.JspFile;
import org.jdom.Element;

import javax.swing.*;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author max
 */
public final class LocalInspectionToolWrapper extends DescriptorProviderInspection {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.ex.LocalInspectionToolWrapper");

  private LocalInspectionTool myTool;

  public LocalInspectionToolWrapper(LocalInspectionTool tool) {
    myTool = tool;
  }

  public LocalInspectionTool getTool() {
    return myTool;
  }

  public void processFile(PsiFile file) {
    file.accept(new PsiRecursiveElementVisitor() {
      public void visitReferenceExpression(PsiReferenceExpression expression) {
        visitElement(expression);
      }

      @Override
      public void visitJspFile(JspFile file) {
        final PsiElement[] roots = file.getPsiRoots();
        for (PsiElement root : roots) {
          visitElement(root);
        }
      }

      public void visitField(PsiField field) {
        super.visitField(field);
        if (InspectionManagerEx.isToCheckMember(field, myTool.getID())) {
          ProblemDescriptor[] problemDescriptions = myTool.checkField(field, getManager(), false);
          addProblemDescriptors(field, problemDescriptions);
        }
      }

      private ProblemDescriptor[] filterUnsuppressedProblemDescriptions(ProblemDescriptor[] problemDescriptions) {
        Set<ProblemDescriptor> set = null;
        for (ProblemDescriptor description : problemDescriptions) {
          if (InspectionManagerEx.inspectionResultSuppressed(description.getPsiElement(), myTool.getID())) {
            if (set == null) set = new LinkedHashSet<ProblemDescriptor>(Arrays.asList(problemDescriptions));
            set.remove(description);
          }
        }
        return set == null ? problemDescriptions : set.toArray(new ProblemDescriptor[set.size()]);
      }

      public void visitClass(PsiClass aClass) {
        super.visitClass(aClass);
        if (InspectionManagerEx.isToCheckMember(aClass, myTool.getID()) && !(aClass instanceof PsiTypeParameter)) {
          ProblemDescriptor[] problemDescriptions = myTool.checkClass(aClass, getManager(), false);
          addProblemDescriptors(aClass, problemDescriptions);
        }
      }


      public void visitMethod(PsiMethod method) {
        super.visitMethod(method);
        if (InspectionManagerEx.isToCheckMember(method, myTool.getID())) {
          ProblemDescriptor[] problemDescriptions = myTool.checkMethod(method, getManager(), false);
          addProblemDescriptors(method, problemDescriptions);
        }
      }

      private void addProblemDescriptors(PsiElement element, ProblemDescriptor[] problemDescriptions) {
        if (problemDescriptions != null) {
          problemDescriptions = filterUnsuppressedProblemDescriptions(problemDescriptions);
          if (problemDescriptions.length != 0) {
            RefManager refManager = getManager().getRefManager();
            RefElement refElement = refManager.getReference(element);
            if (refElement != null) {
              addProblemElement(refElement, problemDescriptions);
            }
          }
        }
      }

      public void visitFile(PsiFile file) {
        super.visitFile(file);
        ProblemDescriptor[] problemDescriptions = myTool.checkFile(file, getManager(), false);
        addProblemDescriptors(file, problemDescriptions);
      }
    });
  }

  public JobDescriptor[] getJobDescriptors() {
    return new JobDescriptor[0];
  }

  public void runInspection(AnalysisScope scope) {
    LOG.assertTrue(ApplicationManager.getApplication().isUnitTestMode());
    scope.accept(new PsiRecursiveElementVisitor() {
      public void visitReferenceExpression(PsiReferenceExpression expression) {
      }

      public void visitFile(PsiFile file) {
        processFile(file);
      }
    });
  }

  public boolean isGraphNeeded() {
    return false;
  }

  public String getDisplayName() {
    return myTool.getDisplayName();
  }

  public String getGroupDisplayName() {
    return myTool.getGroupDisplayName();
  }

  public String getShortName() {
    return myTool.getShortName();
  }

  public boolean isEnabledByDefault() {
    return myTool.isEnabledByDefault();
  }

  public HighlightDisplayLevel getDefaultLevel() {
    return myTool.getDefaultLevel();
  }

  public void readExternal(Element element) throws InvalidDataException {
    myTool.readSettings(element);
  }

  public void writeExternal(Element element) throws WriteExternalException {
    myTool.writeSettings(element);
  }

  protected JComponent createOptionsPanel() {
    JComponent provided = myTool.createOptionsPanel();
    return provided == null ? super.createOptionsPanel() : provided;
  }
}
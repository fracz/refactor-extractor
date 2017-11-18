package com.intellij.codeInspection.unneededThrows;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInsight.ExceptionUtil;
import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ex.DescriptorProviderInspection;
import com.intellij.codeInspection.ex.InspectionManagerEx;
import com.intellij.codeInspection.ex.JobDescriptor;
import com.intellij.codeInspection.ex.ProblemDescriptorImpl;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.codeInspection.reference.RefEntity;
import com.intellij.codeInspection.reference.RefMethod;
import com.intellij.codeInspection.reference.RefVisitor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author max
 */
public class UnneededThrows extends DescriptorProviderInspection {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.unneededThrows.UnneededThrows");
  public static final String DISPLAY_NAME = InspectionsBundle.message("inspection.redundant.throws.display.name");
  private QuickFix myQuickFix;
  @NonNls public static final String SHORT_NAME = "UnneededThrows";

  public void runInspection(AnalysisScope scope) {
    getRefManager().iterate(new RefVisitor() {
      public void visitElement(RefEntity refEntity) {
        if (refEntity instanceof RefMethod && !((RefMethod)refEntity).isSyntheticJSP()) {
          RefMethod refMethod = (RefMethod)refEntity;
          if (!InspectionManagerEx.isToCheckMember((PsiDocCommentOwner) refMethod.getElement(), UnneededThrows.this)) return;
          ProblemDescriptorImpl[] descriptors = checkMethod(refMethod);
          if (descriptors != null) {
            addProblemElement(refMethod, descriptors);
          }
        }
      }
    });
  }

  public boolean isGraphNeeded() {
    return true;
  }

  @Nullable
  private ProblemDescriptorImpl[] checkMethod(RefMethod refMethod) {
    if (refMethod.isLibraryOverride()) return null;
    if (refMethod.getSuperMethods().size() > 0) return null;

    PsiClassType[] unThrown = refMethod.getUnThrownExceptions();
    if (unThrown == null) return null;

    PsiMethod psiMethod = (PsiMethod)refMethod.getElement();
    PsiClassType[] throwsList = psiMethod.getThrowsList().getReferencedTypes();
    PsiJavaCodeReferenceElement[] throwsRefs = psiMethod.getThrowsList().getReferenceElements();
    ArrayList<ProblemDescriptor> problems = null;

    for (int i = 0; i < throwsList.length; i++) {
      PsiClassType throwsType = throwsList[i];
      PsiJavaCodeReferenceElement throwsRef = throwsRefs[i];
      if (ExceptionUtil.isUncheckedException(throwsType)) continue;

      for (int j = 0; j < unThrown.length; j++) {
        PsiClassType s = unThrown[j];
        if (s.equals(throwsType)) {
          if (problems == null) problems = new ArrayList<ProblemDescriptor>(1);

          if (refMethod.isAbstract() || refMethod.getOwnerClass().isInterface()) {
            problems.add(
              getManager().createProblemDescriptor(throwsRef, InspectionsBundle.message("inspection.redundant.throws.problem.descriptor",
                                                                                        "<code>#ref</code>"), getFix(),
                                                                                                              ProblemHighlightType.LIKE_UNUSED_SYMBOL));
          }
          else if (refMethod.getDerivedMethods().size() > 0) {
            problems.add(
              getManager().createProblemDescriptor(throwsRef, InspectionsBundle.message("inspection.redundant.throws.problem.descriptor1",
                                                                                        "<code>#ref</code>"), getFix(),
                                                                                                              ProblemHighlightType.LIKE_UNUSED_SYMBOL));
          }
          else {
            problems.add(
              getManager().createProblemDescriptor(throwsRef, InspectionsBundle.message("inspection.redundant.throws.problem.descriptor2",
                                                                                        "<code>#ref</code>"), getFix(),
                                                                                                              ProblemHighlightType.LIKE_UNUSED_SYMBOL));
          }


        }
      }
    }

    if (problems != null) {
      return problems.toArray(new ProblemDescriptorImpl[problems.size()]);
    }

    return null;
  }

  public boolean queryExternalUsagesRequests() {
    getRefManager().iterate(new RefVisitor() {
      public void visitElement(RefEntity refEntity) {
        if (getDescriptions(refEntity) != null) {
          refEntity.accept(new RefVisitor() {
            public void visitMethod(final RefMethod refMethod) {
              getManager().enqueueDerivedMethodsProcessor(refMethod, new InspectionManagerEx.DerivedMethodsProcessor() {
                public boolean process(PsiMethod derivedMethod) {
                  ignoreElement(refMethod);
                  return true;
                }
              });
            }
          });
        }
      }
    });

    return false;
  }

  public JobDescriptor[] getJobDescriptors() {
    return new JobDescriptor[]{InspectionManagerEx.BUILD_GRAPH, InspectionManagerEx.FIND_EXTERNAL_USAGES};
  }

  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  public String getGroupDisplayName() {
    return GroupNames.DECLARATION_REDUNDANCY;
  }

  public String getShortName() {
    return SHORT_NAME;
  }

  public UnneededThrows() {
  }

  private LocalQuickFix getFix() {
    if (myQuickFix == null) {
      myQuickFix = new QuickFix();
    }
    return myQuickFix;
  }

  private class QuickFix implements LocalQuickFix {
    public String getName() {
      return InspectionsBundle.message("inspection.redundant.throws.remove.quickfix");
    }

    public void applyFix(Project project, ProblemDescriptor descriptor) {
      RefElement refElement = (RefElement)getElement(descriptor);
      if (refElement.isValid() && refElement instanceof RefMethod) {
        RefMethod refMethod = (RefMethod)refElement;
        removeExcessiveThrows(refMethod);
      }
    }

    public String getFamilyName() {
      return getName();
    }

    private void removeExcessiveThrows(RefMethod refMethod) {
      try {
        Project project = getManager().getProject();
        ProblemDescriptor[] problems = (ProblemDescriptor[])getDescriptions(refMethod);
        if (problems == null) return;
        PsiManager psiManager = PsiManager.getInstance(project);
        List<PsiJavaCodeReferenceElement> refsToDelete = new ArrayList<PsiJavaCodeReferenceElement>();
        for (int i = 0; i < problems.length; i++) {
          ProblemDescriptor problem = problems[i];
          PsiJavaCodeReferenceElement classRef = (PsiJavaCodeReferenceElement)problem.getPsiElement();
          if (classRef == null) continue;
          PsiType psiType = psiManager.getElementFactory().createType(classRef);
          removeException(refMethod, psiType, refsToDelete);
        }

        for (Iterator<PsiJavaCodeReferenceElement> iterator = refsToDelete.iterator(); iterator.hasNext();) {
          iterator.next().delete();
        }
      }
      catch (IncorrectOperationException e) {
        LOG.error(e);
      }
    }

    private void removeException(RefMethod refMethod, PsiType exceptionType,
                                 List<PsiJavaCodeReferenceElement> refsToDelete) {
      PsiMethod psiMethod = (PsiMethod)refMethod.getElement();
      PsiManager psiManager = psiMethod.getManager();

      PsiJavaCodeReferenceElement[] refs = psiMethod.getThrowsList().getReferenceElements();
      for (int i = 0; i < refs.length; i++) {
        PsiJavaCodeReferenceElement ref = refs[i];
        PsiType refType = psiManager.getElementFactory().createType(ref);
        if (exceptionType.isAssignableFrom(refType)) {
          refsToDelete.add(ref);
        }
      }

      for (Iterator<RefMethod> iterator = refMethod.getDerivedMethods().iterator(); iterator.hasNext();) {
        RefMethod refDerived = iterator.next();
        removeException(refDerived, exceptionType, refsToDelete);
      }
    }
  }
}
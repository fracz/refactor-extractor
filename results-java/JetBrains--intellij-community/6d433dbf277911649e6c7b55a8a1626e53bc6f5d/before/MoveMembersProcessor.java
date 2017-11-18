/**
 * created at Sep 11, 2001
 * @author Jeka
 */
package com.intellij.refactoring.move.moveMembers;

import com.intellij.codeInsight.ChangeContextUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.JavaResolveUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.MethodSignatureUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.move.MoveCallback;
import com.intellij.refactoring.move.MoveHandler;
import com.intellij.refactoring.ui.ConflictsDialog;
import com.intellij.refactoring.util.*;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.intellij.usageView.UsageViewUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MoveMembersProcessor extends BaseRefactoringProcessor {
  private static final Logger LOG = Logger.getInstance("#com.intellij.refactoring.move.moveMembers.MoveMembersProcessor");

  private PsiClass myTargetClass;
  private final Set<PsiMember> myMembersToMove = new LinkedHashSet<PsiMember>();
  private final MoveCallback myMoveCallback;
  private String myNewVisibility; // "null" means "as is"
  private String myCommandName = MoveMembersImpl.REFACTORING_NAME;
  private boolean myMakeEnumConstant;

  public MoveMembersProcessor(Project project, MoveCallback moveCallback, MoveMembersOptions options) {
    super(project);
    myMoveCallback = moveCallback;
    setOptions(options);
  }

  public MoveMembersProcessor(Project project, MoveMembersOptions options) {
    this(project, null, options);
  }

  protected String getCommandName() {
    return myCommandName;
  }

  private void setOptions(MoveMembersOptions dialog) {
    PsiMember[] members = dialog.getSelectedMembers();
    myMembersToMove.clear();
    myMembersToMove.addAll(Arrays.asList(members));

    setCommandName(members);

    final PsiManager manager = PsiManager.getInstance(myProject);
    myTargetClass =
      JavaPsiFacade.getInstance(manager.getProject()).findClass(dialog.getTargetClassName(), GlobalSearchScope.projectScope(myProject));
    myNewVisibility = dialog.getMemberVisibility();
    myMakeEnumConstant = dialog.makeEnumConstant();
  }

  private void setCommandName(final PsiMember[] members) {
    StringBuilder commandName = new StringBuilder();
    commandName.append(MoveHandler.REFACTORING_NAME);
    commandName.append(" ");
    boolean first = true;
    for (PsiMember member : members) {
      if (!first) commandName.append(", ");
      commandName.append(UsageViewUtil.getType(member));
      commandName.append(' ');
      commandName.append(UsageViewUtil.getShortName(member));
      first = false;
    }

    myCommandName = commandName.toString();
  }

  protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usages) {
    return new MoveMemberViewDescriptor(myMembersToMove.toArray(new PsiElement[myMembersToMove.size()]));
  }

  @NotNull
  protected UsageInfo[] findUsages() {
    final List<UsageInfo> usagesList = new ArrayList<UsageInfo>();
    for (PsiMember member : myMembersToMove) {
      for (PsiReference psiReference : ReferencesSearch.search(member)) {
        PsiElement ref = psiReference.getElement();
        if (ref instanceof PsiReferenceExpression) {
          PsiReferenceExpression refExpr = (PsiReferenceExpression)ref;
          PsiExpression qualifier = refExpr.getQualifierExpression();
          if (RefactoringHierarchyUtil.willBeInTargetClass(refExpr, myMembersToMove, myTargetClass, true)) {
            // both member and the reference to it will be in target class
            if (!isInMovedElement(refExpr)) {
              if (qualifier != null) {
                usagesList.add(new MyUsageInfo(member, refExpr, null, qualifier, psiReference));  // remove qualifier
              }
            }
            else {
              if (qualifier instanceof PsiReferenceExpression && ((PsiReferenceExpression)qualifier).isReferenceTo(member.getContainingClass())) {
                usagesList.add(new MyUsageInfo(member, refExpr, null, qualifier, psiReference));  // change qualifier
              }
            }
          }
          else {
            // member in target class, the reference will be outside target class
            if (qualifier == null) {
              usagesList.add(new MyUsageInfo(member, refExpr, myTargetClass, refExpr, psiReference)); // add qualifier
            }
            else {
              usagesList.add(new MyUsageInfo(member, refExpr, myTargetClass, qualifier, psiReference)); // change qualifier
            }
          }
        }
        else {
          if (!isInMovedElement(ref)) {
            usagesList.add(new MyUsageInfo(member, ref, null, ref, psiReference));
          }
        }
      }
    }
    UsageInfo[] usageInfos = usagesList.toArray(new UsageInfo[usagesList.size()]);
    usageInfos = UsageViewUtil.removeDuplicatedUsages(usageInfos);
    return usageInfos;
  }

  protected void refreshElements(PsiElement[] elements) {
    LOG.assertTrue(myMembersToMove.size() == elements.length);
    myMembersToMove.clear();
    for (PsiElement resolved : elements) {
      myMembersToMove.add((PsiMember)resolved);
    }
  }

  private boolean isInMovedElement(PsiElement element) {
    for (PsiMember member : myMembersToMove) {
      if (PsiTreeUtil.isAncestor(member, element, false)) return true;
    }
    return false;
  }

  protected void performRefactoring(final UsageInfo[] usages) {
    try {
      // correct references to moved members from the outside
      ArrayList<MyUsageInfo> otherUsages = new ArrayList<MyUsageInfo>();
      for (UsageInfo usageInfo : usages) {
        MyUsageInfo usage = (MyUsageInfo)usageInfo;
        if (!usage.reference.isValid()) continue;
        if (usage.reference instanceof PsiReferenceExpression) {
          PsiReferenceExpression refExpr = (PsiReferenceExpression)usage.reference;
          PsiExpression qualifier = refExpr.getQualifierExpression();
          if (qualifier != null) {
            if (usage.qualifierClass != null) {
              changeQualifier(refExpr, usage.qualifierClass);
            }
            else {
              removeQualifier(refExpr);
            }
          }
          else { // no qualifier
            if (usage.qualifierClass != null) {
              changeQualifier(refExpr, usage.qualifierClass);
            }
          }
        }
        else {
          otherUsages.add(usage);
        }
      }

      // correct references inside moved members and outer references to Inner Classes
      for (PsiMember member : myMembersToMove) {
        if (member instanceof PsiVariable) {
          ((PsiVariable)member).normalizeDeclaration();
        }
        final RefactoringElementListener elementListener = getTransaction().getElementListener(member);
        ChangeContextUtil.encodeContextInfo(member, true);

        PsiElement anchor = getAnchor(member);

        final PsiMember memberCopy;
        if (myMakeEnumConstant && member instanceof PsiVariable && EnumConstantsUtil.isSuitableForEnumConstant(((PsiVariable)member).getType(), myTargetClass)) {
          memberCopy = EnumConstantsUtil.createEnumConstant(myTargetClass, member.getName(), ((PsiVariable)member).getInitializer());
        } else {
          memberCopy = (PsiMember)member.copy();
          if (member.getContainingClass().isInterface() && !myTargetClass.isInterface()) {
            //might need to make modifiers explicit, see IDEADEV-11416
            final PsiModifierList list = memberCopy.getModifierList();
            assert list != null;
            list.setModifierProperty(PsiModifier.STATIC, member.hasModifierProperty(PsiModifier.STATIC));
            list.setModifierProperty(PsiModifier.FINAL, member.hasModifierProperty(PsiModifier.FINAL));
            RefactoringUtil.setVisibility(list, VisibilityUtil.getVisibilityModifier(member.getModifierList()));
          }
        }

        ArrayList<PsiReference> refsToBeRebind = new ArrayList<PsiReference>();
        for (Iterator<MyUsageInfo> iterator = otherUsages.iterator(); iterator.hasNext();) {
          MyUsageInfo info = iterator.next();
          if (member.equals(info.member)) {
            PsiReference ref = info.getReference();
            if (ref != null) {
              refsToBeRebind.add(ref);
            }
            iterator.remove();
          }
        }
        member.delete();

        PsiMember newMember =
          anchor != null ? (PsiMember)myTargetClass.addAfter(memberCopy, anchor) : (PsiMember)myTargetClass.add(memberCopy);

        fixVisibility(newMember, usages);
        for (PsiReference reference : refsToBeRebind) {
          reference.bindToElement(newMember);
        }

        elementListener.elementMoved(newMember);
      }

      // qualifier info must be decoded after members are moved
      ChangeContextUtil.decodeContextInfo(myTargetClass, null, null);
      myMembersToMove.clear();
      if (myMoveCallback != null) {
        myMoveCallback.refactoringCompleted();
      }
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }

  @Nullable
  private PsiElement getAnchor(final PsiMember member) {
    if (member instanceof PsiField && member.hasModifierProperty(PsiModifier.STATIC)) {
      final List<PsiField> referencedFields = new ArrayList<PsiField>();
      final PsiExpression psiExpression = ((PsiField)member).getInitializer();
      if (psiExpression != null) {
        psiExpression.accept(new JavaRecursiveElementWalkingVisitor() {
          @Override
          public void visitReferenceExpression(final PsiReferenceExpression expression) {
            super.visitReferenceExpression(expression);
            final PsiElement psiElement = expression.resolve();
            if (psiElement instanceof PsiField) {
              final PsiField psiField = (PsiField)psiElement;
              if (psiField.getContainingClass() == myTargetClass && !referencedFields.contains(psiField)) {
                referencedFields.add(psiField);
              }
            }
          }
        });
      }
      if (!referencedFields.isEmpty()) {
        Collections.sort(referencedFields, new Comparator<PsiField>() {
          public int compare(final PsiField o1, final PsiField o2) {
            return -PsiUtilBase.compareElementsByPosition(o1, o2);
          }
        });
        return referencedFields.get(0);
      }
    }
    return null;
  }

  private void fixVisibility(PsiMember newMember, final UsageInfo[] usages) throws IncorrectOperationException {
    PsiModifierList modifierList = newMember.getModifierList();

    if(myTargetClass.isInterface()) {
      modifierList.setModifierProperty(PsiModifier.PUBLIC, false);
      modifierList.setModifierProperty(PsiModifier.PROTECTED, false);
      modifierList.setModifierProperty(PsiModifier.PRIVATE, false);
      return;
    }

    if(myNewVisibility == null) return;

    if (VisibilityUtil.ESCALATE_VISIBILITY.equals(myNewVisibility)) {
      for (UsageInfo usage : usages) {
        if (usage instanceof MyUsageInfo) {
          final PsiElement place = usage.getElement();
          if (place != null) {
            VisibilityUtil.escalateVisibility(newMember, place);
          }
        }
      }
    } else {
      RefactoringUtil.setVisibility(modifierList, myNewVisibility);
    }
  }


  private static void removeQualifier(PsiReferenceExpression refExpr) throws IncorrectOperationException{
    refExpr.setQualifierExpression(null);
  }

  private void changeQualifier(PsiReferenceExpression refExpr, PsiClass aClass) throws IncorrectOperationException{
    if (RefactoringUtil.hasOnDemandStaticImport(refExpr, aClass)) {
      removeQualifier(refExpr);
    }
    else {
      PsiElementFactory factory = JavaPsiFacade.getInstance(myProject).getElementFactory();
      refExpr.setQualifierExpression(factory.createReferenceExpression(aClass));
    }
  }

  protected boolean preprocessUsages(Ref<UsageInfo[]> refUsages) {
    final ArrayList<String> conflicts = new ArrayList<String>();
    final UsageInfo[] usages = refUsages.get();
    try {
      addInaccessiblleConflicts(conflicts, usages);
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
    RefactoringUtil.analyzeModuleConflicts(myProject, myMembersToMove, usages, myTargetClass, conflicts);
    return showConflicts(conflicts);
  }

  private void addInaccessiblleConflicts(final ArrayList<String> conflicts, final UsageInfo[] usages) throws IncorrectOperationException {
    String newVisibility = myNewVisibility;
    if (VisibilityUtil.ESCALATE_VISIBILITY.equals(newVisibility)) { //Still need to check for access object
      newVisibility = PsiModifier.PUBLIC;
    }

    Map<PsiMember, PsiModifierList> modifierListCopies = new HashMap<PsiMember, PsiModifierList>();
    for (PsiMember member : myMembersToMove) {
      final PsiModifierList copy = (PsiModifierList)member.getModifierList().copy();
      if (newVisibility != null) {
        RefactoringUtil.setVisibility(copy, newVisibility);
      }
      modifierListCopies.put(member, copy);
    }

    for (UsageInfo usage : usages) {
      if (usage instanceof MyUsageInfo) {
        final MyUsageInfo usageInfo = (MyUsageInfo)usage;
        PsiElement element = usage.getElement();
        if (element != null) {
          final PsiMember member = usageInfo.member;
          if (element instanceof PsiReferenceExpression) {
            PsiExpression qualifier = ((PsiReferenceExpression)element).getQualifierExpression();
            PsiClass accessObjectClass = null;
            if (qualifier != null) {
              accessObjectClass = (PsiClass)PsiUtil.getAccessObjectClass(qualifier).getElement();
            }

            if (!JavaResolveUtil.isAccessible(member, myTargetClass, modifierListCopies.get(member), element, accessObjectClass, null)) {
              newVisibility = newVisibility == null ? VisibilityUtil.getVisibilityStringToDisplay(member) : newVisibility;
              String message =
                RefactoringBundle.message("0.with.1.visibility.is.not.accesible.from.2", RefactoringUIUtil.getDescription(member, true),
                                          newVisibility, RefactoringUIUtil.getDescription(ConflictsUtil.getContainer(element), true));
              conflicts.add(message);
            }
          }
        }
      }
    }
  }

  public void doRun() {
    if (myMembersToMove.isEmpty()){
      String message = RefactoringBundle.message("no.members.selected");
      CommonRefactoringUtil.showErrorMessage(MoveMembersImpl.REFACTORING_NAME, message, HelpID.MOVE_MEMBERS, myProject);
      return;
    }
    if (canRefactor()) {
      super.doRun();
    }
  }

  private boolean canRefactor() {
    final String[] conflicts = analyzeMoveConflicts(myMembersToMove, myTargetClass, myNewVisibility);
    if (conflicts.length > 0) {
      ConflictsDialog dialog = new ConflictsDialog(myProject, conflicts);
      dialog.show();
      return dialog.isOK();
    }
    return true;
  }

  private static String[] analyzeMoveConflicts(@NotNull Set<PsiMember> membersToMove, final PsiClass targetClass, final String newVisibility) {
    final LinkedHashSet<String> conflicts = new LinkedHashSet<String>();
    for (final PsiMember member : membersToMove) {
      if (member instanceof PsiMethod) {
        PsiMethod method = (PsiMethod)member;
        if (hasMethod(targetClass, method)) {
          String message = RefactoringBundle.message("0.already.exists.in.the.target.class", RefactoringUIUtil.getDescription(method, false));
          message = ConflictsUtil.capitalize(message);
          conflicts.add(message);
        }
      }
      else if (member instanceof PsiField) {
        PsiField field = (PsiField)member;
        if (hasField(targetClass, field)) {
          String message = RefactoringBundle.message("0.already.exists.in.the.target.class", RefactoringUIUtil.getDescription(field, false));
          message = ConflictsUtil.capitalize(message);
          conflicts.add(message);
        }
      }
    }
    return analyzeAccessibilityConflicts(membersToMove, targetClass, conflicts, newVisibility);
  }

  public static String[] analyzeAccessibilityConflicts(@NotNull Set<PsiMember> membersToMove,
                                                final PsiClass targetClass,
                                                final LinkedHashSet<String> conflicts, String newVisibility) {
    if (VisibilityUtil.ESCALATE_VISIBILITY.equals(newVisibility)) { //Still need to check for access object
      newVisibility = PsiModifier.PUBLIC;
    }

    for (PsiMember member : membersToMove) {
      checkUsedElements(member, member, membersToMove, targetClass, conflicts);

      PsiModifierList modifierList = (PsiModifierList)member.getModifierList().copy();

      if (newVisibility != null) {
        try {
          RefactoringUtil.setVisibility(modifierList, newVisibility);
        }
        catch (IncorrectOperationException ex) {
          /* do nothing and hope for the best */
        }
      }
      JavaPsiFacade manager = JavaPsiFacade.getInstance(member.getProject());
      for (PsiReference psiReference : ReferencesSearch.search(member)) {
        PsiElement ref = psiReference.getElement();
        if (!RefactoringHierarchyUtil.willBeInTargetClass(ref, membersToMove, targetClass, false)) {
          //Check for target class accessibility
          if (!manager.getResolveHelper().isAccessible(targetClass, targetClass.getModifierList(), ref, null, null)) {
            String message = RefactoringBundle.message("0.is.1.and.will.not.be.accessible.from.2.in.the.target.class",
                                                       RefactoringUIUtil.getDescription(targetClass, true),
                                                       VisibilityUtil.getVisibilityStringToDisplay(targetClass),
                                                       RefactoringUIUtil.getDescription(ConflictsUtil.getContainer(ref), true));
            message = ConflictsUtil.capitalize(message);
            conflicts.add(message);
          }
          //check for member accessibility
          else if (!manager.getResolveHelper().isAccessible(member, modifierList, ref, null, null)) {
            String message = RefactoringBundle.message("0.is.1.and.will.not.be.accessible.from.2.in.the.target.class",
                                                       RefactoringUIUtil.getDescription(member, true),
                                                       VisibilityUtil.getVisibilityStringToDisplay(member),
                                                       RefactoringUIUtil.getDescription(ConflictsUtil.getContainer(ref), true));
            message = ConflictsUtil.capitalize(message);
            conflicts.add(message);
          }
        }
      }
    }
    return ArrayUtil.toStringArray(conflicts);
  }

  private static void checkUsedElements(PsiMember member, PsiElement scope, @NotNull Set<PsiMember> membersToMove, PsiClass newContext, LinkedHashSet<String> conflicts) {
    if(scope instanceof PsiReferenceExpression) {
      PsiReferenceExpression refExpr = (PsiReferenceExpression)scope;
      PsiElement refElement = refExpr.resolve();
      if (refElement instanceof PsiMember) {
        if (!RefactoringHierarchyUtil.willBeInTargetClass(refElement, membersToMove, newContext, false)){
          PsiExpression qualifier = refExpr.getQualifierExpression();
          PsiClass accessClass = (PsiClass)(qualifier != null ? PsiUtil.getAccessObjectClass(qualifier).getElement() : null);
          checkAccessibility((PsiMember)refElement, newContext, accessClass, member, conflicts);
        }
      }
    }
    else if (scope instanceof PsiNewExpression) {
      final PsiMethod refElement = ((PsiNewExpression)scope).resolveConstructor();
      if (refElement != null) {
        if (!RefactoringHierarchyUtil.willBeInTargetClass(refElement, membersToMove, newContext, false)) {
          checkAccessibility(refElement, newContext, null, member, conflicts);
        }
      }
    }
    else if (scope instanceof PsiJavaCodeReferenceElement) {
      PsiJavaCodeReferenceElement refExpr = (PsiJavaCodeReferenceElement)scope;
      PsiElement refElement = refExpr.resolve();
      if (refElement instanceof PsiMember) {
        if (!RefactoringHierarchyUtil.willBeInTargetClass(refElement, membersToMove, newContext, false)){
          checkAccessibility((PsiMember)refElement, newContext, null, member, conflicts);
        }
      }
    }

    PsiElement[] children = scope.getChildren();
    for (PsiElement child : children) {
      if (!(child instanceof PsiWhiteSpace)) {
        checkUsedElements(member, child, membersToMove, newContext, conflicts);
      }
    }
  }

  private static void checkAccessibility(PsiMember refMember,
                                         PsiClass newContext,
                                         PsiClass accessClass,
                                         PsiMember member,
                                         LinkedHashSet<String> conflicts) {
    if (!PsiUtil.isAccessible(refMember, newContext, accessClass)) {
      String message = RefactoringBundle.message("0.is.1.and.will.not.be.accessible.from.2.in.the.target.class",
                                                 RefactoringUIUtil.getDescription(refMember, true),
                                                 VisibilityUtil.getVisibilityStringToDisplay(refMember),
                                                 RefactoringUIUtil.getDescription(member, false));
      message = ConflictsUtil.capitalize(message);
      conflicts.add(message);
    }
  }

  private static boolean hasMethod(PsiClass targetClass, PsiMethod method) {
    PsiMethod[] targetClassMethods = targetClass.getMethods();
    for (PsiMethod method1 : targetClassMethods) {
      if (MethodSignatureUtil.areSignaturesEqual(method.getSignature(PsiSubstitutor.EMPTY),
                                                 method1.getSignature(PsiSubstitutor.EMPTY))) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasField(PsiClass targetClass, PsiField field) {
    String fieldName = field.getName();
    PsiField[] targetClassFields = targetClass.getFields();
    for (PsiField targetClassField : targetClassFields) {
      if (fieldName.equals(targetClassField.getName())) {
        return true;
      }
    }
    return false;
  }

  public List<PsiElement> getMembers() {
    return new ArrayList<PsiElement>(myMembersToMove);
  }

  public PsiClass getTargetClass() {
    return myTargetClass;
  }


  private static class MyUsageInfo extends MoveRenameUsageInfo {
    public final PsiClass qualifierClass;
    public final PsiElement reference;
    public final PsiMember member;

    public MyUsageInfo(PsiMember member, PsiElement element, PsiClass qualifierClass, PsiElement highlightElement, final PsiReference ref) {
      super(highlightElement, ref, member);
      this.member = member;
      this.qualifierClass = qualifierClass;
      reference = element;
    }
  }

}
/*
 * Created by IntelliJ IDEA.
 * User: dsl
 * Date: 07.05.2002
 * Time: 11:17:31
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.intellij.refactoring.introduceParameter;

import com.intellij.codeInsight.ChangeContextUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.IntroduceParameterRefactoring;
import com.intellij.refactoring.ui.ConflictsDialog;
import com.intellij.refactoring.util.ConflictsUtil;
import com.intellij.refactoring.util.FieldConflictsResolver;
import com.intellij.refactoring.util.RefactoringUtil;
import com.intellij.refactoring.util.VisibilityUtil;
import com.intellij.refactoring.util.javadoc.MethodJavaDocHelper;
import com.intellij.refactoring.util.occurences.ExpressionOccurenceManager;
import com.intellij.refactoring.util.occurences.LocalVariableOccurenceManager;
import com.intellij.refactoring.util.occurences.OccurenceManager;
import com.intellij.refactoring.util.usageInfo.DefaultConstructorImplicitUsageInfo;
import com.intellij.refactoring.util.usageInfo.DefaultConstructorUsageCollector;
import com.intellij.refactoring.util.usageInfo.NoConstructorClassUsageInfo;
import com.intellij.usageView.FindUsagesCommand;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.intellij.usageView.UsageViewUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.HashSet;
import com.intellij.util.containers.HashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class IntroduceParameterProcessor extends BaseRefactoringProcessor {
  private static final Logger LOG = Logger.getInstance("#com.intellij.refactoring.introduceParameter.IntroduceParameterProcessor");

  private PsiMethod myMethodToReplaceIn;
  private PsiMethod myMethodToSearchFor;
  private PsiExpression myParameterInitializer;
  private final PsiExpression myExpressionToSearch;
  private final PsiLocalVariable myLocalVariable;
  private final boolean myRemoveLocalVariable;
  private String myParameterName;
  private boolean myReplaceAllOccurences;

  private int myReplaceFieldsWithGetters;
  private final boolean myDeclareFinal;
  private PsiType myForcedType;
  private PsiManager myManager;

  /**
   * if expressionToSearch is null, search for localVariable
   */
  public IntroduceParameterProcessor(Project project,
                                     PsiMethod methodToReplaceIn,
                                     PsiMethod methodToSearchFor,
                                     PsiExpression parameterInitializer,
                                     PsiExpression expressionToSearch,
                                     PsiLocalVariable localVariable,
                                     boolean removeLocalVariable,
                                     String parameterName,
                                     boolean replaceAllOccurences,
                                     int replaceFieldsWithGetters,
                                     boolean declareFinal,
                                     PsiType forcedType) {
    super(project);

    myMethodToReplaceIn = methodToReplaceIn;
    myMethodToSearchFor = methodToSearchFor;
    myParameterInitializer = parameterInitializer;
    myExpressionToSearch = expressionToSearch;

    myLocalVariable = localVariable;
    myRemoveLocalVariable = removeLocalVariable;
    myParameterName = parameterName;
    myReplaceAllOccurences = replaceAllOccurences;
    myReplaceFieldsWithGetters = replaceFieldsWithGetters;
    myDeclareFinal = declareFinal;
    myForcedType = forcedType;
    myManager = PsiManager.getInstance(project);
  }

  protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usages, FindUsagesCommand refreshCommand) {
    return new IntroduceParameterViewDescriptor(myMethodToSearchFor, usages, refreshCommand);
  }

  public PsiType getForcedType() {
    return myForcedType;
  }

  public void setForcedType(PsiType forcedType) {
    myForcedType = forcedType;
  }

  public int getReplaceFieldsWithGetters() {
    return myReplaceFieldsWithGetters;
  }

  public void setReplaceFieldsWithGetters(int replaceFieldsWithGetters) {
    myReplaceFieldsWithGetters = replaceFieldsWithGetters;
  }

  protected UsageInfo[] findUsages() {
    ArrayList<UsageInfo> result = new ArrayList<UsageInfo>();
    PsiSearchHelper helper = myManager.getSearchHelper();

    PsiMethod[] overridingMethods = helper.findOverridingMethods(myMethodToSearchFor, GlobalSearchScope.projectScope(myProject), true);
    for (int j = 0; j < overridingMethods.length; j++) {
      result.add(new UsageInfo(overridingMethods[j]));
    }

    if(myMethodToSearchFor.isConstructor()) {
      final PsiParameter[] parameters = myMethodToSearchFor.getParameterList().getParameters();
      if(parameters.length == 0) {
        addImplicitDefaultConstructorUsages(result, myMethodToSearchFor.getContainingClass());
      }
    }

    PsiReference[] refs = helper.findReferencesIncludingOverriding(myMethodToSearchFor, GlobalSearchScope.projectScope(myProject), true);

    int i;
    for (i = 0; i < refs.length; i++) {
      PsiElement ref = refs[i].getElement();
      if (!insideMethodToBeReplaced(ref)) {
        result.add(new ExternalUsageInfo(ref));
      }
      else {
        result.add(new ChangedMethodCallInfo(ref));
      }
    }

    if (myReplaceAllOccurences) {
      PsiElement[] exprs;
      final OccurenceManager occurenceManager;
      if(myLocalVariable == null) {
        occurenceManager = new ExpressionOccurenceManager(myExpressionToSearch, myMethodToReplaceIn, null);
      } else {
        occurenceManager = new LocalVariableOccurenceManager(myLocalVariable, null);
      }
      exprs = occurenceManager.getOccurences();
      for (i = 0; i < exprs.length; i++) {
        PsiElement expr = exprs[i];

        result.add(new InternalUsageInfo(expr));
      }
    }
    else {
      if (myExpressionToSearch != null) {
        result.add(new InternalUsageInfo(myExpressionToSearch));
      }
    }

    final UsageInfo[] usageInfos = result.toArray(new UsageInfo[0]);
    return UsageViewUtil.removeDuplicatedUsages(usageInfos);
  }

  private void addImplicitDefaultConstructorUsages(final ArrayList<UsageInfo> result, PsiClass aClass) {
    final RefactoringUtil.ImplicitConstructorUsageVisitor implicitConstructorUsageVistor
            = new DefaultConstructorUsageCollector(result);

    RefactoringUtil.visitImplicitConstructorUsages(aClass, implicitConstructorUsageVistor);
  }

  private static class ReferencedElementsCollector extends PsiRecursiveElementVisitor {
    private Set<PsiElement> myResult = new HashSet<PsiElement>();

    public void visitReferenceExpression(PsiReferenceExpression expression) {
      visitReferenceElement(expression);
    }

    public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
      super.visitReferenceElement(reference);
      final PsiElement element = reference.resolve();
      if (element != null) {
        myResult.add(element);
      }
    }
  }

  protected boolean preprocessUsages(UsageInfo[][] usages) {
    ArrayList<String> conflicts = new ArrayList<String> ();

    AnySameNameVariables anySameNameVariables = new AnySameNameVariables();
    myMethodToReplaceIn.accept(anySameNameVariables);
    if (anySameNameVariables.getConflict() != null) {
      conflicts.add(anySameNameVariables.getConflict());
    }

    final UsageInfo[] usageArray = usages[0];
    detectAccessibilityConflicts(usageArray, conflicts);

    if (myParameterInitializer != null && !myMethodToReplaceIn.hasModifierProperty(PsiModifier.PRIVATE)) {
      final AnySupers anySupers = new AnySupers();
      myParameterInitializer.accept(anySupers);
      if (anySupers.isResult()) {
        final UsageInfo[] u = usages[0];
        for (int i = 0; i < u.length; i++) {
          UsageInfo usageInfo = u[i];
          if (!(usageInfo.getElement() instanceof PsiMethod) && !(usageInfo instanceof InternalUsageInfo)) {
            final PsiElement element = usageInfo.getElement();
            if (!PsiTreeUtil.isAncestor(myMethodToReplaceIn.getContainingClass(), element, false)) {
              conflicts.add("Parameter initializer contains " + ConflictsUtil.htmlEmphasize("super")
                      + ", but not all calls to method are in its class.");
              break;
            }
          }
        }
      }
    }
    if(conflicts.size() > 0 && myPrepareSuccessfulSwingThreadCallback!= null) {
      ConflictsDialog dialog = new ConflictsDialog(conflicts.toArray(new String[conflicts.size()]), myProject);
      dialog.show();
      if(!dialog.isOK()) return false;
    }

    prepareSuccessful();
    return true;
  }

  private void detectAccessibilityConflicts(final UsageInfo[] usageArray, ArrayList<String> conflicts) {
    if (myParameterInitializer != null) {
      final ReferencedElementsCollector collector = new ReferencedElementsCollector();
      myParameterInitializer.accept(collector);
      final Set<PsiElement> result = collector.myResult;
      if (!result.isEmpty()) {
        for (int i = 0; i < usageArray.length; i++) {
          final UsageInfo usageInfo = usageArray[i];
          if (usageInfo instanceof ExternalUsageInfo && RefactoringUtil.isMethodUsage(usageInfo.getElement())) {
            final PsiElement place = usageInfo.getElement();
            for (Iterator<PsiElement> iterator = result.iterator(); iterator.hasNext();) {
              final PsiElement element = iterator.next();
              if (element instanceof PsiMember &&
                  !myManager.getResolveHelper().isAccessible((PsiMember)element, place, null)) {
                String message =
                  ConflictsUtil.getDescription(element, true) + " is not accesible from " +
                  ConflictsUtil.getDescription(ConflictsUtil.getContainer(place), true) + ". " +
                  "Value for introduced parameter in that method call will be incorrect.";
                conflicts.add(message);
              }
            }
          }
        }
      }
    }
  }


  public class AnySupers extends PsiRecursiveElementVisitor {
    private boolean myResult = false;
    public void visitSuperExpression(PsiSuperExpression expression) {
      myResult = true;
    }

    public boolean isResult() {
      return myResult;
    }

    public void visitReferenceExpression(PsiReferenceExpression expression) {
      visitElement(expression);
    }
  }

  public class AnySameNameVariables extends PsiRecursiveElementVisitor {
    private String conflict = null;

    public String getConflict() {
      return conflict;
    }

    public void visitVariable(PsiVariable variable) {
      if (variable == myLocalVariable) return;
      if (variable.getName().equals(myParameterName)) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("There is already a ");
        buffer.append(ConflictsUtil.getDescription(variable, true));
        buffer.append(". It will conflict with an introduced parameter");

        conflict = ConflictsUtil.capitalize(buffer.toString());
      }
    }

    public void visitReferenceExpression(PsiReferenceExpression expression) {
    }

    public void visitElement(PsiElement element) {
      if(conflict != null) return;
      super.visitElement(element);
    }
  }

  private boolean insideMethodToBeReplaced(PsiElement methodUsage) {
    PsiElement parent = methodUsage.getParent();
    while(parent != null) {
      if(parent.equals(myMethodToReplaceIn))
        return true;
      parent = parent.getParent();
    }
    return false;
  }

  protected void refreshElements(PsiElement[] elements) {
  }

  protected void performRefactoring(UsageInfo[] usages) {
    try {
      PsiElementFactory factory = myMethodToReplaceIn.getManager().getElementFactory();

      PsiType initializerType = getInitializerType(myForcedType, myParameterInitializer, myLocalVariable);


      // Converting myParameterInitializer
      if (myParameterInitializer != null) {
        myParameterInitializer =
                RefactoringUtil.convertInitializerToNormalExpression(myParameterInitializer, initializerType);
      } else {
        LOG.assertTrue(myLocalVariable != null);
        myParameterInitializer = factory.createExpressionFromText(myLocalVariable.getName(), myLocalVariable);
      }


      // Changing external occurences (the tricky part)
      ChangeContextUtil.encodeContextInfo(myParameterInitializer, true);
      for (int i = 0; i < usages.length; i++) {
        UsageInfo usage = usages[i];

        if (!(usage instanceof InternalUsageInfo)) {
          if(usage instanceof DefaultConstructorImplicitUsageInfo) {
            addSuperCall(((DefaultConstructorImplicitUsageInfo) usage).getConstructor());
          } else if(usage instanceof NoConstructorClassUsageInfo) {
            addDefaultConstructor(((NoConstructorClassUsageInfo) usage).getPsiClass());
          } else if (usage.getElement() instanceof PsiMethod) {
            if (!myManager.areElementsEquivalent(usage.getElement(), myMethodToReplaceIn)) {
              changeMethodSignatureAndResolveFieldConflicts((PsiMethod)usage.getElement(), initializerType);
            }
          }
          else {
            changeExternalUsage(usage);
          }
        }
      }
      // Changing signature of initial method
      // (signature of myMethodToReplaceIn will be either changed now or have already been changed)
      LOG.assertTrue(initializerType.isValid());
      final FieldConflictsResolver fieldConflictsResolver =
              new FieldConflictsResolver(myParameterName, myMethodToReplaceIn.getBody());
      changeMethodSignature(myMethodToReplaceIn, initializerType);
      if (myMethodToSearchFor != myMethodToReplaceIn) {
        changeMethodSignatureAndResolveFieldConflicts(myMethodToSearchFor, initializerType);
      }
      ChangeContextUtil.clearContextInfo(myParameterInitializer);

      // Replacing expression occurences
      for (int j = 0; j < usages.length; j++) {
        UsageInfo usage = usages[j];

        if(usage instanceof ChangedMethodCallInfo) {
          PsiElement element = usage.getElement();

          processChangedMethodCall(element);
        }
        else if (usage instanceof InternalUsageInfo) {
          PsiElement element = usage.getElement();
          if(element instanceof PsiExpression) {
            element = RefactoringUtil.outermostParenthesizedExpression((PsiExpression) element);
          }
          PsiElement newExpr = factory.createExpressionFromText(myParameterName, element);
          element.replace(newExpr);
        }
      }

      if(myLocalVariable != null && myRemoveLocalVariable) {
        myLocalVariable.normalizeDeclaration();
        myLocalVariable.getParent().delete();
      }
      fieldConflictsResolver.fix();
    }
    catch (IncorrectOperationException ex) {
      LOG.assertTrue(false);
    }
  }

  private void addDefaultConstructor(PsiClass aClass) throws IncorrectOperationException {
    if (!(aClass instanceof PsiAnonymousClass)) {
      final PsiElementFactory factory = myManager.getElementFactory();
      PsiMethod constructor = factory.createMethodFromText(aClass.getName() + "(){}", aClass);
      constructor = (PsiMethod) CodeStyleManager.getInstance(myProject).reformat(constructor);
      constructor = (PsiMethod) aClass.add(constructor);
      constructor.getModifierList().setModifierProperty(VisibilityUtil.getVisibilityModifier(aClass.getModifierList()), true);
      addSuperCall(constructor);
    }
  }

  private void addSuperCall(PsiMethod constructor) throws IncorrectOperationException {
    final PsiElementFactory factory = myManager.getElementFactory();
    PsiExpressionStatement superCall =
            (PsiExpressionStatement) factory.createStatementFromText("super();", constructor);
    superCall = (PsiExpressionStatement) CodeStyleManager.getInstance(myProject).reformat(superCall);
    final PsiStatement[] statements = constructor.getBody().getStatements();
    if(statements.length > 0) {
      superCall = (PsiExpressionStatement) constructor.getBody().addBefore(superCall, statements[0]);
    } else {
      superCall = (PsiExpressionStatement) constructor.getBody().add(superCall);
    }
    PsiCallExpression expression = (PsiCallExpression) superCall.getExpression();
    fixActualArgumentsList(expression);
  }

  private void fixActualArgumentsList(PsiCallExpression expression) throws IncorrectOperationException {
    PsiExpression newArg = (PsiExpression) expression.getArgumentList().add(myParameterInitializer);
    new OldReferencesResolver(expression, newArg, myReplaceFieldsWithGetters).resolve();
  }

  static PsiType getInitializerType(PsiType forcedType, PsiExpression parameterInitializer, PsiLocalVariable localVariable) {
    final PsiType initializerType;
    if (forcedType == null) {
      if (parameterInitializer == null) {
        if (localVariable != null) {
          initializerType = localVariable.getType();
        } else {
          LOG.assertTrue(false);
          initializerType = null;
        }
      } else {
        if (localVariable == null) {
          initializerType = RefactoringUtil.getTypeByExpressionWithExpectedType(parameterInitializer);
        } else {
          initializerType = localVariable.getType();
        }
      }
    } else {
      initializerType = forcedType;
    }
    return initializerType;
  }

  private void processChangedMethodCall(PsiElement element)
          throws IncorrectOperationException {
    if(element.getParent() instanceof PsiMethodCallExpression) {
      PsiMethodCallExpression methodCall = (PsiMethodCallExpression) element.getParent();

      PsiElementFactory factory = methodCall.getManager().getElementFactory();
      PsiElement newArg = factory.createExpressionFromText(myParameterName, null);
      PsiExpressionList argList = methodCall.getArgumentList();
      PsiExpression[] exprs = argList.getExpressions();

      if (exprs.length > 0) {
        argList.addAfter(newArg, exprs[exprs.length - 1]);
      }
      else {
        argList.add(newArg);
      }
    }
    else {
      LOG.assertTrue(false);
    }
  }

  private void changeExternalUsage(UsageInfo usage) throws IncorrectOperationException {
    if(!RefactoringUtil.isMethodUsage(usage.getElement()))
      return;

    PsiCallExpression callExpression =
            RefactoringUtil.getCallExpressionByMethodReference((PsiJavaCodeReferenceElement) usage.getElement());
    PsiExpressionList argList = callExpression.getArgumentList();
    PsiExpression[] oldArgs = argList.getExpressions();

    PsiExpression newArg;
    final PsiExpression anchor;
    if (!myMethodToSearchFor.isVarArgs()) {
      anchor = getLast(oldArgs);
    }
    else {
      final PsiParameter[] parameters = myMethodToSearchFor.getParameterList().getParameters();
      if (parameters.length > oldArgs.length) {
        anchor = getLast(oldArgs);
      }
      else {
        LOG.assertTrue(parameters.length > 0);
        final int lastNonVararg = parameters.length - 2;
        anchor = lastNonVararg >= 0 ? oldArgs[lastNonVararg] : null;
      }
    }
    newArg = (PsiExpression) argList.addAfter(myParameterInitializer, anchor);
    ChangeContextUtil.decodeContextInfo(newArg, null, null);

    // here comes some postprocessing...
    new OldReferencesResolver(callExpression, newArg, myReplaceFieldsWithGetters).resolve();
  }

  private PsiExpression getLast(PsiExpression[] oldArgs) {
    PsiExpression anchor;
    if (oldArgs.length > 0) {
      anchor = oldArgs[oldArgs.length - 1];
    }
    else {
      anchor = null;
    }
    return anchor;
  }

  static PsiElement getClassContainingResolve (final ResolveResult result) {
    final PsiElement elem = result.getElement ();
    if (elem != null) {
      if (elem instanceof PsiLocalVariable || elem instanceof PsiParameter) {
        return PsiTreeUtil.getParentOfType (elem, PsiClass.class);
      }
      else {
        return result.getCurrentFileResolveScope();
      }
    }
    return null;
  }


  private class OldReferencesResolver {
    private PsiCallExpression myContext;
    private PsiExpression myExpr;
    private HashMap<PsiExpression,String> myTempVars;
    private PsiExpression myInstanceRef;
    private PsiExpression[] myActualArgs;
    private final int myReplaceFieldsWithGetters;

    public OldReferencesResolver(PsiCallExpression context, PsiExpression expr, int replaceFieldsWithGetters) {
      myContext = context;
      myExpr = expr;
      myTempVars = new com.intellij.util.containers.HashMap<PsiExpression, String>();
      myActualArgs = myContext.getArgumentList().getExpressions();
      if(myContext instanceof PsiMethodCallExpression) {
        myInstanceRef = ((PsiMethodCallExpression)myContext).getMethodExpression().getQualifierExpression();
      }
      else {
        // todo: distinguish between 'null' instance ref (when instance ref is "this")
        // and unavailable instance ref (as in 'new A()')
        myInstanceRef = null;
      }
      myReplaceFieldsWithGetters = replaceFieldsWithGetters;
    }

    public void resolve() throws IncorrectOperationException {
      resolveOldReferences(myExpr,  myParameterInitializer);

      Set<Map.Entry<PsiExpression,String>> mappingsSet = myTempVars.entrySet();

      PsiElementFactory factory = myContext.getManager().getElementFactory();

      for (Iterator<Map.Entry<PsiExpression,String>> iterator = mappingsSet.iterator(); iterator.hasNext();) {
        Map.Entry<PsiExpression,String> entry = iterator.next();

        PsiExpression oldRef = entry.getKey();
        PsiElement newRef = factory.createExpressionFromText(entry.getValue(), null);
        oldRef.replace(newRef);
      }
    }


    private void resolveOldReferences(PsiElement expr, PsiElement oldExpr)
            throws IncorrectOperationException {
      if (expr == null || oldExpr == null) return;
      PsiManager manager = myParameterInitializer.getManager();
      PsiElementFactory factory = manager.getElementFactory();
      PsiElement newExpr = expr;  // references continue being resolved in the children of newExpr

      if (oldExpr instanceof PsiReferenceExpression) {
        final PsiReferenceExpression oldRef = (PsiReferenceExpression) oldExpr;
        final ResolveResult adv = oldRef.advancedResolve(false);
        final PsiElement scope = getClassContainingResolve (adv);
        final PsiClass clss = PsiTreeUtil.getParentOfType (oldExpr, PsiClass.class);
        if (PsiTreeUtil.isAncestor (clss, scope, false)) {

          final PsiElement subj = adv.getElement ();


          // Parameters
          if (subj instanceof PsiParameter) {
            PsiParameterList formalArgList = myMethodToReplaceIn.getParameterList();
            if (formalArgList != null) {
              PsiParameter[] formalArgs = formalArgList.getParameters();

              int index;
              for (index = 0; index < formalArgs.length; index++) {
                if (subj.equals(formalArgs[index])) break;
              }
              if (index < formalArgs.length) {
                PsiExpression actualArg;
                try {
                  actualArg = myActualArgs[index];
                }
                catch (NullPointerException $ex) {
                  return;
                }
                catch (ArrayIndexOutOfBoundsException $ex) {
                  return;
                }
                int copyingSafetyLevel = RefactoringUtil.verifySafeCopyExpression(actualArg);
                if(copyingSafetyLevel == RefactoringUtil.EXPR_COPY_PROHIBITED) {
                  actualArg = factory.createExpressionFromText(getTempVar(actualArg), null);
                }
                newExpr = newExpr.replace(actualArg);
              }
            }
          }
          // "naked" field and methods  (should become qualified)
          else if ((subj instanceof PsiField || subj instanceof PsiMethod)
                   && oldRef.getQualifierExpression() == null) {

            boolean isStatic =
              ((subj instanceof PsiField)
               && ((PsiField) subj).hasModifierProperty(PsiModifier.STATIC))
              || ((subj instanceof PsiMethod)
                  && ((PsiMethod) subj).hasModifierProperty(PsiModifier.STATIC));

            if (myInstanceRef != null && !isStatic) {
              String name = ((PsiNamedElement) subj).getName();
              PsiReferenceExpression newRef = (PsiReferenceExpression) factory.createExpressionFromText("a." + name, null);
              newRef = (PsiReferenceExpression) CodeStyleManager.getInstance(myProject).reformat(newRef);

              PsiExpression instanceRef = getInstanceRef(factory);

              newRef.getQualifierExpression().replace(instanceRef);
              newRef = (PsiReferenceExpression) expr.replace(newRef);
              newExpr = newRef.getReferenceNameElement();
            }
          }

          if (subj instanceof PsiField) {
            // probably replacing field with a getter
            if (myReplaceFieldsWithGetters != IntroduceParameterRefactoring.REPLACE_FIELDS_WITH_GETTERS_NONE) {
              if (myReplaceFieldsWithGetters == IntroduceParameterRefactoring.REPLACE_FIELDS_WITH_GETTERS_ALL ||
                  (myReplaceFieldsWithGetters == IntroduceParameterRefactoring.REPLACE_FIELDS_WITH_GETTERS_INACCESSIBLE &&
                   !manager.getResolveHelper().isAccessible((PsiMember)subj, newExpr, null))) {
                newExpr = replaceFieldWithGetter(newExpr, (PsiField) subj);
              }
            }
          }
        }
      }
      else if (oldExpr instanceof PsiThisExpression) {
        if (myInstanceRef != null) {
          expr.replace(getInstanceRef(factory));
        }
        return;
      }
      else if (oldExpr instanceof PsiSuperExpression) {
        if (myInstanceRef != null) {
          expr.replace(getInstanceRef(factory));
        }
        return;
      }

      PsiElement[] oldChildren = oldExpr.getChildren();
      PsiElement[] newChildren = newExpr.getChildren();

      if (oldChildren != null && newChildren != null & oldChildren.length == newChildren.length) {
        for (int i = 0; i < oldChildren.length; i++) {
          PsiElement oldChild = oldChildren[i];
          PsiElement newChild = newChildren[i];

          resolveOldReferences(newChild, oldChild);
        }
      }
    }

    private PsiExpression getInstanceRef(PsiElementFactory factory) throws IncorrectOperationException {
      int copyingSafetyLevel = RefactoringUtil.verifySafeCopyExpression(myInstanceRef);

      PsiExpression instanceRef = myInstanceRef;
      if(copyingSafetyLevel == RefactoringUtil.EXPR_COPY_PROHIBITED) {
        instanceRef = factory.createExpressionFromText(getTempVar(myInstanceRef), null);
      }
      return instanceRef;
    }

    private String getTempVar(PsiExpression expr) throws IncorrectOperationException {
      String id = myTempVars.get(expr);
      if(id != null) {
        return id;
      }
      else {
        id = RefactoringUtil.createTempVar(expr, myContext, true);
        myTempVars.put(expr, id);
        return id;
      }
    }
  }

  private PsiElement replaceFieldWithGetter(PsiElement expr, PsiField psiField)
          throws IncorrectOperationException {
    if (RefactoringUtil.isAssignmentLHS(expr)) {
      // todo: warning
      return expr;
    }
    PsiElement newExpr = expr;

    PsiMethod getterPrototype = PropertyUtil.generateGetterPrototype(psiField);

    PsiMethod getter = psiField.getContainingClass().findMethodBySignature(getterPrototype, true);

    if (getter != null) {

      if (psiField.getManager().getResolveHelper().isAccessible(getter, newExpr, null)) {
        PsiElementFactory factory = newExpr.getManager().getElementFactory();
        String id = getter.getName();
        PsiMethodCallExpression getterCall =
                (PsiMethodCallExpression) factory.createExpressionFromText(id + "()", null);
        getterCall = (PsiMethodCallExpression) CodeStyleManager.getInstance(myProject).reformat(getterCall);
        if(newExpr.getParent() != null) {
          newExpr = newExpr.replace(getterCall);
        }
        else {
          newExpr = getterCall;
        }
      }
      else {
        // todo: warning
      }
    }

    return newExpr;
  }


  protected String getCommandName() {
    return "Introducing parameter to " + UsageViewUtil.getDescriptiveName(myMethodToReplaceIn);
  }

  private void changeMethodSignatureAndResolveFieldConflicts(PsiMethod overridingMethod, PsiType parameterType) throws IncorrectOperationException {
    final FieldConflictsResolver fieldConflictsResolver = new FieldConflictsResolver(myParameterName, overridingMethod.getBody());
    changeMethodSignature(overridingMethod, parameterType);
    fieldConflictsResolver.fix();
  }

  private void changeMethodSignature(PsiMethod methodToReplaceIn, PsiType initializerType) throws IncorrectOperationException {
    final MethodJavaDocHelper javaDocHelper = new MethodJavaDocHelper(methodToReplaceIn);
    PsiManager manager = methodToReplaceIn.getManager();
    PsiElementFactory factory = manager.getElementFactory();

    PsiParameter parameter = factory.createParameter(myParameterName, initializerType);
    parameter.getModifierList().setModifierProperty(PsiModifier.FINAL, myDeclareFinal);
    final PsiParameter anchorParameter = getAnchorParameter(methodToReplaceIn);
    PsiParameterList parameterList = methodToReplaceIn.getParameterList();
    parameter = (PsiParameter)parameterList.addAfter(parameter, anchorParameter);
    manager.getCodeStyleManager().shortenClassReferences(parameter);
    final PsiDocTag tagForAnchorParameter = javaDocHelper.getTagForParameter(anchorParameter);
    javaDocHelper.addParameterAfter(myParameterName, tagForAnchorParameter);
  }

  private PsiParameter getAnchorParameter(PsiMethod methodToReplaceIn) {
    PsiParameterList parameterList = methodToReplaceIn.getParameterList();
    final PsiParameter anchorParameter;
    final PsiParameter[] parameters = parameterList.getParameters();
    final int length = parameters.length;
    if (!methodToReplaceIn.isVarArgs()) {
      anchorParameter = length > 0 ? parameters[length-1] : null;
    }
    else {
      LOG.assertTrue(length > 0);
      LOG.assertTrue(parameters[length-1].isVarArgs());
      anchorParameter = length > 1 ? parameters[length - 1] : null;
    }
    return anchorParameter;
  }

}
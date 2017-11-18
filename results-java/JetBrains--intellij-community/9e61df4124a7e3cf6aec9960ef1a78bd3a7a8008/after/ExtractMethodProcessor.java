package com.intellij.refactoring.extractMethod;

import com.intellij.codeInsight.ChangeContextUtil;
import com.intellij.codeInsight.ExceptionUtil;
import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.ide.DataManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.controlFlow.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiFormatUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.extractMethodObject.ExtractMethodObjectHandler;
import com.intellij.refactoring.util.*;
import com.intellij.refactoring.util.classMembers.ElementNeedsThis;
import com.intellij.refactoring.util.duplicates.DuplicatesFinder;
import com.intellij.refactoring.util.duplicates.Match;
import com.intellij.refactoring.util.duplicates.MatchProvider;
import com.intellij.refactoring.util.duplicates.VariableReturnValue;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashSet;
import com.intellij.util.containers.IntArrayList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ExtractMethodProcessor implements MatchProvider {
  private static final Logger LOG = Logger.getInstance("#com.intellij.refactoring.extractMethod.ExtractMethodProcessor");

  protected final Project myProject;
  private final Editor myEditor;
  protected final PsiElement[] myElements;
  private final PsiBlockStatement myEnclosingBlockStatement;
  private final PsiType myForcedReturnType;
  private final String myRefactoringName;
  protected final String myInitialMethodName;
  private final String myHelpId;

  private final PsiManager myManager;
  private final PsiElementFactory myElementFactory;
  private final CodeStyleManager myStyleManager;

  private PsiExpression myExpression;

  private PsiElement myCodeFragmentMember; // parent of myCodeFragment

  protected String myMethodName; // name for extracted method
  protected PsiType myReturnType; // return type for extracted method
  protected PsiTypeParameterList myTypeParameterList; //type parameter list of extracted method
  private ParameterTablePanel.VariableData[] myVariableDatum; // parameter data for extracted method
  protected PsiClassType[] myThrownExceptions; // exception to declare as thrown by extracted method
  protected boolean myStatic; // whether to declare extracted method static

  protected PsiClass myTargetClass; // class to create the extracted method in
  private PsiElement myAnchor; // anchor to insert extracted method after it

  protected ControlFlow myControlFlow; // control flow of myCodeFragment
  private int myFlowStart; // offset in control flow corresponding to the start of the code to be extracted
  private int myFlowEnd; // offset in control flow corresponding to position just after the end of the code to be extracted

  protected List<PsiVariable> myInputVariables; // input variables
  protected PsiVariable[] myOutputVariables; // output variables
  protected PsiVariable myOutputVariable; // the only output variable
  private Collection<PsiStatement> myExitStatements;

  private boolean myHasReturnStatement; // there is a return statement
  private boolean myHasReturnStatementOutput; // there is a return statement and its type is not void
  protected boolean myHasExpressionOutput; // extracted code is an expression with non-void type
  private boolean myNeedChangeContext; // target class is not immediate container of the code to be extracted

  private boolean myShowErrorDialogs = true;
  protected boolean myCanBeStatic;
  protected boolean myCanBeChainedConstructor;
  protected boolean myIsChainedConstructor;
  private DuplicatesFinder myDuplicatesFinder;
  private List<Match> myDuplicates;
  @Modifier private String myMethodVisibility = PsiModifier.PRIVATE;
  private boolean myGenerateConditionalExit;
  private PsiStatement myFirstExitStatementCopy;
  private PsiMethod myExtractedMethod;
  private PsiMethodCallExpression myMethodCall;

  public ExtractMethodProcessor(Project project,
                                Editor editor,
                                PsiElement[] elements,
                                PsiType forcedReturnType,
                                String refactoringName,
                                String initialMethodName,
                                String helpId) {
    myProject = project;
    myEditor = editor;
    if (elements.length != 1 || elements.length == 1 && !(elements[0] instanceof PsiBlockStatement)) {
      myElements = elements;
      myEnclosingBlockStatement = null;
    }
    else {
      myEnclosingBlockStatement = (PsiBlockStatement)elements[0];
      PsiElement[] codeBlockChildren = myEnclosingBlockStatement.getCodeBlock().getChildren();
      myElements = processCodeBlockChildren(codeBlockChildren);
    }
    myForcedReturnType = forcedReturnType;
    myRefactoringName = refactoringName;
    myInitialMethodName = initialMethodName;
    myHelpId = helpId;

    myManager = PsiManager.getInstance(myProject);
    myElementFactory = JavaPsiFacade.getInstance(myManager.getProject()).getElementFactory();
    myStyleManager = CodeStyleManager.getInstance(myProject);
  }

  private static PsiElement[] processCodeBlockChildren(PsiElement[] codeBlockChildren) {
    int resultLast = codeBlockChildren.length;

    if (codeBlockChildren.length == 0) return PsiElement.EMPTY_ARRAY;

    final PsiElement first = codeBlockChildren[0];
    int resultStart = 0;
    if (first instanceof PsiJavaToken && ((PsiJavaToken)first).getTokenType() == JavaTokenType.LBRACE) {
      resultStart++;
    }
    final PsiElement last = codeBlockChildren[codeBlockChildren.length - 1];
    if (last instanceof PsiJavaToken && ((PsiJavaToken)last).getTokenType() == JavaTokenType.RBRACE) {
      resultLast--;
    }
    final ArrayList<PsiElement> result = new ArrayList<PsiElement>();
    for (int i = resultStart; i < resultLast; i++) {
      PsiElement element = codeBlockChildren[i];
      if (!(element instanceof PsiWhiteSpace)) {
        result.add(element);
      }
    }

    return result.toArray(new PsiElement[result.size()]);
  }

  /**
   * Method for test purposes
   */
  public void setShowErrorDialogs(boolean showErrorDialogs) {
    myShowErrorDialogs = showErrorDialogs;
  }

  public void setChainedConstructor(final boolean isChainedConstructor) {
    myIsChainedConstructor = isChainedConstructor;
  }

  private boolean areExitStatementsTheSame() {
    if (myExitStatements.isEmpty()) return false;
    PsiStatement first = null;
    for (PsiStatement statement : myExitStatements) {
      if (first == null) {
        first = statement;
        continue;
      }
      if (!PsiEquivalenceUtil.areElementsEquivalent(first, statement)) return false;
    }

    myFirstExitStatementCopy = (PsiStatement)first.copy();
    return true;
  }

  /**
   * Invoked in atomic action
   */
  public boolean prepare() throws PrepareFailedException {
    myExpression = null;
    if (myElements.length == 1 && myElements[0] instanceof PsiExpression) {
      final PsiExpression expression = (PsiExpression)myElements[0];
      if (expression.getParent() instanceof PsiExpressionStatement) {
        myElements[0] = expression.getParent();
      }
      else {
        myExpression = expression;
      }
    }

    final PsiElement codeFragment = ControlFlowUtil.findCodeFragment(myElements[0]);
    myCodeFragmentMember = codeFragment.getParent();

    try {
      myControlFlow = ControlFlowFactory.getInstance(myProject).getControlFlow(codeFragment, new LocalsControlFlowPolicy(codeFragment), false, true);
    }
    catch (AnalysisCanceledException e) {
      throw new PrepareFailedException(RefactoringBundle.message("extract.method.control.flow.analysis.failed"), e.getErrorElement());
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(myControlFlow.toString());
    }

    calculateFlowStartAndEnd();

    IntArrayList exitPoints = new IntArrayList();

    myExitStatements = ControlFlowUtil.findExitPointsAndStatements(myControlFlow, myFlowStart, myFlowEnd, exitPoints, ControlFlowUtil.DEFAULT_EXIT_STATEMENTS_CLASSES);

    if (LOG.isDebugEnabled()) {
      LOG.debug("exit points:");
      for (int i = 0; i < exitPoints.size(); i++) {
        LOG.debug("  " + exitPoints.get(i));
      }
      LOG.debug("exit statements:");
      for (PsiStatement exitStatement : myExitStatements) {
        LOG.debug("  " + exitStatement);
      }
    }
    if (exitPoints.isEmpty()) {
      // if the fragment never exits assume as if it exits in the end
      exitPoints.add(myControlFlow.getEndOffset(myElements[myElements.length - 1]));
    }

    if (exitPoints.size() != 1) {
      if (!areExitStatementsTheSame()) {
        showMultipleExitPointsMessage();
        return false;
      }
      myGenerateConditionalExit = true;
    }
    else {
      myHasReturnStatement = myExpression == null && ControlFlowUtil.returnPresentBetween(myControlFlow, myFlowStart, myFlowEnd);
      //myHasReturnStatement = myExpression == null &&
      //                       (ControlFlowUtil.getCompletionReasons(myControlFlow, myFlowStart, myFlowEnd) & ControlFlowUtil.RETURN_COMPLETION_REASON) != 0;

    }

    myOutputVariables = ControlFlowUtil.getOutputVariables(myControlFlow, myFlowStart, myFlowEnd, exitPoints.toArray());
    if (myGenerateConditionalExit) {
      //variables declared in selected block used in return statements are to be considered output variables when extracting guard methods
      final Set<PsiVariable> outputVariables = new HashSet<PsiVariable>(Arrays.asList(myOutputVariables));
      for (PsiStatement statement : myExitStatements) {
        statement.accept(new JavaRecursiveElementVisitor() {

          @Override public void visitReferenceExpression(PsiReferenceExpression expression) {
            super.visitReferenceExpression(expression);
            final PsiElement resolved = expression.resolve();
            if (resolved instanceof PsiVariable) {
              final PsiVariable variable = (PsiVariable)resolved;
              if (isWrittenInside(variable)) {
                outputVariables.add(variable);
              }
            }
          }

          private boolean isWrittenInside(final PsiVariable variable) {
            final List<Instruction> instructions = myControlFlow.getInstructions();
            for (int i = myFlowStart; i < myFlowEnd; i++) {
              Instruction instruction = instructions.get(i);
              if (instruction instanceof WriteVariableInstruction &&
                  variable.equals(((WriteVariableInstruction)instruction).variable)) return true;
            }

            return false;
          }
        });
      }

      myOutputVariables = outputVariables.toArray(new PsiVariable[outputVariables.size()]);
    }

    final List<PsiVariable> inputVariables = ControlFlowUtil.getInputVariables(myControlFlow, myFlowStart, myFlowEnd);
    if (myGenerateConditionalExit) {
      List<PsiVariable> inputVariableList = new ArrayList<PsiVariable>(inputVariables);
      removeParametersUsedInExitsOnly(codeFragment, myExitStatements, myControlFlow, myFlowStart, myFlowEnd, inputVariableList);
      myInputVariables = inputVariableList;
    }
    else {
      myInputVariables = inputVariables;
    }

    //varargs variables go last, otherwise order is induced by original ordering
    Collections.sort(myInputVariables, new Comparator<PsiVariable>() {
      public int compare(final PsiVariable v1, final PsiVariable v2) {
        if (v1.getType() instanceof PsiEllipsisType) {
          return 1;
        }
        if (v2.getType() instanceof PsiEllipsisType) {
          return -1;
        }
        return v1.getTextOffset() - v2.getTextOffset();
      }
    });

    chooseTargetClass();

    PsiType expressionType = null;
    if (myExpression != null) {
      if (myForcedReturnType != null) {
        expressionType = myForcedReturnType;
      }
      else {
        expressionType = RefactoringUtil.getTypeByExpressionWithExpectedType(myExpression);
      }
    }
    if (expressionType == null) {
      expressionType = PsiType.VOID;
    }
    myHasExpressionOutput = expressionType != PsiType.VOID;

    PsiType returnStatementType = null;
    if (myHasReturnStatement) {
      returnStatementType = myCodeFragmentMember instanceof PsiMethod ? ((PsiMethod)myCodeFragmentMember).getReturnType() : null;
    }
    myHasReturnStatementOutput = returnStatementType != null && returnStatementType != PsiType.VOID;

    if (!myHasReturnStatementOutput && checkOutputVariablesCount()) {
      showMultipleOutputMessage(expressionType);
      return false;
    }

    myOutputVariable = myOutputVariables.length > 0 ? myOutputVariables[0] : null;
    if (myHasReturnStatementOutput) {
      myReturnType = returnStatementType;
    }
    else if (myOutputVariable != null) {
      myReturnType = myOutputVariable.getType();
    }
    else if (myGenerateConditionalExit) {
      myReturnType = PsiType.BOOLEAN;
    }
    else {
      myReturnType = expressionType;
    }

    PsiElement container = PsiTreeUtil.getParentOfType(myElements[0], PsiClass.class, PsiMethod.class);
    if (container instanceof PsiMethod) {
      myTypeParameterList = ((PsiMethod)container).getTypeParameterList();
    }
    myThrownExceptions = ExceptionUtil.getThrownCheckedExceptions(myElements);
    myStatic = shouldBeStatic();

    if (myTargetClass.getContainingClass() == null || myTargetClass.hasModifierProperty(PsiModifier.STATIC)) {
      ElementNeedsThis needsThis = new ElementNeedsThis(myTargetClass);
      for (int i = 0; i < myElements.length && !needsThis.usesMembers(); i++) {
        PsiElement element = myElements[i];
        element.accept(needsThis);
      }
      myCanBeStatic = !needsThis.usesMembers();
    }
    else {
      myCanBeStatic = false;
    }

    if (container instanceof PsiMethod) {
      checkLocalClasses((PsiMethod) container);
    }

    checkCanBeChainedConstructor();

    List<PsiElement> elements = new ArrayList<PsiElement>();
    for (PsiElement element : myElements) {
      if (!(element instanceof PsiWhiteSpace || element instanceof PsiComment)) {
        elements.add(element);
      }
    }

    if (myExpression != null) {
      myDuplicatesFinder = new DuplicatesFinder(elements.toArray(new PsiElement[elements.size()]), myInputVariables, new ArrayList<PsiVariable>());
      myDuplicates = myDuplicatesFinder.findDuplicates(myTargetClass);
    }
    else {
      myDuplicatesFinder = new DuplicatesFinder(elements.toArray(new PsiElement[elements.size()]), myInputVariables, myOutputVariable != null ? new VariableReturnValue(myOutputVariable) : null, Arrays.asList(myOutputVariables));
      myDuplicates = myDuplicatesFinder.findDuplicates(myTargetClass);
    }

    return true;
  }

  protected boolean checkOutputVariablesCount() {
    int outputCount = (myHasExpressionOutput ? 1 : 0) + (myGenerateConditionalExit ? 1 : 0) + myOutputVariables.length;
    return outputCount > 1;
  }

  private void checkCanBeChainedConstructor() {
    if (!(myCodeFragmentMember instanceof PsiMethod)) {
      return;
    }
    final PsiMethod method = (PsiMethod)myCodeFragmentMember;
    if (!method.isConstructor() || myReturnType != PsiType.VOID) {
      return;
    }
    final PsiCodeBlock body = method.getBody();
    if (body == null) return;
    final PsiStatement[] psiStatements = body.getStatements();
    if (psiStatements.length > 0 && myElements [0] == psiStatements [0]) {
      myCanBeChainedConstructor = true;
    }
  }

  private void checkLocalClasses(final PsiMethod container) throws PrepareFailedException {
    final List<PsiClass> localClasses = new ArrayList<PsiClass>();
    container.accept(new JavaRecursiveElementVisitor() {
      @Override public void visitClass(final PsiClass aClass) {
        localClasses.add(aClass);
      }

      @Override public void visitAnonymousClass(final PsiAnonymousClass aClass) {
        visitElement(aClass);
      }

      @Override public void visitTypeParameter(final PsiTypeParameter classParameter) {
        visitElement(classParameter);
      }
    });
    for(PsiClass localClass: localClasses) {
      final boolean classExtracted = isExtractedElement(localClass);
      final List<PsiElement> extractedReferences = new ArrayList<PsiElement>();
      final List<PsiElement> remainingReferences = new ArrayList<PsiElement>();
      ReferencesSearch.search(localClass).forEach(new Processor<PsiReference>() {
        public boolean process(final PsiReference psiReference) {
          final PsiElement element = psiReference.getElement();
          final boolean elementExtracted = isExtractedElement(element);
          if (elementExtracted && !classExtracted) {
            extractedReferences.add(element);
            return false;
          }
          if (!elementExtracted && classExtracted) {
            remainingReferences.add(element);
            return false;
          }
          return true;
        }
      });
      if (!extractedReferences.isEmpty()) {
        throw new PrepareFailedException("Cannot extract method because the selected code fragment uses local classes defined outside of the fragment", extractedReferences.get(0));
      }
      if (!remainingReferences.isEmpty()) {
        throw new PrepareFailedException("Cannot extract method because the selected code fragment defines local classes used outside of the fragment", remainingReferences.get(0));
      }
    }
  }

  private boolean isExtractedElement(final PsiElement element) {
    boolean isExtracted = false;
    for(PsiElement psiElement: myElements) {
      if (PsiTreeUtil.isAncestor(psiElement, element, false)) {
        isExtracted = true;
        break;
      }
    }
    return isExtracted;
  }

  private static void removeParametersUsedInExitsOnly(PsiElement codeFragment,
                                               Collection<PsiStatement> exitStatements,
                                               ControlFlow controlFlow,
                                               int startOffset,
                                               int endOffset,
                                               List<PsiVariable> inputVariables) {
    LocalSearchScope scope = new LocalSearchScope(codeFragment);
    Variables:
    for (Iterator<PsiVariable> iterator = inputVariables.iterator(); iterator.hasNext();) {
      PsiVariable variable = iterator.next();
      for (PsiReference ref : ReferencesSearch.search(variable, scope)) {
        PsiElement element = ref.getElement();
        int elementOffset = controlFlow.getStartOffset(element);
        if (elementOffset >= startOffset && elementOffset <= endOffset) {
          if (!isInExitStatements(element, exitStatements)) continue Variables;
        }
      }
      iterator.remove();
    }
  }

  private static boolean isInExitStatements(PsiElement element, Collection<PsiStatement> exitStatements) {
    for (PsiStatement exitStatement : exitStatements) {
      if (PsiTreeUtil.isAncestor(exitStatement, element, false)) return true;
    }
    return false;
  }

  private boolean shouldBeStatic() {
    for(PsiElement element: myElements) {
      final PsiExpressionStatement statement = PsiTreeUtil.getParentOfType(element, PsiExpressionStatement.class);
      if (statement != null && RefactoringUtil.isSuperOrThisCall(statement, true, true)) {
        return true;
      }
    }
    PsiElement codeFragmentMember = myCodeFragmentMember;
    while (codeFragmentMember != null && PsiTreeUtil.isAncestor(myTargetClass, codeFragmentMember, true)) {
      if (((PsiModifierListOwner)codeFragmentMember).hasModifierProperty(PsiModifier.STATIC)) {
        return true;
      }
      codeFragmentMember = PsiTreeUtil.getParentOfType(codeFragmentMember, PsiModifierListOwner.class, true);
    }
    return false;
  }

  public boolean showDialog(final boolean direct) {
    AbstractExtractDialog dialog = createExtractMethodDialog(direct);
    dialog.show();
    if (!dialog.isOK()) return false;
    apply(dialog);
    return true;
  }

  protected void apply(final AbstractExtractDialog dialog) {
    myMethodName = dialog.getChosenMethodName();
    myVariableDatum = dialog.getChosenParameters();
    myStatic |= dialog.isMakeStatic();
    myIsChainedConstructor = dialog.isChainedConstructor();
    myMethodVisibility = dialog.getVisibility();
  }

  protected AbstractExtractDialog createExtractMethodDialog(final boolean direct) {
    return new ExtractMethodDialog(myProject, myTargetClass, myInputVariables, myReturnType, myTypeParameterList,
                                                         myThrownExceptions, myStatic, myCanBeStatic, myCanBeChainedConstructor, myInitialMethodName,
                                                         myRefactoringName, myHelpId, myElements) {
      {
        init();
      }
      protected boolean areTypesDirected() {
        return direct;
      }
    };
  }

  public boolean showDialog() {
    return showDialog(true);
  }

  public void testRun() throws IncorrectOperationException {
    myMethodName = myInitialMethodName;
    myVariableDatum = new ParameterTablePanel.VariableData[myInputVariables.size()];
    for (int i = 0; i < myInputVariables.size(); i++) {
      myVariableDatum[i] = new ParameterTablePanel.VariableData(myInputVariables.get(i), myInputVariables.get(i).getType());
      myVariableDatum[i].passAsParameter = true;
      myVariableDatum[i].name = myInputVariables.get(i).getName();
    }

    doRefactoring();
  }

  /**
   * Invoked in command and in atomic action
   */
  public void doRefactoring() throws IncorrectOperationException {
    chooseAnchor();

    int col = myEditor.getCaretModel().getLogicalPosition().column;
    int line = myEditor.getCaretModel().getLogicalPosition().line;
    LogicalPosition pos = new LogicalPosition(0, 0);
    myEditor.getCaretModel().moveToLogicalPosition(pos);

    SearchScope processConflictsScope = myMethodVisibility.equals(PsiModifier.PRIVATE) ?
                                        new LocalSearchScope(myTargetClass) :
                                        GlobalSearchScope.projectScope(myProject);

    final Map<PsiMethodCallExpression, PsiMethod> overloadsResolveMap =
      ExtractMethodUtil.encodeOverloadTargets(myTargetClass, processConflictsScope, myMethodName, myCodeFragmentMember);

    doExtract();
    ExtractMethodUtil.decodeOverloadTargets(overloadsResolveMap, myExtractedMethod, myCodeFragmentMember);

    LogicalPosition pos1 = new LogicalPosition(line, col);
    myEditor.getCaretModel().moveToLogicalPosition(pos1);
    int offset = myMethodCall.getMethodExpression().getTextRange().getStartOffset();
    myEditor.getCaretModel().moveToOffset(offset);
    myEditor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
    myEditor.getSelectionModel().removeSelection();
    myEditor.getSelectionModel().removeSelection();
  }

  private void doExtract() throws IncorrectOperationException {
    renameInputVariables();

    PsiMethod newMethod = generateEmptyMethod(myThrownExceptions, myStatic);

    LOG.assertTrue(myElements[0].isValid());

    PsiCodeBlock body = newMethod.getBody();
    myMethodCall = generateMethodCall(null, true);

    LOG.assertTrue(myElements[0].isValid());

    if (myExpression == null) {
      String outVariableName = myOutputVariable != null ? getNewVariableName(myOutputVariable) : null;
      PsiReturnStatement returnStatement;
      if (myOutputVariable != null) {
        returnStatement = (PsiReturnStatement)myElementFactory.createStatementFromText("return " + outVariableName + ";", null);
      }
      else if (myGenerateConditionalExit) {
        returnStatement = (PsiReturnStatement)myElementFactory.createStatementFromText("return true;", null);
      }
      else {
        returnStatement = (PsiReturnStatement)myElementFactory.createStatementFromText("return;", null);
      }

      boolean hasNormalExit = false;
      PsiElement lastElement = myElements[myElements.length - 1];
      if (!(lastElement instanceof PsiReturnStatement || lastElement instanceof PsiBreakStatement ||
            lastElement instanceof PsiContinueStatement)) {
        hasNormalExit = true;
      }

      PsiStatement exitStatementCopy = null;
      // replace all exit-statements such as break's or continue's with appropriate return
      for (PsiStatement exitStatement : myExitStatements) {
        if (exitStatement instanceof PsiReturnStatement) {
          if (!myGenerateConditionalExit) continue;
        }
        else if (exitStatement instanceof PsiBreakStatement) {
          PsiStatement statement = ((PsiBreakStatement)exitStatement).findExitedStatement();
          if (statement == null) continue;
          int startOffset = myControlFlow.getStartOffset(statement);
          int endOffset = myControlFlow.getEndOffset(statement);
          if (myFlowStart <= startOffset && endOffset <= myFlowEnd) continue;
        }
        else if (exitStatement instanceof PsiContinueStatement) {
          PsiStatement statement = ((PsiContinueStatement)exitStatement).findContinuedStatement();
          if (statement == null) continue;
          int startOffset = myControlFlow.getStartOffset(statement);
          int endOffset = myControlFlow.getEndOffset(statement);
          if (myFlowStart <= startOffset && endOffset <= myFlowEnd) continue;
        }
        else {
          LOG.assertTrue(false, exitStatement);
          continue;
        }

        int index = -1;
        for (int j = 0; j < myElements.length; j++) {
          if (exitStatement.equals(myElements[j])) {
            index = j;
            break;
          }
        }
        if (exitStatementCopy == null) {
          if (needExitStatement(exitStatement)) {
            exitStatementCopy = (PsiStatement)exitStatement.copy();
          }
        }
        PsiElement result = exitStatement.replace(returnStatement);
        if (index >= 0) {
          myElements[index] = result;
        }
      }

      declareNecessaryVariablesInsideBody(myFlowStart, myFlowEnd, body);

      if (myNeedChangeContext) {
        for (PsiElement element : myElements) {
          ChangeContextUtil.encodeContextInfo(element, false);
        }
      }

      body.addRange(myElements[0], myElements[myElements.length - 1]);
      if (myGenerateConditionalExit) {
        body.add(myElementFactory.createStatementFromText("return false;", null));
      }
      else if (!myHasReturnStatement && hasNormalExit && myOutputVariable != null) {
        body.add(returnStatement);
      }

      if (myGenerateConditionalExit) {
        PsiIfStatement ifStatement = (PsiIfStatement)myElementFactory.createStatementFromText("if (a) b;", null);
        ifStatement = (PsiIfStatement)addToMethodCallLocation(ifStatement);
        myMethodCall = (PsiMethodCallExpression)ifStatement.getCondition().replace(myMethodCall);
        ifStatement.getThenBranch().replace(myFirstExitStatementCopy);
        CodeStyleManager.getInstance(myProject).reformat(ifStatement);
      }
      else if (myOutputVariable != null) {
        String name = myOutputVariable.getName();
        boolean toDeclare = isDeclaredInside(myOutputVariable);
        if (!toDeclare) {
          PsiExpressionStatement statement = (PsiExpressionStatement)myElementFactory.createStatementFromText(name + "=x;", null);
          statement = (PsiExpressionStatement)myStyleManager.reformat(statement);
          statement = (PsiExpressionStatement)addToMethodCallLocation(statement);
          PsiAssignmentExpression assignment = (PsiAssignmentExpression)statement.getExpression();
          myMethodCall = (PsiMethodCallExpression)assignment.getRExpression().replace(myMethodCall);
        }
        else {
          PsiDeclarationStatement statement =
            myElementFactory.createVariableDeclarationStatement(name, myOutputVariable.getType(), myMethodCall);
          statement = (PsiDeclarationStatement)addToMethodCallLocation(statement);
          PsiVariable var = (PsiVariable)statement.getDeclaredElements()[0];
          myMethodCall = (PsiMethodCallExpression)var.getInitializer();
          var.getModifierList().replace(myOutputVariable.getModifierList());
        }
      }
      else if (myHasReturnStatementOutput) {
        PsiStatement statement = myElementFactory.createStatementFromText("return x;", null);
        statement = (PsiStatement)addToMethodCallLocation(statement);
        myMethodCall = (PsiMethodCallExpression)((PsiReturnStatement)statement).getReturnValue().replace(myMethodCall);
      }
      else {
        PsiStatement statement = myElementFactory.createStatementFromText("x();", null);
        statement = (PsiStatement)addToMethodCallLocation(statement);
        myMethodCall = (PsiMethodCallExpression)((PsiExpressionStatement)statement).getExpression().replace(myMethodCall);
      }
      if (myHasReturnStatement && !myHasReturnStatementOutput && !hasNormalExit) {
        PsiStatement statement = myElementFactory.createStatementFromText("return;", null);
        addToMethodCallLocation(statement);
      }
      else if (!myGenerateConditionalExit && exitStatementCopy != null) {
        addToMethodCallLocation(exitStatementCopy);
      }

      declareNecessaryVariablesAfterCall(myFlowEnd, myOutputVariable);

      deleteExtracted();
    }
    else {
      if (myHasExpressionOutput) {
        PsiReturnStatement returnStatement = (PsiReturnStatement)myElementFactory.createStatementFromText("return x;", null);
        final PsiExpression returnValue = RefactoringUtil.convertInitializerToNormalExpression(myExpression, myForcedReturnType);
        returnStatement.getReturnValue().replace(returnValue);
        body.add(returnStatement);
      }
      else {
        PsiExpressionStatement statement = (PsiExpressionStatement)myElementFactory.createStatementFromText("x;", null);
        statement.getExpression().replace(myExpression);
        body.add(statement);
      }
      myMethodCall = (PsiMethodCallExpression)myExpression.replace(myMethodCall);
    }

    if (myAnchor instanceof PsiField) {
      ((PsiField)myAnchor).normalizeDeclaration();
    }

    adjustFinalParameters(newMethod);

    myExtractedMethod = (PsiMethod)myTargetClass.addAfter(newMethod, myAnchor);
    if (myNeedChangeContext) {
      ChangeContextUtil.decodeContextInfo(myExtractedMethod, myTargetClass, RefactoringUtil.createThisExpression(myManager, null));
    }

  }

  private boolean needExitStatement(final PsiStatement exitStatement) {
    if (exitStatement instanceof PsiContinueStatement) {
      //IDEADEV-11748
      PsiStatement statement = ((PsiContinueStatement)exitStatement).findContinuedStatement();
      if (statement == null) return true;
      if (statement instanceof PsiLoopStatement) statement = ((PsiLoopStatement)statement).getBody();
      int endOffset = myControlFlow.getEndOffset(statement);
      return endOffset > myFlowEnd;
    }
    return true;
  }

  private void adjustFinalParameters(final PsiMethod method) throws IncorrectOperationException {
    final IncorrectOperationException[] exc = new IncorrectOperationException[1];
    exc[0] = null;
    final PsiParameter[] parameters = method.getParameterList().getParameters();
    if (parameters.length > 0) {
      if (CodeStyleSettingsManager.getSettings(myProject).GENERATE_FINAL_PARAMETERS) {
        method.accept(new JavaRecursiveElementVisitor() {

          @Override public void visitReferenceExpression(PsiReferenceExpression expression) {
            final PsiElement resolved = expression.resolve();
            if (resolved != null) {
              final int index = ArrayUtil.find(parameters, resolved);
              if (index >= 0) {
                final PsiParameter param = parameters[index];
                if (param.hasModifierProperty(PsiModifier.FINAL) && PsiUtil.isAccessedForWriting(expression)) {
                  try {
                    param.getModifierList().setModifierProperty(PsiModifier.FINAL, false);
                  }
                  catch (IncorrectOperationException e) {
                    exc[0] = e;
                  }
                }
              }
            }
            super.visitReferenceExpression(expression);
          }
        });
      }
      else {
        method.accept(new JavaRecursiveElementVisitor() {
          @Override public void visitReferenceExpression(PsiReferenceExpression expression) {
            final PsiElement resolved = expression.resolve();
            final int index = ArrayUtil.find(parameters, resolved);
            if (index >= 0) {
              final PsiParameter param = parameters[index];
              if (!param.hasModifierProperty(PsiModifier.FINAL) && RefactoringUtil.isInsideAnonymous(expression, method)) {
                try {
                  param.getModifierList().setModifierProperty(PsiModifier.FINAL, true);
                }
                catch (IncorrectOperationException e) {
                  exc[0] = e;
                }
              }
            }
            super.visitReferenceExpression(expression);
          }
        });
      }
      if (exc[0] != null) {
        throw exc[0];
      }
    }
  }

  public List<Match> getDuplicates() {
    if (myIsChainedConstructor) {
      return filterChainedConstructorDuplicates(myDuplicates);
    }
    return myDuplicates;
  }

  private static List<Match> filterChainedConstructorDuplicates(final List<Match> duplicates) {
    List<Match> result = new ArrayList<Match>();
    for(Match duplicate: duplicates) {
      final PsiElement matchStart = duplicate.getMatchStart();
      final PsiMethod method = PsiTreeUtil.getParentOfType(matchStart, PsiMethod.class);
      if (method != null && method.isConstructor()) {
        final PsiCodeBlock body = method.getBody();
        if (body != null) {
          final PsiStatement[] psiStatements = body.getStatements();
          if (psiStatements.length > 0 && matchStart == psiStatements [0]) {
            result.add(duplicate);
          }
        }
      }
    }
    return result;
  }

  public PsiElement processMatch(Match match) throws IncorrectOperationException {
    match.changeSignature(myExtractedMethod);
    if (RefactoringUtil.isInStaticContext(match.getMatchStart(), myExtractedMethod.getContainingClass())) {
      myExtractedMethod.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
    }
    final PsiMethodCallExpression methodCallExpression = generateMethodCall(match.getInstanceExpression(), false);

    ArrayList<ParameterTablePanel.VariableData> datas = new ArrayList<ParameterTablePanel.VariableData>();
    for (final ParameterTablePanel.VariableData variableData : myVariableDatum) {
      if (variableData.passAsParameter) {
        datas.add(variableData);
      }
    }
    for (ParameterTablePanel.VariableData data : datas) {
      final List<PsiElement> parameterValue = match.getParameterValues(data.variable);
      for (PsiElement val : parameterValue) {
        methodCallExpression.getArgumentList().add(val);
      }
    }
    return match.replace(methodCallExpression, myOutputVariable);
  }

  private void deleteExtracted() throws IncorrectOperationException {
    if (myEnclosingBlockStatement == null) {
      myElements[0].getParent().deleteChildRange(myElements[0], myElements[myElements.length - 1]);
    }
    else {
      myEnclosingBlockStatement.delete();
    }
  }

  protected PsiElement addToMethodCallLocation(PsiElement statement) throws IncorrectOperationException {
    if (myEnclosingBlockStatement == null) {
      return myElements[0].getParent().addBefore(statement, myElements[0]);
    }
    else {
      return myEnclosingBlockStatement.getParent().addBefore(statement, myEnclosingBlockStatement);
    }
  }


  private void calculateFlowStartAndEnd() {
    myFlowStart = -1;
    int index = 0;
    while (index < myElements.length) {
      myFlowStart = myControlFlow.getStartOffset(myElements[index]);
      if (myFlowStart >= 0) break;
      index++;
    }
    if (myFlowStart < 0) {
      // no executable code
      myFlowStart = 0;
      myFlowEnd = 0;
    }
    else {
      index = myElements.length - 1;
      while (true) {
        myFlowEnd = myControlFlow.getEndOffset(myElements[index]);
        if (myFlowEnd >= 0) break;
        index--;
      }
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("start offset:" + myFlowStart);
      LOG.debug("end offset:" + myFlowEnd);
    }
  }

  private void renameInputVariables() throws IncorrectOperationException {
    for (ParameterTablePanel.VariableData data : myVariableDatum) {
      PsiVariable variable = data.variable;
      if (!data.name.equals(variable.getName())) {
        for (PsiElement element : myElements) {
          RefactoringUtil.renameVariableReferences(variable, data.name, new LocalSearchScope(element));
        }
      }
    }
  }

  public PsiClass getTargetClass() {
    return myTargetClass;
  }

  private PsiMethod generateEmptyMethod(PsiClassType[] exceptions, boolean isStatic) throws IncorrectOperationException {
    PsiMethod newMethod;
    if (myIsChainedConstructor) {
      newMethod = myElementFactory.createConstructor();
    }
    else {
      newMethod = myElementFactory.createMethod(myMethodName, myReturnType);
      newMethod.getModifierList().setModifierProperty(PsiModifier.STATIC, isStatic);
    }
    newMethod.getModifierList().setModifierProperty(myMethodVisibility, true);
    if (myTypeParameterList != null) {
      newMethod.getTypeParameterList().replace(myTypeParameterList);
    }
    PsiCodeBlock body = newMethod.getBody();
    LOG.assertTrue(body != null);

    boolean isFinal = CodeStyleSettingsManager.getSettings(myProject).GENERATE_FINAL_PARAMETERS;
    PsiParameterList list = newMethod.getParameterList();
    for (ParameterTablePanel.VariableData data : myVariableDatum) {
      if (data.passAsParameter) {
        PsiParameter parm = myElementFactory.createParameter(data.name, data.type);
        if (isFinal) {
          parm.getModifierList().setModifierProperty(PsiModifier.FINAL, true);
        }
        list.add(parm);
      }
      else {
        @NonNls StringBuilder buffer = new StringBuilder();
        if (isFinal) {
          buffer.append("final ");
        }
        buffer.append("int ");
        buffer.append(data.name);
        buffer.append("=;");
        String text = buffer.toString();

        PsiDeclarationStatement declaration = (PsiDeclarationStatement)myElementFactory.createStatementFromText(text, null);
        declaration = (PsiDeclarationStatement)myStyleManager.reformat(declaration);
        final PsiTypeElement typeElement = myElementFactory.createTypeElement(data.type);
        ((PsiVariable)declaration.getDeclaredElements()[0]).getTypeElement().replace(typeElement);
        body.add(declaration);
      }
    }

    PsiReferenceList throwsList = newMethod.getThrowsList();
    for (PsiClassType exception : exceptions) {
      throwsList.add(JavaPsiFacade.getInstance(myManager.getProject()).getElementFactory().createReferenceElementByType(exception));
    }

    return (PsiMethod)myStyleManager.reformat(newMethod);
  }

  protected PsiMethodCallExpression generateMethodCall(PsiExpression instanceQualifier, final boolean generateArgs) throws IncorrectOperationException {
    @NonNls StringBuilder buffer = new StringBuilder();

    final boolean skipInstanceQualifier;
    if (myIsChainedConstructor) {
      skipInstanceQualifier = true;
      buffer.append(PsiKeyword.THIS);
    }
    else {
      skipInstanceQualifier = instanceQualifier == null || instanceQualifier instanceof PsiThisExpression;
      if (skipInstanceQualifier) {
        if (myNeedChangeContext) {
          boolean needsThisQualifier = false;
          PsiElement parent = myCodeFragmentMember;
          while (!myTargetClass.equals(parent)) {
            if (parent instanceof PsiMethod) {
              String methodName = ((PsiMethod)parent).getName();
              if (methodName.equals(myMethodName)) {
                needsThisQualifier = true;
                break;
              }
            }
            parent = parent.getParent();
          }
          if (needsThisQualifier) {
            buffer.append(myTargetClass.getName());
            buffer.append(".this.");
          }
        }
      }
      else {
        buffer.append("qqq.");
      }

      buffer.append(myMethodName);
    }
    buffer.append("(");
    if (generateArgs) {
      int count = 0;
      for (ParameterTablePanel.VariableData data : myVariableDatum) {
        if (data.passAsParameter) {
          if (count > 0) {
            buffer.append(",");
          }
          buffer.append(data.variable.getName());
          count++;
        }
      }
    }
    buffer.append(")");
    String text = buffer.toString();

    PsiMethodCallExpression expr = (PsiMethodCallExpression)myElementFactory.createExpressionFromText(text, null);
    expr = (PsiMethodCallExpression)myStyleManager.reformat(expr);
    if (!skipInstanceQualifier) {
      PsiExpression qualifierExpression = expr.getMethodExpression().getQualifierExpression();
      LOG.assertTrue(qualifierExpression != null);
      qualifierExpression.replace(instanceQualifier);
    }
    return expr;
  }

  private void declareNecessaryVariablesInsideBody(int start, int end, PsiCodeBlock body) throws IncorrectOperationException {
    List<PsiVariable> usedVariables = ControlFlowUtil.getUsedVariables(myControlFlow, start, end);
    for (PsiVariable variable : usedVariables) {
      boolean toDeclare = !isDeclaredInside(variable) && !myInputVariables.contains(variable);
      if (toDeclare) {
        String name = variable.getName();
        PsiDeclarationStatement statement = myElementFactory.createVariableDeclarationStatement(name, variable.getType(), null);
        body.add(statement);
      }
    }
  }

  protected void declareNecessaryVariablesAfterCall(int end, PsiVariable outputVariable) throws IncorrectOperationException {
    List<PsiVariable> usedVariables = ControlFlowUtil.getUsedVariables(myControlFlow, end, myControlFlow.getSize());
    Collection<ControlFlowUtil.VariableInfo> reassigned = ControlFlowUtil.getInitializedTwice(myControlFlow, end, myControlFlow.getSize());
    for (PsiVariable variable : usedVariables) {
      boolean toDeclare = isDeclaredInside(variable) && !variable.equals(outputVariable);
      if (toDeclare) {
        String name = variable.getName();
        PsiDeclarationStatement statement = myElementFactory.createVariableDeclarationStatement(name, variable.getType(), null);
        if (reassigned.contains(new ControlFlowUtil.VariableInfo(variable, null))) {
          final PsiElement[] psiElements = statement.getDeclaredElements();
          assert psiElements.length > 0;
          PsiVariable var = (PsiVariable) psiElements [0];
          var.getModifierList().setModifierProperty(PsiModifier.FINAL, false);
        }
        addToMethodCallLocation(statement);
      }
    }
  }

  public PsiMethodCallExpression getMethodCall() {
    return myMethodCall;
  }

  public boolean isDeclaredInside(PsiVariable variable) {
    if (variable instanceof ImplicitVariable) return false;
    int startOffset = myElements[0].getTextRange().getStartOffset();
    int endOffset = myElements[myElements.length - 1].getTextRange().getEndOffset();
    PsiIdentifier nameIdentifier = variable.getNameIdentifier();
    if (nameIdentifier == null) return false;
    final TextRange range = nameIdentifier.getTextRange();
    if (range == null) return false;
    int offset = range.getStartOffset();
    return startOffset <= offset && offset <= endOffset;
  }

  private String getNewVariableName(PsiVariable variable) {
    for (ParameterTablePanel.VariableData data : myVariableDatum) {
      if (data.variable.equals(variable)) {
        return data.name;
      }
    }
    return variable.getName();
  }

  private void chooseTargetClass() {
    myNeedChangeContext = false;
    myTargetClass = myCodeFragmentMember instanceof PsiMember
                    ? ((PsiMember)myCodeFragmentMember).getContainingClass()
                    : (PsiClass)myCodeFragmentMember.getParent();
    if (myTargetClass instanceof PsiAnonymousClass) {
      PsiElement target = myTargetClass.getParent();
      PsiElement targetMember = myTargetClass;
      while (true) {
        if (target instanceof PsiFile) break;
        if (target instanceof PsiClass && !(target instanceof PsiAnonymousClass)) break;
        targetMember = target;
        target = target.getParent();
      }
      if (target instanceof PsiClass) {
        PsiClass newTargetClass = (PsiClass)target;
        List<PsiVariable> array = new ArrayList<PsiVariable>();
        boolean success = true;
        for (PsiElement element : myElements) {
          if (!ControlFlowUtil.collectOuterLocals(array, element, myCodeFragmentMember, targetMember)) {
            success = false;
            break;
          }
        }
        if (success) {
          myTargetClass = newTargetClass;
          myInputVariables = new ArrayList<PsiVariable>(ContainerUtil.concat(myInputVariables, array));
          myNeedChangeContext = true;
        }
      }
    }
  }

  private void chooseAnchor() {
    myAnchor = myCodeFragmentMember;
    while (!myAnchor.getParent().equals(myTargetClass)) {
      myAnchor = myAnchor.getParent();
    }
  }

  private void showMultipleExitPointsMessage() {
    if (myShowErrorDialogs) {
      HighlightManager highlightManager = HighlightManager.getInstance(myProject);
      PsiStatement[] exitStatementsArray = myExitStatements.toArray(new PsiStatement[myExitStatements.size()]);
      EditorColorsManager manager = EditorColorsManager.getInstance();
      TextAttributes attributes = manager.getGlobalScheme().getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);
      highlightManager.addOccurrenceHighlights(myEditor, exitStatementsArray, attributes, true, null);
      String message = RefactoringBundle
        .getCannotRefactorMessage(RefactoringBundle.message("there.are.multiple.exit.points.in.the.selected.code.fragment"));
      CommonRefactoringUtil.showErrorHint(myProject, myEditor, message, myRefactoringName, myHelpId);
      WindowManager.getInstance().getStatusBar(myProject).setInfo(RefactoringBundle.message("press.escape.to.remove.the.highlighting"));
    }
  }

  private void showMultipleOutputMessage(PsiType expressionType) {
    if (myShowErrorDialogs) {
      StringBuilder buffer = new StringBuilder();
      buffer.append(RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("there.are.multiple.output.values.for.the.selected.code.fragment")));
      buffer.append("\n");
      if (myHasExpressionOutput) {
        buffer.append("    ").append(RefactoringBundle.message("expression.result")).append(": ");
        buffer.append(PsiFormatUtil.formatType(expressionType, 0, PsiSubstitutor.EMPTY));
        buffer.append(",\n");
      }
      if (myGenerateConditionalExit) {
        buffer.append("    ").append(RefactoringBundle.message("boolean.method.result"));
        buffer.append(",\n");
      }
      for (int i = 0; i < myOutputVariables.length; i++) {
        PsiVariable var = myOutputVariables[i];
        buffer.append("    ");
        buffer.append(var.getName());
        buffer.append(" : ");
        buffer.append(PsiFormatUtil.formatType(var.getType(), 0, PsiSubstitutor.EMPTY));
        if (i < myOutputVariables.length - 1) {
          buffer.append(",\n");
        }
        else {
          buffer.append(".");
        }
      }
      buffer.append("\nWould you like to Extract Method Object?");

      String message = buffer.toString();

      if (ApplicationManager.getApplication().isUnitTestMode()) throw new RuntimeException(message);
      RefactoringMessageDialog dialog = new RefactoringMessageDialog(myRefactoringName, message, myHelpId, "OptionPane.errorIcon", true,
                                                                     myProject);
      dialog.show();
      if (dialog.isOK()) {
        new ExtractMethodObjectHandler().invoke(myProject, myEditor, myTargetClass.getContainingFile(), DataManager.getInstance().getDataContext());
      }
    }
  }

  public PsiMethod getExtractedMethod() {
    return myExtractedMethod;
  }

  public boolean hasDuplicates() {
    final List<Match> duplicates = getDuplicates();
    return duplicates != null && !duplicates.isEmpty();
  }

  public boolean hasDuplicates(Set<VirtualFile> files) {
    if (hasDuplicates()) return true;
    final PsiManager psiManager = PsiManager.getInstance(myProject);
    for (VirtualFile file : files) {
      if (!myDuplicatesFinder.findDuplicates(psiManager.findFile(file)).isEmpty()) return true;
    }
    return false;
  }

  @NotNull
  public String getConfirmDuplicatePrompt(Match match) {
    final boolean needToBeStatic = RefactoringUtil.isInStaticContext(match.getMatchStart(), myExtractedMethod.getContainingClass());
    final String changedSignature = match.getChangedSignature(myExtractedMethod, needToBeStatic, VisibilityUtil.getVisibilityStringToDisplay(myExtractedMethod));
    if (changedSignature != null) {
      return RefactoringBundle.message("replace.this.code.fragment.and.change.signature", changedSignature);
    }
    if (needToBeStatic && !myExtractedMethod.hasModifierProperty(PsiModifier.STATIC)) {
      return RefactoringBundle.message("replace.this.code.fragment.and.make.method.static");
    }
    return RefactoringBundle.message("replace.this.code.fragment");
  }
}
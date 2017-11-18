package com.jetbrains.python.refactoring.extractmethod;

import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.codeInsight.codeFragment.CodeFragment;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.find.FindManager;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.extractMethod.AbstractExtractMethodDialog;
import com.intellij.refactoring.extractMethod.AbstractVariableData;
import com.intellij.refactoring.extractMethod.ExtractMethodDecorator;
import com.intellij.refactoring.extractMethod.ExtractMethodValidator;
import com.intellij.refactoring.listeners.RefactoringElementListenerComposite;
import com.intellij.refactoring.rename.RenameUtil;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.ui.ReplacePromptDialog;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.hash.HashMap;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.codeInsight.codeFragment.PyCodeFragment;
import com.jetbrains.python.codeInsight.controlflow.ControlFlowCache;
import com.jetbrains.python.codeInsight.controlflow.ScopeOwner;
import com.jetbrains.python.codeInsight.dataflow.scope.Scope;
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyFunctionBuilder;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import com.jetbrains.python.refactoring.PyReplaceExpressionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author oleg
 */
public class PyExtractMethodUtil {
  public static final String NAME = "extract.method.name";

  private PyExtractMethodUtil() {
  }

  public static void extractFromStatements(@NotNull final Project project,
                                           @NotNull final Editor editor,
                                           @NotNull final PyCodeFragment fragment,
                                           @NotNull final PsiElement statement1,
                                           @NotNull final PsiElement statement2) {
    if (!fragment.getOutputVariables().isEmpty() && fragment.isReturnInstructionInside()) {
      CommonRefactoringUtil.showErrorHint(project, editor,
                                          PyBundle.message("refactoring.extract.method.error.cannot.perform.refactoring.with.local"),
                                          RefactoringBundle.message("error.title"), "refactoring.extractMethod");
      return;
    }

    final PyFunction function = PsiTreeUtil.getParentOfType(statement1, PyFunction.class);
    final PyUtil.MethodFlags flags = function == null ? null : PyUtil.MethodFlags.of(function);
    final boolean isClassMethod = flags != null && flags.isClassMethod();
    final boolean isStaticMethod = flags != null && flags.isStaticMethod();

    // collect statements
    final List<PsiElement> elementsRange = PyPsiUtils.collectElements(statement1, statement2);
    if (elementsRange.isEmpty()) {
      CommonRefactoringUtil.showErrorHint(project, editor,
                                          "Cannot perform refactoring from empty code fragment",
                                          RefactoringBundle.message("extract.method.title"), "refactoring.extractMethod");
      return;
    }

    final Pair<String, AbstractVariableData[]> data = getNameAndVariableData(project, fragment, statement1, isClassMethod, isStaticMethod);
    if (data.first == null || data.second == null) {
      return;
    }

    final String methodName = data.first;
    final AbstractVariableData[] variableData = data.second;

    final PyDuplicatesFinder finder = new PyDuplicatesFinder(statement1, statement2);

    if (fragment.getOutputVariables().isEmpty()) {
      CommandProcessor.getInstance().executeCommand(project, new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              // Generate method
              PyFunction generatedMethod = generateMethodFromElements(project, methodName, variableData, elementsRange, flags);
              generatedMethod = insertGeneratedMethod(statement1, generatedMethod);

              // Process parameters
              final PsiElement firstElement = elementsRange.get(0);
              final boolean isMethod = PyPsiUtils.isMethodContext(firstElement);
              processParameters(project, generatedMethod, variableData, isMethod, isClassMethod, isStaticMethod);
              processGlobalWrites(generatedMethod, fragment);
              processNonlocalWrites(generatedMethod, fragment);

              // Generating call element
              final StringBuilder builder = new StringBuilder();
              if (fragment.isReturnInstructionInside()) {
                builder.append("return ");
              }
              if (fragment.isYieldInside()) {
                builder.append("yield from ");
              }
              if (isMethod) {
                appendSelf(firstElement, builder, isStaticMethod);
              }
              builder.append(methodName);
              builder.append("(").append(createCallArgsString(variableData)).append(")");
              PsiElement callElement = PyElementGenerator.getInstance(project).createFromText(LanguageLevel.getDefault(), PyCallExpression.class, builder.toString());

              // Replace statements with call
              callElement = replaceElements(elementsRange, callElement);
              callElement = CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(callElement);
              processDuplicates(callElement, generatedMethod, finder, editor);

              // Set editor
              setSelectionAndCaret(editor, callElement);
            }
          });
        }
      }, "Extract method", null);
    }
    else {
      CommandProcessor.getInstance().executeCommand(project, new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              // Generate return modified variables statements
              final StringBuilder builder = new StringBuilder();
              for (String s : fragment.getOutputVariables()) {
                if (builder.length() != 0) {
                  builder.append(", ");
                }
                builder.append(s);
              }
              final List<PsiElement> newMethodElements = new ArrayList<PsiElement>(elementsRange);
              final PsiElement returnStatement =
                PyElementGenerator.getInstance(project).createFromText(LanguageLevel.getDefault(), PyElement.class, "return " + builder.toString());
              newMethodElements.add(returnStatement);

              // Generate method
              PyFunction generatedMethod = generateMethodFromElements(project, methodName, variableData, newMethodElements, flags);
              generatedMethod = (PyFunction) CodeStyleManager.getInstance(project).reformat(generatedMethod);
              generatedMethod = insertGeneratedMethod(statement1, generatedMethod);

              // Process parameters
              final boolean isMethod = PyPsiUtils.isMethodContext(elementsRange.get(0));
              processParameters(project, generatedMethod, variableData, isMethod, isClassMethod, isStaticMethod);
              processGlobalWrites(generatedMethod, fragment);
              processNonlocalWrites(generatedMethod, fragment);

              // Generate call element
              builder.append(" = ");
              if (fragment.isYieldInside()) {
                builder.append("yield from ");
              }
              if (isMethod){
                appendSelf(elementsRange.get(0), builder, isStaticMethod);
              }
              builder.append(methodName).append("(");
              builder.append(createCallArgsString(variableData)).append(")");
              PsiElement callElement = PyElementGenerator.getInstance(project).createFromText(LanguageLevel.getDefault(), PyElement.class, builder.toString());

              // replace statements with call
              callElement = replaceElements(elementsRange, callElement);
              callElement = CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(callElement);
              processDuplicates(callElement, generatedMethod, finder, editor);

              // Set editor
              setSelectionAndCaret(editor, callElement);
            }
          });
        }
      }, "Extract method", null);
    }
  }

  private static void processDuplicates(@NotNull final PsiElement callElement,
                                        @NotNull final PyFunction generatedMethod,
                                        @NotNull final PyDuplicatesFinder finder,
                                        @NotNull final Editor editor) {
    ScopeOwner owner = ScopeUtil.getScopeOwner(callElement);
    if (owner instanceof PsiFile) return;
    List<PsiElement> scope = new ArrayList<PsiElement>();
    if (owner instanceof PyFunction) {
      scope.add(owner);
      final PyClass containingClass = ((PyFunction)owner).getContainingClass();
      if (containingClass != null) {
        for (PyFunction function : containingClass.getMethods()) {
          if (!function.equals(owner) && !function.equals(generatedMethod))
            scope.add(function);
        }
      }
    }
    final List<Pair<PsiElement, PsiElement>> duplicates = finder.findDuplicates(scope, generatedMethod);

    if (duplicates.size() > 0) {
      final String message = RefactoringBundle.message("0.has.detected.1.code.fragments.in.this.file.that.can.be.replaced.with.a.call.to.extracted.method",
                                                       ApplicationNamesInfo.getInstance().getProductName(), duplicates.size());
      final boolean isUnittest = ApplicationManager.getApplication().isUnitTestMode();
      final int exitCode = !isUnittest ?  Messages.showYesNoDialog(callElement.getProject(), message,
                                                    RefactoringBundle.message("refactoring.extract.method.dialog.title"), Messages.getInformationIcon()) :
                                          DialogWrapper.OK_EXIT_CODE;
      if (exitCode == DialogWrapper.OK_EXIT_CODE) {
        boolean replaceAll = false;
        for (Pair<PsiElement, PsiElement> match : duplicates) {
          final List<PsiElement> elementsRange = PyPsiUtils.collectElements(match.getFirst(), match.getSecond());
          if (!replaceAll) {
            highlightInEditor(callElement.getProject(), match, editor);

            int promptResult = FindManager.PromptResult.ALL;
            if (!isUnittest) {
              ReplacePromptDialog promptDialog = new ReplacePromptDialog(false, RefactoringBundle.message("replace.fragment"), callElement.getProject());
              promptDialog.show();
              promptResult = promptDialog.getExitCode();
            }
            if (promptResult == FindManager.PromptResult.SKIP) continue;
            if (promptResult == FindManager.PromptResult.CANCEL) break;

            if (promptResult == FindManager.PromptResult.OK) {
              replaceElements(elementsRange, callElement);
            }
            else if (promptResult == FindManager.PromptResult.ALL) {
              replaceElements(elementsRange, callElement);
              replaceAll = true;
            }
          }
          else {
            replaceElements(elementsRange, callElement);
          }
        }
      }
    }
  }

  private static void highlightInEditor(@NotNull final Project project, @NotNull final Pair<PsiElement, PsiElement> pair,
                                        @NotNull final Editor editor) {
    final HighlightManager highlightManager = HighlightManager.getInstance(project);
    final EditorColorsManager colorsManager = EditorColorsManager.getInstance();
    final TextAttributes attributes = colorsManager.getGlobalScheme().getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);
    final int startOffset = pair.getFirst().getTextRange().getStartOffset();
    final int endOffset = pair.getSecond().getTextRange().getEndOffset();
    highlightManager.addRangeHighlight(editor, startOffset, endOffset, attributes, true, null);
    final LogicalPosition logicalPosition = editor.offsetToLogicalPosition(startOffset);
    editor.getScrollingModel().scrollTo(logicalPosition, ScrollType.MAKE_VISIBLE);
  }

  private static void processGlobalWrites(@NotNull final PyFunction function, @NotNull final PyCodeFragment fragment) {
    final Set<String> globalWrites = fragment.getGlobalWrites();
    final Set<String> newGlobalNames = new LinkedHashSet<String>();
    final Scope scope = ControlFlowCache.getScope(function);
    for (String name : globalWrites) {
      if (!scope.isGlobal(name)) {
        newGlobalNames.add(name);
      }
    }
    if (!newGlobalNames.isEmpty()) {
      final PyElementGenerator generator = PyElementGenerator.getInstance(function.getProject());
      final PyGlobalStatement globalStatement = generator.createFromText(LanguageLevel.forElement(function),
                                                                         PyGlobalStatement.class,
                                                                         "global " + StringUtil.join(newGlobalNames, ", "));
      final PyStatementList statementList = function.getStatementList();
      if (statementList != null) {
        statementList.addBefore(globalStatement, statementList.getFirstChild());
      }
    }
  }

  private static void processNonlocalWrites(@NotNull PyFunction function, @NotNull PyCodeFragment fragment) {
    final Set<String> nonlocalWrites = fragment.getNonlocalWrites();
    final Set<String> newNonlocalNames = new LinkedHashSet<String>();
    final Scope scope = ControlFlowCache.getScope(function);
    for (String name : nonlocalWrites) {
      if (!scope.isNonlocal(name)) {
        newNonlocalNames.add(name);
      }
    }
    if (!newNonlocalNames.isEmpty()) {
      final PyElementGenerator generator = PyElementGenerator.getInstance(function.getProject());
      final PyNonlocalStatement nonlocalStatement = generator.createFromText(LanguageLevel.forElement(function),
                                                                             PyNonlocalStatement.class,
                                                                             "nonlocal " + StringUtil.join(newNonlocalNames, ", "));
      final PyStatementList statementList = function.getStatementList();
      if (statementList != null) {
        statementList.addBefore(nonlocalStatement, statementList.getFirstChild());
      }
    }
  }


  private static void appendSelf(PsiElement firstElement, StringBuilder builder, boolean staticMethod) {
    if (staticMethod) {
      final PyClass containingClass = PsiTreeUtil.getParentOfType(firstElement, PyClass.class);
      assert containingClass != null;
      builder.append(containingClass.getName());
    }
    else {
      builder.append(PyUtil.getFirstParameterName(PsiTreeUtil.getParentOfType(firstElement, PyFunction.class)));
    }
    builder.append(".");
  }

  public static void extractFromExpression(@NotNull final Project project,
                                           final Editor editor,
                                           final PyCodeFragment fragment,
                                           final PsiElement expression) {
    if (!fragment.getOutputVariables().isEmpty()){
      CommonRefactoringUtil.showErrorHint(project, editor,
                                          "Cannot perform refactoring from expression with local variables modifications inside code fragment",
                                          RefactoringBundle.message("error.title"), "refactoring.extractMethod");
      return;
    }

    if (fragment.isReturnInstructionInside()){
      CommonRefactoringUtil.showErrorHint(project, editor,
                                          "Cannot extract method with return instructions inside code fragment",
                                          RefactoringBundle.message("error.title"), "refactoring.extractMethod");
      return;
    }

    PyFunction function = PsiTreeUtil.getParentOfType(expression, PyFunction.class);
    final PyUtil.MethodFlags flags = function == null ? null : PyUtil.MethodFlags.of(function);
    final boolean isClassMethod = flags != null && flags.isClassMethod();
    final boolean isStaticMethod = flags != null && flags.isClassMethod();

    final Pair<String, AbstractVariableData[]> data = getNameAndVariableData(project, fragment, expression, isClassMethod, isStaticMethod);
    if (data.first == null || data.second == null) {
      return;
    }

    final String methodName = data.first;
    final AbstractVariableData[] variableData = data.second;

    if (fragment.getOutputVariables().isEmpty()) {
      CommandProcessor.getInstance().executeCommand(project, new Runnable() {
        @Override
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
              // Generate method
              PyFunction generatedMethod = generateMethodFromExpression(project, methodName, variableData, expression, flags);
              generatedMethod = insertGeneratedMethod(expression, generatedMethod);

              // Process parameters
              final boolean isMethod = PyPsiUtils.isMethodContext(expression);
              processParameters(project, generatedMethod, variableData, isMethod, isClassMethod, isStaticMethod);

              // Generating call element
              final StringBuilder builder = new StringBuilder();
              if (fragment.isYieldInside()) {
                builder.append("yield from ");
              } else {
                builder.append("return ");
              }
              if (isMethod){
                appendSelf(expression, builder, isStaticMethod);
              }
              builder.append(methodName);
              builder.append("(").append(createCallArgsString(variableData)).append(")");
              final PyElementGenerator generator = PyElementGenerator.getInstance(project);
              final PyElement generated = generator.createFromText(LanguageLevel.getDefault(), PyElement.class, builder.toString());
              PsiElement callElement = null;
              if (generated instanceof PyReturnStatement) {
                callElement = ((PyReturnStatement)generated).getExpression();
              }
              else if (generated instanceof PyExpressionStatement) {
                callElement = ((PyExpressionStatement)generated).getExpression();
              }

              // replace statements with call
              if (callElement != null) {
                callElement = PyReplaceExpressionUtil.replaceExpression(expression, callElement);
              }

              // Set editor
              setSelectionAndCaret(editor, callElement);
            }
          });
        }
      }, "Extract method", null);
    }
  }

  private static void setSelectionAndCaret(Editor editor, PsiElement callElement) {
    editor.getSelectionModel().removeSelection();
    editor.getCaretModel().moveToOffset(callElement.getTextOffset());
  }

  private static PsiElement replaceElements(final List<PsiElement> elementsRange, PsiElement callElement) {
    callElement = elementsRange.get(0).replace(callElement);
    if (elementsRange.size() > 1) {
      callElement.getParent().deleteChildRange(elementsRange.get(1), elementsRange.get(elementsRange.size() - 1));
    }
    return callElement;
  }

  // Creates string for call
  private static String createCallArgsString(AbstractVariableData[] variableDatas) {
    final StringBuilder builder = new StringBuilder();
    for (AbstractVariableData data : variableDatas) {
      if (data.isPassAsParameter()) {
        if (builder.length() != 0) {
          builder.append(", ");
        }
        builder.append(data.getOriginalName());
      }
    }
    return builder.toString();
  }

  private static void processParameters(final Project project,
                                        final PyFunction generatedMethod,
                                        final AbstractVariableData[] variableData,
                                        final boolean isMethod,
                                        final boolean isClassMethod,
                                        final boolean isStaticMethod) {
    final Map<String, String> map = createMap(variableData);
    // Rename parameters
    for (PyParameter parameter : generatedMethod.getParameterList().getParameters()) {
      final String name = parameter.getName();
      final String newName = map.get(name);
      if (name != null && newName != null && !name.equals(newName)){
        Map<PsiElement, String> allRenames = new java.util.HashMap<PsiElement, String>();
        allRenames.put(parameter, newName);
        UsageInfo[] usages = RenameUtil.findUsages(parameter, newName, false, false, allRenames);
        try {
          RenameUtil.doRename(parameter, newName, usages, project, new RefactoringElementListenerComposite());
        }
        catch (IncorrectOperationException e) {
          RenameUtil.showErrorMessage(e, parameter, project);
          return;
        }
      }
    }
    // Change signature according to pass settings and
    PyFunctionBuilder builder = new PyFunctionBuilder("foo");
    if (isClassMethod) {
      builder.parameter("cls");
    }
    else if (isMethod && !isStaticMethod) {
      builder.parameter("self");
    }
    for (AbstractVariableData data : variableData) {
      if (data.isPassAsParameter()) {
        builder.parameter(data.getName());
      }
    }
    final PyParameterList pyParameterList = builder.buildFunction(project, LanguageLevel.getDefault()).getParameterList();
    generatedMethod.getParameterList().replace(pyParameterList);
  }

  private static Map<String, String> createMap(final AbstractVariableData[] variableData) {
    final Map<String, String> map = new HashMap<String, String>();
    for (AbstractVariableData data : variableData) {
      map.put(data.getOriginalName(), data.getName());
    }
    return map;
  }

  private static PyFunction insertGeneratedMethod(PsiElement anchor, final PyFunction generatedMethod) {
    final Pair<PsiElement, TextRange> data = anchor.getUserData(PyReplaceExpressionUtil.SELECTION_BREAKS_AST_NODE);
    if (data != null) {
      anchor = data.first;
    }
    final PsiNamedElement parent = PsiTreeUtil.getParentOfType(anchor, PyFile.class, PyClass.class, PyFunction.class);

    PsiElement result;
    if (parent instanceof PyFile || parent instanceof PyClass) {
      PsiElement target = parent instanceof PyClass ? ((PyClass)parent).getStatementList() : parent;
      final PsiElement anchorStatement = PyPsiUtils.getStatement(target, anchor);
      result = target.addBefore(generatedMethod, anchorStatement);
    }
    else {
      result = parent.getParent().addBefore(generatedMethod, parent);
    }
    // to ensure correct reformatting, mark the entire method as generated
    result.accept(new PsiRecursiveElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        super.visitElement(element);
        CodeEditUtil.setNodeGenerated(element.getNode(), true);
      }
    });
    return (PyFunction)result;
  }

  private static PyFunction generateMethodFromExpression(final Project project,
                                                         final String methodName,
                                                         final AbstractVariableData[] variableData,
                                                         final PsiElement expression,
                                                         @Nullable final PyUtil.MethodFlags flags) {
    final PyFunctionBuilder builder = new PyFunctionBuilder(methodName);
    addDecorators(builder, flags);
    addFakeParameters(builder, variableData);
    final String text;
    if (expression instanceof PyYieldExpression) {
      text = String.format("(%s)", expression.getText());
    }
    else {
      text = expression.getText();
    }
    builder.statement("return " + text);
    return builder.buildFunction(project, LanguageLevel.getDefault());
  }

  private static PyFunction generateMethodFromElements(final Project project,
                                                       final String methodName,
                                                       final AbstractVariableData[] variableData,
                                                       final List<PsiElement> elementsRange,
                                                       @Nullable final PyUtil.MethodFlags flags) {
    assert !elementsRange.isEmpty() : "Empty statements list was selected!";

    final PyFunctionBuilder builder = new PyFunctionBuilder(methodName);
    addDecorators(builder, flags);
    addFakeParameters(builder, variableData);
    final PyFunction method = builder.buildFunction(project, LanguageLevel.getDefault());
    final PyStatementList statementList = method.getStatementList();
    assert statementList != null;
    for (PsiElement element : elementsRange) {
      if (element instanceof PsiWhiteSpace) {
        continue;
      }
      statementList.add(element);
    }
    // remove last instruction
    final PsiElement child = statementList.getFirstChild();
    if (child != null) {
      child.delete();
    }
    PsiElement last = statementList;
    while (last != null) {
      last = last.getLastChild();
      if (last instanceof PsiWhiteSpace) {
        last.delete();
      }
    }
    return method;
  }

  private static void addDecorators(PyFunctionBuilder builder, PyUtil.MethodFlags flags) {
    if (flags != null) {
      if (flags.isClassMethod()) {
        builder.decorate(PyNames.CLASSMETHOD);
      }
      else if (flags.isStaticMethod()) {
        builder.decorate(PyNames.STATICMETHOD);
      }
    }
  }

  private static void addFakeParameters(PyFunctionBuilder builder, AbstractVariableData[] variableData) {
    for (AbstractVariableData data : variableData) {
      builder.parameter(data.getOriginalName());
    }
  }

  private static Pair<String, AbstractVariableData[]> getNameAndVariableData(final Project project,
                                                                             final CodeFragment fragment,
                                                                             final PsiElement element,
                                                                             final boolean isClassMethod,
                                                                             final boolean isStaticMethod) {
    final ExtractMethodValidator validator = new PyExtractMethodValidator(element, project);
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      String name = System.getProperty(NAME);
      if (name == null){
        name = "foo";
      }
      final String error = validator.check(name);
      if (error != null){
        if (ApplicationManager.getApplication().isUnitTestMode()) {
          throw new CommonRefactoringUtil.RefactoringErrorHintException(error);
        }
        final StringBuilder builder = new StringBuilder();
        builder.append(error).append(". ").append(RefactoringBundle.message("do.you.wish.to.continue"));
        if (Messages.showOkCancelDialog(builder.toString(), RefactoringBundle.message("warning.title"), Messages.getWarningIcon()) != 0){
          throw new CommonRefactoringUtil.RefactoringErrorHintException(error);
        }
      }
      final List<AbstractVariableData> data = new ArrayList<AbstractVariableData>();
      for (String in : fragment.getInputVariables()) {
        final AbstractVariableData d = new AbstractVariableData();
        d.name = in+"_new";
        d.originalName = in;
        d.passAsParameter = true;
        data.add(d);
      }
      return Pair.create(name, data.toArray(new AbstractVariableData[data.size()]));
    }

    final boolean isMethod = PyPsiUtils.isMethodContext(element);
    final ExtractMethodDecorator decorator = new ExtractMethodDecorator() {
      public String createMethodPreview(final String methodName, final AbstractVariableData[] variableDatas) {
        final StringBuilder builder = new StringBuilder();
        if (isClassMethod) {
          builder.append("cls");
        }
        else if (isMethod && !isStaticMethod) {
          builder.append("self");
        }
        for (AbstractVariableData variableData : variableDatas) {
          if (variableData.passAsParameter) {
            if (builder.length() != 0) {
              builder.append(", ");
            }
            builder.append(variableData.name);
          }
        }
        builder.insert(0, "(");
        builder.insert(0, methodName);
        builder.insert(0, "def ");
        builder.append(")");
        return builder.toString();
      }
    };

    final AbstractExtractMethodDialog dialog = new AbstractExtractMethodDialog(project, "method_name", fragment, validator, decorator) {
      @Override
      protected String getHelpId() {
        return "python.reference.extractMethod";
      }

      @NotNull
      @Override
      protected FileType getFileType() {
        return PythonFileType.INSTANCE;
      }
    };
    dialog.show();

    //return if don`t want to extract method
    if (!dialog.isOK()) {
      return Pair.create(null, null);
    }

    return Pair.create(dialog.getMethodName(), dialog.getVariableData());
  }

  private static class PyExtractMethodValidator implements ExtractMethodValidator {
    private final PsiElement myElement;
    private final Project myProject;
    private final Function<String, Boolean> myFunction;

    public PyExtractMethodValidator(final PsiElement element, final Project project) {
      myElement = element;
      myProject = project;
      final ScopeOwner parent = ScopeUtil.getScopeOwner(myElement);
      myFunction = new Function<String, Boolean>() {
        @Override
        public Boolean fun(String s) {
          ScopeOwner owner = parent;
          while (owner != null) {
            if (owner instanceof PyClass) {
              if (((PyClass)owner).findMethodByName(s, true) != null) {
                return false;
              }
            }
            final Scope scope = ControlFlowCache.getScope(owner);
            if (scope.containsDeclaration(s)) {
              return false;
            }
            owner = ScopeUtil.getScopeOwner(owner);
          }
          return true;
        }
      };
    }

    public String check(final String name) {
      if (myFunction != null && !myFunction.fun(name)){
        return PyBundle.message("refactoring.extract.method.error.name.clash");
      }
      return null;
    }

    public boolean isValidName(final String name) {
      final NamesValidator validator = LanguageNamesValidation.INSTANCE.forLanguage(PythonLanguage.getInstance());
      assert validator != null;
      return validator.isIdentifier(name, myProject);
    }
  }
}

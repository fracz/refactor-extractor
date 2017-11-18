package com.jetbrains.python.refactoring.extractmethod;

import com.intellij.codeInsight.codeFragment.CodeFragment;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.extractMethod.AbstractExtractMethodDialog;
import com.intellij.refactoring.extractMethod.AbstractVariableData;
import com.intellij.refactoring.extractMethod.ExtractMethodDecorator;
import com.intellij.refactoring.extractMethod.ExtractMethodValidator;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.hash.HashMap;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oleg
 */
public class PyExtractMethodUtil {

  public static final String NAME = "extract.method.name";

  public static void extractFromStatements(final Project project,
                                           final Editor editor,
                                           final CodeFragment fragment,
                                           final PsiElement statement1,
                                           final PsiElement statement2) {
    if (!fragment.getOutputVariables().isEmpty() && fragment.isReturnInstructonInside()) {
      CommonRefactoringUtil.showErrorHint(project, editor,
                                          "Cannot perform refactoring from expression with local variables modifications and return instructions inside code fragment",
                                          RefactoringBundle.message("error.title"), "refactoring.extractMethod");
      return;
    }

    final Pair<String, AbstractVariableData[]> data = getNameAndVariableData(project, fragment, statement1);
    if (data.first == null || data.second == null) {
      return;
    }

    // collect statements
    final List<PsiElement> elementsRange = PyPsiUtils.collectElements(statement1, statement2);
    final String methodName = data.first;
    final AbstractVariableData[] variableData = data.second;

    if (fragment.getOutputVariables().isEmpty()) {
      CommandProcessor.getInstance().executeCommand(project, new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              // Generate method
              PyFunction generatedMethod = generateMethodFromElements(project, methodName, variableData, elementsRange);
              generatedMethod = insertGeneratedMethod(statement1, generatedMethod);

              // Process parameters
              final boolean isMethod = PyPsiUtils.isMethodContext(elementsRange.get(0));
              processParameters(project, generatedMethod, variableData, isMethod);

              // Generating call element
              final StringBuilder builder = new StringBuilder();
              if (fragment.isReturnInstructonInside()) {
                builder.append("return ");
              }
              if (isMethod){
                builder.append("self.");
              }
              builder.append(methodName);
              builder.append("(").append(createCallArgsString(variableData)).append(")");
              PsiElement callElement = PythonLanguage.getInstance().getElementGenerator().createFromText(project, PyCallExpression.class, builder.toString());

              //# replace statements with call
              callElement = replaceElements(elementsRange, callElement);

              // # Set editor
              setSelectionAndCaret(editor, callElement);
            }
          });
        }
      }, "Extract method", null);
    } else {
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
                PythonLanguage.getInstance().getElementGenerator().createFromText(project, PyElement.class, "return " + builder.toString());
              newMethodElements.add(returnStatement);

              // Generate method
              PyFunction generatedMethod = generateMethodFromElements(project, methodName, variableData, newMethodElements);
              generatedMethod = (PyFunction) CodeStyleManager.getInstance(project).reformat(generatedMethod);
              generatedMethod = insertGeneratedMethod(statement1, generatedMethod);

              // Process parameters
              final boolean isMethod = PyPsiUtils.isMethodContext(elementsRange.get(0));
              processParameters(project, generatedMethod, variableData, isMethod);

              // Generate call element
              builder.append(" = ");
              if (isMethod){
                builder.append("self.");
              }
              builder.append(methodName).append("(");
              builder.append(createCallArgsString(variableData)).append(")");
              PsiElement callElement = PythonLanguage.getInstance().getElementGenerator().createFromText(project, PyElement.class, builder.toString());

              // replace statements with call
              callElement = replaceElements(elementsRange, callElement);

              // Set editor
              setSelectionAndCaret(editor, callElement);
            }
          });
        }
      }, "Extract method", null);
    }
  }

  public static void extractFromExpression(final Project project,
                                           final Editor editor,
                                           final CodeFragment fragment,
                                           final PsiElement expression) {
    if (!fragment.getOutputVariables().isEmpty()){
      CommonRefactoringUtil.showErrorHint(project, editor,
                                          "Cannot perform refactoring from expression with local variables modifications inside code fragment",
                                          RefactoringBundle.message("error.title"), "refactoring.extractMethod");
      return;
    }

    if (fragment.isReturnInstructonInside()){
      CommonRefactoringUtil.showErrorHint(project, editor,
                                          "Cannot extract method with return instructions inside code fragment",
                                          RefactoringBundle.message("error.title"), "refactoring.extractMethod");
      return;
    }
    final Pair<String, AbstractVariableData[]> data = getNameAndVariableData(project, fragment, expression);
    if (data.first == null || data.second == null) {
      return;
    }

    final String methodName = data.first;
    final AbstractVariableData[] variableData = data.second;

    if (fragment.getOutputVariables().isEmpty()) {
      CommandProcessor.getInstance().executeCommand(project, new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              // Generate method
              PyFunction generatedMethod = generateMethodFromExpression(project, methodName, variableData, expression);
              generatedMethod = insertGeneratedMethod(expression, generatedMethod);

              // Process parameters
              final boolean isMethod = PyPsiUtils.isMethodContext(expression);
              processParameters(project, generatedMethod, variableData, isMethod);

              // Generating call element
              final StringBuilder builder = new StringBuilder();
              if (fragment.isReturnInstructonInside()) {
                builder.append("return ");
              }
              if (isMethod){
                builder.append("self.");
              }
              builder.append(methodName);
              builder.append("(").append(createCallArgsString(variableData)).append(")");
              PsiElement callElement = PythonLanguage.getInstance().getElementGenerator().createFromText(project, PyElement.class, builder.toString());

              // replace statements with call
              callElement = PyPsiUtils.replaceExpression(project, expression, callElement);

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
                                        final boolean isMethod) {
    final Map<String, String> map = createMap(variableData);
    // Rename parameters
    for (PyParameter parameter : generatedMethod.getParameterList().getParameters()) {
      final String name = parameter.getName();
      final String newName = map.get(name);
      if (name != null && newName != null && !name.equals(newName)){
        RefactoringFactory.getInstance(project).createRename(parameter, newName).run();
      }
    }
    // Change signature according to pass settings and
    final StringBuilder builder = new StringBuilder();
    builder.append("def foo(");
    final String params = createMethodParamsString(variableData, false);
    if (isMethod){
      builder.append("self");
      if (params.length() != 0){
        builder.append(", ");
        builder.append(params);
      }
    } else {
      builder.append(params);
    }
    builder.append(")\n  pass");
    final PyParameterList pyParameterList =
      PythonLanguage.getInstance().getElementGenerator().createFromText(project, PyFunction.class, builder.toString()).getParameterList();
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
    final Pair<PsiElement, TextRange> data = anchor.getUserData(PyPsiUtils.SELECTION_BREAKS_AST_NODE);
    if (data != null){
      anchor = data.first;
    }
    final PsiNamedElement parent = PsiTreeUtil.getParentOfType(anchor, PyFile.class, PyClass.class, PyFunction.class);

    if (parent instanceof PyFile || parent instanceof PyClass) {
      final PsiElement statement = PyPsiUtils.getStatement(parent, anchor);
      return (PyFunction) parent.addBefore(generatedMethod, statement);
    }
    return (PyFunction) parent.getParent().addBefore(generatedMethod, parent);
  }

  //  Creates string for method parameters
  private static String createMethodParamsString(final AbstractVariableData[] variableDatas, final boolean fakeSignature) {
    final StringBuilder builder = new StringBuilder();
    for (AbstractVariableData data : variableDatas) {
      if (fakeSignature || data.isPassAsParameter()) {
        if (builder.length() != 0) {
          builder.append(", ");
        }
        builder.append(fakeSignature ? data.getOriginalName() : data.getName());
      }
    }
    return builder.toString();
  }

  private static String generateSignature(final String methodName, final AbstractVariableData[] variableData, final PsiElement expression) {
    final StringBuilder builder = new StringBuilder();
    builder.append("def ").append(methodName).append("(");
    builder.append(createMethodParamsString(variableData, true));
    builder.append("):\n");
    return builder.toString();
  }

  private static PyFunction generateMethodFromExpression(final Project project,
                                                         final String methodName,
                                                         final AbstractVariableData[] variableData,
                                                         final PsiElement expression) {
    final StringBuilder builder = new StringBuilder();
    builder.append(generateSignature(methodName, variableData, expression));
    builder.append("  return ").append(expression.getText());
    return PythonLanguage.getInstance().getElementGenerator().createFromText(project, PyFunction.class, builder.toString());
  }

  private static PyFunction generateMethodFromElements(final Project project,
                                                       final String methodName,
                                                       final AbstractVariableData[] variableData,
                                                       final List<PsiElement> elementsRange) {
    assert !elementsRange.isEmpty() : "Empty statements list was selected!";
    final StringBuilder builder = new StringBuilder();
    builder.append(generateSignature(methodName, variableData, elementsRange.get(0)));
    builder.append("  pass\n");
    final PyElementGenerator pyElementGenerator = PythonLanguage.getInstance().getElementGenerator();
    final PyFunction method =
      pyElementGenerator.createFromText(project, PyFunction.class, builder.toString());
    final PyStatementList statementList = method.getStatementList();
    for (PsiElement element : elementsRange) {
      if (element instanceof PsiWhiteSpace){
        continue;
      }
      statementList.add(element);
    }
    // remove last instruction
    statementList.getFirstChild().delete();
    return method;
  }

  private static Pair<String, AbstractVariableData[]> getNameAndVariableData(final Project project,
                                                                             final CodeFragment fragment,
                                                                             final PsiElement element) {
      final ExtractMethodValidator validator = new PyExtractMethodValidator(element, project);
    if (ApplicationManager.getApplication().isUnitTestMode()){
      String name = System.getProperty(NAME);
      if (name == null){
        name = "foo";
      }
      final String result = validator.check(name);
      if (result != null){
        throw new CommonRefactoringUtil.RefactoringErrorHintException(result);
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
        if (isMethod) {
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

    final AbstractExtractMethodDialog dialog = new AbstractExtractMethodDialog(project, "method_name", fragment, validator, decorator);
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
      final PsiNamedElement parent = PsiTreeUtil.getParentOfType(myElement, PyFile.class, PyClass.class);
      if (parent instanceof PyFile){
        final List<PyFunction> functions = ((PyFile)parent).getTopLevelFunctions();
        myFunction = new Function<String, Boolean>() {
          public Boolean fun(@NotNull final String s) {
            for (PyFunction function : functions) {
              if (s.equals(function.getName())){
                return false;
              }
            }
            return true;
          }
        };
      } else
      if (parent instanceof PyClass){
        myFunction = new Function<String, Boolean>() {
          public Boolean fun(@NotNull final String s) {
            return ((PyClass) parent).findMethodByName(s, true) == null;
          }
        };
      } else {
        myFunction = null;
      }
    }

    public String check(final String name) {
      if (myFunction != null && !myFunction.fun(name)){
        return PyBundle.message("refactoring.extract.method.error.name.clash");
      }
      return null;
    }

    public boolean isValidName(final String name) {
      return LanguageNamesValidation.INSTANCE.forLanguage(PythonLanguage.getInstance()).isIdentifier(name, myProject);
    }
  }
}

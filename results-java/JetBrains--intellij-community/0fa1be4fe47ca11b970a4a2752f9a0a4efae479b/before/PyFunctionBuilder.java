package com.jetbrains.python.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyElementGenerator;
import com.jetbrains.python.psi.PyFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author yole
 */
public class PyFunctionBuilder {
  private String myName;
  private List<String> myParameters = new ArrayList<String>();
  private List<String> myStatements = new ArrayList<String>();

  public PyFunctionBuilder(String name) {
    myName = name;
  }

  public PyFunctionBuilder parameter(String baseName) {
    String name = baseName;
    int uniqueIndex = 0;
    while(myParameters.contains(name)) {
      uniqueIndex++;
      name = baseName + uniqueIndex;
    }
    myParameters.add(name);
    return this;
  }

  public PyFunctionBuilder statement(String text) {
    myStatements.add(text);
    return this;
  }

  public PyFunction addFunction(PsiElement target) {
    return (PyFunction) target.add(buildFunction(target.getProject()));
  }

  public PyFunction buildFunction(Project project) {
    String text = buildText(project);
    PyElementGenerator generator = PythonLanguage.getInstance().getElementGenerator();
    return generator.createFromText(project, PyFunction.class, text);
  }

  private String buildText(Project project) {
    StringBuilder builder = new StringBuilder("def ");
    builder.append(myName).append("(");
    builder.append(StringUtil.join(myParameters, ", "));
    builder.append("):");
    List<String> statements = myStatements.isEmpty() ? Collections.singletonList("pass") : myStatements;
    final CodeStyleSettings codeStyleSettings = CodeStyleSettingsManager.getInstance(project).getCurrentSettings();
    int indentSize = codeStyleSettings.getIndentOptions(PythonFileType.INSTANCE).INDENT_SIZE;
    String indent = StringUtil.repeatSymbol(' ', indentSize);
    for (String statement : statements) {
      builder.append("\n").append(indent).append(statement);
    }
    return builder.toString();
  }
}
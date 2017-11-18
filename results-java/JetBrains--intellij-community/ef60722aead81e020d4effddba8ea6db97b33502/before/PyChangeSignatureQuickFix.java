/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.inspections.quickfix;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashSet;
import com.intellij.xml.util.XmlStringUtil;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.PyCallExpression.PyArgumentsMapping;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyUnionType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.jetbrains.python.refactoring.NameSuggesterUtil;
import com.jetbrains.python.refactoring.PyRefactoringUtil;
import com.jetbrains.python.refactoring.changeSignature.PyChangeSignatureDialog;
import com.jetbrains.python.refactoring.changeSignature.PyMethodDescriptor;
import com.jetbrains.python.refactoring.changeSignature.PyParameterInfo;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.jetbrains.python.psi.PyUtil.as;

public class PyChangeSignatureQuickFix extends LocalQuickFixOnPsiElement {

  public static final Key<Boolean> CHANGE_SIGNATURE_ORIGINAL_CALL = Key.create("CHANGE_SIGNATURE_ORIGINAL_CALL");

  @NotNull
  public static PyChangeSignatureQuickFix forMismatchedCall(@NotNull PyArgumentsMapping mapping) {
    assert mapping.getMarkedCallee() != null;
    final PyFunction function = as(mapping.getMarkedCallee().getCallable(), PyFunction.class);
    assert function != null;
    final PyCallExpression callExpression = mapping.getCallExpression();
    int positionalParamAnchor = -1;
    final PyParameter[] parameters = function.getParameterList().getParameters();
    for (PyParameter parameter : parameters) {
      final PyNamedParameter namedParam = parameter.getAsNamed();
      final boolean isVararg = namedParam != null && (namedParam.isPositionalContainer() || namedParam.isKeywordContainer());
      if (parameter instanceof PySingleStarParameter || parameter.hasDefaultValue() || isVararg) {
        break;
      }
      positionalParamAnchor++;
    }
    final List<Pair<Integer, PyParameterInfo>> newParameters = new ArrayList<>();
    final TypeEvalContext context = TypeEvalContext.userInitiated(function.getProject(), callExpression.getContainingFile());
    final Set<String> usedParamNames = new HashSet<>();
    for (PyExpression arg : mapping.getUnmappedArguments()) {
      if (arg instanceof PyKeywordArgument) {
        final String defaultValueText;
        final PyExpression value = ((PyKeywordArgument)arg).getValueExpression();
        if (value != null && PyRefactoringUtil.isSimpleExpression(value) && !value.textContains('\n')) {
          defaultValueText = value.getText();
        }
        else {
          defaultValueText = ApplicationManager.getApplication().isUnitTestMode() ? "None" : "";
        }
        newParameters.add(Pair.create(parameters.length - 1,
                                      new PyParameterInfo(-1, ((PyKeywordArgument)arg).getKeyword(), defaultValueText, true)));
      }
      else {
        final String paramName = generateParameterName(arg, function, usedParamNames, context);
        newParameters.add(Pair.create(positionalParamAnchor, new PyParameterInfo(-1, paramName, "", false)));
        usedParamNames.add(paramName);
      }
    }
    return new PyChangeSignatureQuickFix(function, newParameters, mapping.getCallExpression());
  }

  @NotNull
  public static PyChangeSignatureQuickFix forMismatchingMethods(@NotNull PyFunction function, @NotNull PyFunction complementary) {
    final int paramLength = function.getParameterList().getParameters().length;
    final int complementaryParamLength = complementary.getParameterList().getParameters().length;
    final List<Pair<Integer, PyParameterInfo>> extraParams;
    if (complementaryParamLength > paramLength) {
      extraParams = Collections.singletonList(Pair.create(paramLength - 1, new PyParameterInfo(-1, "**kwargs", "", false)));
    }
    else {
      extraParams = Collections.emptyList();
    }
    return new PyChangeSignatureQuickFix(function, extraParams, null);
  }


  private final List<Pair<Integer, PyParameterInfo>> myExtraParameters;
  private final SmartPsiElementPointer<PyCallExpression> myOriginalCallExpression;


  public PyChangeSignatureQuickFix(@NotNull PyFunction function,
                                   @NotNull List<Pair<Integer, PyParameterInfo>> extraParameters,
                                   @Nullable PyCallExpression expression) {
    super(function);
    myExtraParameters = ContainerUtil.sorted(extraParameters, Comparator.comparingInt(p -> p.getFirst()));
    if (expression != null) {
      myOriginalCallExpression = SmartPointerManager.getInstance(function.getProject()).createSmartPsiElementPointer(expression);
    }
    else {
      myOriginalCallExpression = null;
    }
  }

  @NotNull
  public String getFamilyName() {
    return PyBundle.message("QFIX.NAME.change.signature");
  }

  @NotNull
  @Override
  public String getText() {
    final PyFunction function = getFunction();
    if (function == null) {
      return getFamilyName();
    }
    final String params = StringUtil.join(createMethodDescriptor(function).getParameters(), info -> {
      return info.getOldIndex() == -1 ? "<b>" + info.getName() + "</b>" : info.getName();
    }, ", ");

    final String message = PyBundle.message("QFIX.change.signature.of", StringUtil.notNullize(function.getName()) + "(" + params + ")");
    return XmlStringUtil.wrapInHtml(message);
  }

  @Nullable
  private PyFunction getFunction() {
    return (PyFunction)getStartElement();
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull PsiFile file, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
    final PyFunction function = getFunction();
    final PyMethodDescriptor descriptor = createMethodDescriptor(function);

    final PyChangeSignatureDialog dialog = new PyChangeSignatureDialog(project, descriptor) {
      // Similar to JavaChangeSignatureDialog.createAndPreselectNew()
      @Override
      protected int getSelectedIdx() {
        return (int)StreamEx.of(getParameters()).indexOf(info -> info.getOldIndex() < 0).orElse(super.getSelectedIdx());
      }
    };

    final PyCallExpression originalCall = myOriginalCallExpression.getElement();
    try {
      if (originalCall != null) {
        originalCall.putUserData(CHANGE_SIGNATURE_ORIGINAL_CALL, true);
      }
      if (ApplicationManager.getApplication().isUnitTestMode()) {
        dialog.createRefactoringProcessor().run();
      }
      else {
        dialog.show();
      }
    }
    finally {
      if (originalCall != null) {
        originalCall.putUserData(CHANGE_SIGNATURE_ORIGINAL_CALL, null);
      }
    }
  }

  @NotNull
  private static String generateParameterName(@NotNull PyExpression argumentValue,
                                              @NotNull PyFunction function,
                                              @NotNull Set<String> usedParameterNames,
                                              @NotNull TypeEvalContext context) {
    PyType type = context.getType(argumentValue);
    if (type instanceof PyUnionType) {
      type = ContainerUtil.find(((PyUnionType)type).getMembers(), Conditions.instanceOf(PyClassType.class));
    }
    final String typeName = type != null && type.getName() != null ? type.getName() : "object";

    final Collection<String> suggestions = NameSuggesterUtil.generateNamesByType(typeName);
    final String shortestName = ContainerUtil.getFirstItem(suggestions);
    assert shortestName != null;

    String result = shortestName;
    int counter = 1;
    while (!PyRefactoringUtil.isValidNewName(result, function.getStatementList()) || usedParameterNames.contains(result)) {
      result = shortestName + counter;
      counter++;
    }
    return result;
  }

  @NotNull
  private PyMethodDescriptor createMethodDescriptor(final PyFunction function) {
    return new PyMethodDescriptor(function) {
      @Override
      public List<PyParameterInfo> getParameters() {
        final List<PyParameterInfo> result = new ArrayList<>();
        final List<PyParameterInfo> originalParams = super.getParameters();
        final PeekingIterator<Pair<Integer, PyParameterInfo>> extra = Iterators.peekingIterator(myExtraParameters.iterator());
        while (extra.hasNext() && extra.peek().getFirst() < 0) {
          result.add(extra.next().getSecond());
        }
        for (int i = 0; i < originalParams.size(); i++) {
          result.add(originalParams.get(i));
          while (extra.hasNext() && extra.peek().getFirst() == i) {
            result.add(extra.next().getSecond());
          }
        }
        return result;
      }
    };
  }

  @Nullable
  @Override
  public PsiElement getElementToMakeWritable(@NotNull PsiFile currentFile) {
    return getFunction();
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.refactors.preconditionschecknotnull;

import static com.google.errorprone.BugPattern.Category.GUAVA;
import static com.google.errorprone.BugPattern.MaturityLevel.ON_BY_DEFAULT;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.kindIs;
import static com.google.errorprone.matchers.Matchers.methodSelect;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.sun.source.tree.Tree.Kind.STRING_LITERAL;
import static java.lang.String.format;

import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.RefactoringVisitorState;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.refactors.RefactoringMatcher;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

import java.util.List;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
@BugPattern(
    name = "Preconditions checkNotNull",
    category = GUAVA,
    severity = ERROR,
    maturity = ON_BY_DEFAULT,
    summary = "Literal argument to Preconditions.checkNotNull()",
    explanation =
        "Preconditions.checkNotNull() takes two arguments. The first is the reference " +
        "that should be non-null. The second is the error message to print (usually a string " +
        "literal). Often the order of the two arguments is swapped, and the reference is " +
        "never actually checked for nullity. This check ensures that the first argument to " +
        "Preconditions.checkNotNull() is not a literal.")
public class PreconditionsCheckNotNull extends RefactoringMatcher<MethodInvocationTree> {

  @SuppressWarnings({"unchecked"})
  @Override
  public boolean matches(MethodInvocationTree methodInvocationTree, VisitorState state) {
    return allOf(
        methodSelect(staticMethod("com.google.common.base.Preconditions", "checkNotNull")),
        argument(0, kindIs(STRING_LITERAL, ExpressionTree.class)))
        .matches(methodInvocationTree, state);
  }

  @Override
  public Refactor refactor(MethodInvocationTree methodInvocationTree, RefactoringVisitorState state) {
    List<? extends ExpressionTree> arguments = methodInvocationTree.getArguments();
    ExpressionTree stringLiteralValue = arguments.get(0);
    SuggestedFix fix = new SuggestedFix();
    if (arguments.size() == 2) {
      fix.swap(arguments.get(0), arguments.get(1));
    } else {
      fix.delete(state.getPath().getParentPath().getLeaf());
    }
    return new Refactor(stringLiteralValue,
        format("String literal %s passed as first argument to Preconditions#checkNotNull",
            stringLiteralValue), fix);
  }
}
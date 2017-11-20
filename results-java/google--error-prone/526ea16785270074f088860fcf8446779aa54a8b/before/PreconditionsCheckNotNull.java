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

package com.google.errorprone.refactors;

import com.google.errorprone.RefactoringVisitorState;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

import java.util.List;

import static com.google.errorprone.matchers.Matchers.*;
import static com.sun.source.tree.Tree.Kind.STRING_LITERAL;
import static java.lang.String.format;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
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
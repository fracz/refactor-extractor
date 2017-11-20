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

package com.google.errorprone.checkers;

import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.expressionMethodSelect;
import static com.google.errorprone.matchers.Matchers.kindIs;
import static com.google.errorprone.matchers.Matchers.methodSelect;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static java.lang.String.format;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Error checker for calls to the Preconditions class in Guava which use
 * 'expensive' methods of producing the error string. In most cases, users are
 * better off using the equivalent methods which defer the computation of the
 * string until the test actually fails.
 *
 * @author sjnickerson@google.com (Simon Nickerson)
 */
public class PreconditionsExpensiveStringChecker
    extends ErrorChecker<MethodInvocationTree> {

  @Override
  @SuppressWarnings({"vararg", "unchecked"})
  public Matcher<MethodInvocationTree> matcher() {
    return allOf(
        anyOf(
            methodSelect(staticMethod(
                "com.google.common.base.Preconditions", "checkNotNull")),
            methodSelect(staticMethod(
                "com.google.common.base.Preconditions", "checkState")),
            methodSelect(staticMethod(
                "com.google.common.base.Preconditions", "checkArgument"))),
        argument(1, Matchers.<ExpressionTree>allOf(
            kindIs(Kind.METHOD_INVOCATION, ExpressionTree.class),
            expressionMethodSelect(staticMethod("java.lang.String", "format")),
            new StringFormatCallContainsNoSpecialFormattingMatcher(
                Pattern.compile("%[^%s]"))
        ))
    );
  }

  @Override
  public AstError produceError(MethodInvocationTree methodInvocationTree,
      VisitorState state) {
    MemberSelectTree method =
        (MemberSelectTree) methodInvocationTree.getMethodSelect();

    List<? extends ExpressionTree> arguments =
        methodInvocationTree.getArguments();
    MethodInvocationTree stringFormat = (MethodInvocationTree) arguments.get(1);

    Position position = getPosition(stringFormat);

    // TODO(sjnickerson): Figure out how to get a suggested fix. Basically we
    // remove the String.format() wrapper, but I don't know how to express
    // this. This current one is not correct!
    SuggestedFix fix = null;

    return new AstError(arguments.get(1),
        format("Second argument to Preconditions.%s is a call to " +
            "String.format() which can be unwrapped",
            method.getIdentifier().toString()), fix);
  }

  private static class StringFormatCallContainsNoSpecialFormattingMatcher
      implements Matcher<ExpressionTree> {

    private final Pattern invalidFormatCharacters;

    StringFormatCallContainsNoSpecialFormattingMatcher(
        Pattern invalidFormatCharacters) {
      this.invalidFormatCharacters = invalidFormatCharacters;
    }

    @Override
    public boolean matches(ExpressionTree t, VisitorState state) {
      if (!(t instanceof MethodInvocationTree)) {
        return false;
      }
      MethodInvocationTree stringFormatInvocation = (MethodInvocationTree) t;
      if (!(stringFormatInvocation.getArguments().get(0)
          instanceof LiteralTree)) {
        return false;
      }
      LiteralTree firstArg = (LiteralTree)
          stringFormatInvocation.getArguments().get(0);
      String literal = firstArg.getValue().toString();
      return !invalidFormatCharacters.matcher(literal).find();
    }
  }
}
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
import static com.google.errorprone.matchers.Matchers.hasElementWithValue;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.google.errorprone.matchers.Matchers.stringLiteral;

import com.google.errorprone.ErrorCollectingTreeScanner;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Matcher;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author eaftan@google.com (Eddie Aftandilian)
 * @author pepstein@google.com (Peter Epstein)
 */
public class FallThroughSuppressionChecker extends ErrorChecker<AnnotationTree> {

  @Override
  @SuppressWarnings({"varargs", "unchecked"})
  public Matcher<AnnotationTree> matcher() {
    return allOf(isType("java.lang.SuppressWarnings"),
                 hasElementWithValue("value", stringLiteral("fallthrough")));
  }

  @Override
  public AstError produceError(AnnotationTree annotationTree, VisitorState state) {
    return new AstError(
        annotationTree,
        "this has no effect if fallthrough warning is suppressed",
        getSuggestedFix(annotationTree, state));
  }

  private SuggestedFix getSuggestedFix(AnnotationTree annotationTree, VisitorState state) {
    ListBuffer<JCTree.JCExpression> arguments = new ListBuffer<JCTree.JCExpression>();
    for (ExpressionTree argumentTree : annotationTree.getArguments()) {
      AssignmentTree assignmentTree = (AssignmentTree) argumentTree;
      if (assignmentTree.getVariable().toString().equals("value")) {
        ExpressionTree expressionTree = assignmentTree.getExpression();
        switch (expressionTree.getKind()) {
          case STRING_LITERAL:
              // Fallthrough was the only thing suppressed, so remove line.
            return SuggestedFix.delete(getPosition(annotationTree));
          case NEW_ARRAY:
            JCTree.JCNewArray newArray =
                initializersWithoutFallthrough(state, (NewArrayTree) expressionTree);
            if (newArray.getInitializers().size() >= 2) {
              arguments.add(newArray);
            } else if (newArray.getInitializers().size() == 1) {
              // Only one left, so get rid of the curly braces.
              arguments.add(
                  singleInitializer((JCTree.JCLiteral) newArray.getInitializers().get(0), state));
            } else {
              // Fallthrough was the only thing suppressed, so remove line.
              return SuggestedFix.delete(getPosition(annotationTree));
            }
            break;
        }
      } else {
        // SuppressWarnings only has a value element, but if they ever add more,
        // we want to keep them.
        arguments.add((JCTree.JCExpression) argumentTree);
      }
    }
    JCTree.JCAnnotation replacement = state.getTreeMaker()
        .Annotation((JCTree) annotationTree.getAnnotationType(), arguments.toList());
    return SuggestedFix
        .replace(getPosition(annotationTree), replacement.toString());
  }

  private JCTree.JCLiteral singleInitializer(JCTree.JCLiteral literalExpression,
      VisitorState state) {
    return state.getTreeMaker().Literal(literalExpression.getValue());
  }

  @SuppressWarnings("unchecked")
  private JCTree.JCNewArray initializersWithoutFallthrough(VisitorState state,
      NewArrayTree expressionTree) {
    ListBuffer<JCTree.JCExpression> replacementInitializers = new ListBuffer<JCTree.JCExpression>();
    ListBuffer<JCTree.JCExpression> dimensions = new ListBuffer<JCTree.JCExpression>();
    dimensions.addAll((Collection<? extends JCTree.JCExpression>) expressionTree.getDimensions());
    for (ExpressionTree elementTree : expressionTree.getInitializers()) {
      if (!stringLiteral("fallthrough").matches(elementTree, state)) {
        replacementInitializers.add((JCTree.JCExpression) elementTree);
      }
    }
    return state.getTreeMaker().NewArray((JCTree.JCExpression) expressionTree.getType(),
        dimensions.toList(), replacementInitializers.toList());
  }

  public static class Scanner extends ErrorCollectingTreeScanner {
    public ErrorChecker<AnnotationTree> annotationChecker = new FallThroughSuppressionChecker();

    @Override
    public List<AstError> visitAnnotation(AnnotationTree annotationTree, VisitorState visitorState) {
      List<AstError> result = new ArrayList<AstError>();
      AstError error = annotationChecker
          .check(annotationTree, visitorState.withPath(getCurrentPath()));
      if (error != null) {
        result.add(error);
      }

      super.visitAnnotation(annotationTree, visitorState);
      return result;
    }
  }
}
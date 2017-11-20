/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.MaturityLevel.MATURE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.methodSelect;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.sun.source.tree.Tree.Kind.*;
import static javax.lang.model.element.Modifier.STATIC;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.EditDistance;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;

import java.util.Set;


/**
 * TODO(eaftan): Consider cases where the parent is not a statement or there is no parent?
 *
 * @author eaftan@google.com (Eddie Aftandilian)
 * @author scottjohnson@google.com (Scott Johnson)
 */
@BugPattern(name = "SelfAssignment",
    summary = "Variable assigned to itself",
    explanation = "The left-hand side and right-hand side of this assignment are the same. " +
        "It has no effect.\n\n" +
        "This also handles assignments in which the right-hand side is a call to " +
        "Preconditions.checkNotNull(), which returns the variable that was checked for " +
        "non-nullity.  If you just intended to check that the variable is non-null, please " +
        "don't assign the result to the checked variable; just call Preconditions.checkNotNull() " +
        "as a bare statement.",
    category = JDK, severity = ERROR, maturity = MATURE)
public class SelfAssignmentChecker extends BugChecker
    implements BugChecker.VariableTreeMatcher, BugChecker.AssignmentTreeMatcher {

  @Override
  public Description matchAssignment(AssignmentTree tree, VisitorState state) {
    ExpressionTree expression = stripCheckNotNull(tree.getExpression(), state);
    if (ASTHelpers.sameVariable(tree.getVariable(), expression)) {
      ;
    }
    return null;
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    ExpressionTree initializer = stripCheckNotNull(tree.getInitializer(), state);
    Tree parent = state.getPath().getParentPath().getLeaf();

    // must be a static class variable with member select initializer
    if (initializer == null || initializer.getKind() != MEMBER_SELECT || parent.getKind() != CLASS
        || !tree.getModifiers().getFlags().contains(STATIC)) {
      return null;
    }

    MemberSelectTree rhs = (MemberSelectTree) initializer;
    Symbol rhsClass = ASTHelpers.getSymbol(rhs.getExpression());
    Symbol lhsClass = ASTHelpers.getSymbol(parent);
    // TODO(eaftan): WTF?
    if (rhsClass.equals(lhsClass) && rhs.getIdentifier().contentEquals(tree.getName())) {
      // construct description
    }
    return null;
  }

  /**
   * Matcher for assignments, e.g. foo = bar.
   */
  private class AssignmentTreeMatcher implements BugChecker.AssignmentTreeMatcher {
    @Override
    public Description matchAssignment(AssignmentTree tree, VisitorState state) {
      ExpressionTree expression = stripCheckNotNull(tree.getExpression(), state);
      if (ASTHelpers.sameVariable(tree.getVariable(), expression)) {
        return describe(tree, state);
      }
      return Description.NO_MATCH;
    }

    /**
     * We expect that the lhs is a field and the rhs is an identifier, specifically
     * a parameter to the method.  We base our suggested fixes on this expectation.
     *
     * Case 1: If lhs is a field and rhs is an identifier, find a method parameter
     * of the same type and similar name and suggest it as the rhs.  (Guess that they
     * have misspelled the identifier.)
     *
     * Case 2: If lhs is a field and rhs is not an identifier, find a method parameter
     * of the same type and similar name and suggest it as the rhs.
     *
     * Case 3: If lhs is not a field and rhs is an identifier, find a class field
     * of the same type and similar name and suggest it as the lhs.
     *
     * Case 4: Otherwise suggest deleting the assignment.
     */
    public Description describe(AssignmentTree tree, VisitorState state) {

      // the statement that is the parent of the self-assignment expression
      Tree parent = state.getPath().getParentPath().getLeaf();

      // default fix is to delete assignment
      SuggestedFix fix = new SuggestedFix().delete(parent);

      ExpressionTree lhs = tree.getVariable();
      ExpressionTree rhs = tree.getExpression();

      // if this is a method invocation, they must be calling checkNotNull()
      if (tree.getExpression().getKind() == METHOD_INVOCATION) {
        // change the default fix to be "checkNotNull(x)" instead of "x = checkNotNull(x)"
        fix = new SuggestedFix().replace(tree, rhs.toString());
        // new rhs is first argument to checkNotNull()
        rhs = stripCheckNotNull(rhs, state);
      }


      if (lhs.getKind() == MEMBER_SELECT) {
        // find a method parameter of the same type and similar name and suggest it
        // as the rhs

        // rhs should be either identifier or field access
        assert(rhs.getKind() == IDENTIFIER || rhs.getKind() == MEMBER_SELECT);

        // get current name of rhs
        String rhsName = null;
        if (rhs.getKind() == IDENTIFIER) {
          rhsName = ((JCIdent) rhs).name.toString();
        } else if (rhs.getKind() == MEMBER_SELECT) {
          rhsName = ((JCFieldAccess) rhs).name.toString();
        }

        // find method parameters of the same type
        Type type = ((JCFieldAccess) lhs).type;
        TreePath path = state.getPath();
        while (path != null && path.getLeaf().getKind() != METHOD) {
          path = path.getParentPath();
        }
        JCMethodDecl method = (JCMethodDecl) path.getLeaf();
        int minEditDistance = Integer.MAX_VALUE;
        String replacement = null;
        for (JCVariableDecl var : method.params) {
          if (var.type == type) {
            int editDistance = EditDistance.getEditDistance(rhsName, var.name.toString());
            if (editDistance < minEditDistance) {
              // pick one with minimum edit distance
              minEditDistance = editDistance;
              replacement = var.name.toString();
            }
          }
        }
        if (replacement != null) {
          // suggest replacing rhs with the parameter
          fix = new SuggestedFix().replace(rhs, replacement);
        }
      } else if (rhs.getKind() == IDENTIFIER) {
        // find a field of the same type and similar name and suggest it as the lhs

        // lhs should be identifier
        assert(lhs.getKind() == IDENTIFIER);

        // get current name of lhs
        String lhsName = ((JCIdent) rhs).name.toString();

        // find class instance fields of the same type
        Type type = ((JCIdent) lhs).type;
        TreePath path = state.getPath();
        while (path != null && path.getLeaf().getKind() != CLASS) {
          path = path.getParentPath();
        }
        JCClassDecl klass = (JCClassDecl) path.getLeaf();
        int minEditDistance = Integer.MAX_VALUE;
        String replacement = null;
        for (JCTree member : klass.getMembers()) {
          if (member.getKind() == VARIABLE) {
            JCVariableDecl var = (JCVariableDecl) member;
            if (!Flags.isStatic(var.sym) && var.type == type) {
              int editDistance = EditDistance.getEditDistance(lhsName, var.name.toString());
              if (editDistance < minEditDistance) {
                // pick one with minimum edit distance
                minEditDistance = editDistance;
                replacement = var.name.toString();
              }
            }
          }
        }
        if (replacement != null) {
          // suggest replacing lhs with the field
          fix = new SuggestedFix().replace(lhs, "this." + replacement);
        }
      }

      return describeMatch(tree, fix);
    }

    @Override
    public Set<String> getAllNames() {
      return SelfAssignmentChecker.this.getAllNames();
    }
  }

  /**
   * Matcher for variable declaration and initialization in the same line, e.g.
   * public static final Object obj = Foo.obj;
   */
  private class VariableTreeMatcher implements BugChecker.VariableTreeMatcher {

    @Override
    public Description matchVariable(VariableTree tree, VisitorState state) {
      ExpressionTree initializer = stripCheckNotNull(tree.getInitializer(), state);
      Tree parent = state.getPath().getParentPath().getLeaf();

      // must be a static class variable with member select initializer
      if (initializer == null || initializer.getKind() != MEMBER_SELECT || parent.getKind() != CLASS
          || !tree.getModifiers().getFlags().contains(STATIC)) {
        return Description.NO_MATCH;
      }

      MemberSelectTree rhs = (MemberSelectTree) initializer;
      Symbol rhsClass = ASTHelpers.getSymbol(rhs.getExpression());
      Symbol lhsClass = ASTHelpers.getSymbol(parent);
      // TODO(eaftan): WTF?
      if (!rhsClass.equals(lhsClass) || !rhs.getIdentifier().contentEquals(tree.getName())) {
        return Description.NO_MATCH;
      }

      // for now, don't know how to fix - just delete.
      // TODO(eaftan): The correct fix is probably to just delete the initializer but still
      // declare the variable.
      return describeMatch(tree, new SuggestedFix().delete(parent));
    }

    @Override
    public Set<String> getAllNames() {
      return SelfAssignmentChecker.this.getAllNames();
    }
  }

  /**
   * If the given expression is a call to checkNotNull(x), returns x.
   * Otherwise, returns the given expression.
   *
   * TODO(eaftan): Also match calls to Java 7's Objects.requireNonNull() method.
   */
  private static ExpressionTree stripCheckNotNull(ExpressionTree expression, VisitorState state) {
    if (expression != null && expression instanceof MethodInvocationTree) {
      if (methodSelect(staticMethod("com.google.common.base.Preconditions", "checkNotNull"))
          .matches((MethodInvocationTree) expression, state)) {
        return ((MethodInvocationTree) expression).getArguments().get(0);
      }
    }
    return expression;
  }
}
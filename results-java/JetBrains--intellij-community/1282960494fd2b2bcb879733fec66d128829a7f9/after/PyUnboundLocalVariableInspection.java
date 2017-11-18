package com.jetbrains.python.inspections;

import com.intellij.codeInsight.controlflow.ControlFlowUtil;
import com.intellij.codeInsight.controlflow.Instruction;
import com.intellij.codeInsight.dataflow.DFALimitExceededException;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.HashSet;
import com.jetbrains.cython.CythonLanguageDialect;
import com.jetbrains.mako.MakoLanguage;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.actions.AddGlobalQuickFix;
import com.jetbrains.python.codeInsight.controlflow.ControlFlowCache;
import com.jetbrains.python.codeInsight.controlflow.PyControlFlowBuilder;
import com.jetbrains.python.codeInsight.controlflow.ReadWriteInstruction;
import com.jetbrains.python.codeInsight.controlflow.ScopeOwner;
import com.jetbrains.python.codeInsight.dataflow.scope.Scope;
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil;
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeVariable;
import com.jetbrains.python.console.PydevConsoleRunner;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author oleg
 */
public class PyUnboundLocalVariableInspection extends PyInspection {
  private static Key<Set<ScopeOwner>> LARGE_FUNCTIONS_KEY = Key.create("PyUnboundLocalVariableInspection.LargeFunctions");

  @NotNull
  @Nls
  public String getDisplayName() {
    return PyBundle.message("INSP.NAME.unbound");
  }

  @NotNull
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull final LocalInspectionToolSession session) {
    session.putUserData(LARGE_FUNCTIONS_KEY, new HashSet<ScopeOwner>());
    return new PyInspectionVisitor(holder){
      @Override
      public void visitPyReferenceExpression(final PyReferenceExpression node) {
        if (CythonLanguageDialect._isDisabledFor(node) || MakoLanguage._isDisabledFor(node)) {
          return;
        }
        final Set<ScopeOwner> largeFunctions = session.getUserData(LARGE_FUNCTIONS_KEY);
        assert largeFunctions != null;

        if (node.getContainingFile() instanceof PyExpressionCodeFragment || PydevConsoleRunner.isInPydevConsole(node)){
          return;
        }
        // Ignore global statements arguments
        if (PyGlobalStatementNavigator.getByArgument(node) != null){
          return;
        }
        // Ignore qualifier inspections
        if (node.getQualifier() != null) {
          return;
        }
        // Ignore import subelements
        if (PsiTreeUtil.getParentOfType(node, PyImportStatementBase.class) != null) {
          return;
        }
        final String name = node.getReferencedName();
        if (name == null) {
          return;
        }
        final ScopeOwner owner = ScopeUtil.getDeclarationScopeOwner(node, name);
        if (owner == null || largeFunctions.contains(owner)) {
          return;
        }
        // Ignore references declared in outer scopes
        if (owner != PsiTreeUtil.getParentOfType(node, ScopeOwner.class)) {
          return;
        }
        final Scope scope = ControlFlowCache.getScope(owner);
        // Ignore globals and if scope even doesn't contain such a declaration
        if (scope.isGlobal(name) || (!scope.containsDeclaration(name))){
          return;
        }
        // Start DFA from the assignment statement in case of augmented assignments
        final PsiElement anchor;
        final PyAugAssignmentStatement augAssignment = PsiTreeUtil.getParentOfType(node, PyAugAssignmentStatement.class);
        if (augAssignment != null && name.equals(augAssignment.getTarget().getName())) {
          anchor = augAssignment;
        }
        else {
          anchor = node;
        }
        ScopeVariable variable;
        try {
          variable = scope.getDeclaredVariable(anchor, name);
        }
        catch (DFALimitExceededException e) {
          largeFunctions.add(owner);
          registerLargeFunction(owner);
          return;
        }
        if (variable == null) {
          boolean resolves2LocalVariable = false;
          boolean resolve2Scope = true;
          boolean isBuiltin = false;
          for (ResolveResult result : node.getReference().multiResolve(true)) {
            final PsiElement element = result.getElement();
            if (element == null) {
              continue;
            }
            if (PyBuiltinCache.getInstance(node).hasInBuiltins(element)) {
              isBuiltin = true;
              continue;
            }
            if (PyAssignmentStatementNavigator.getStatementByTarget(element) != null ||
                PyForStatementNavigator.getPyForStatementByIterable(element) != null ||
                PyExceptPartNavigator.getPyExceptPartByTarget(element) != null ||
                PyListCompExpressionNavigator.getPyListCompExpressionByVariable(element) != null ||
                PyImportStatementNavigator.getImportStatementByElement(element) != null) {
              resolves2LocalVariable = true;
              resolve2Scope = PsiTreeUtil.isAncestor(owner, element, false);
              break;
            }
          }

          // TODO: This mechanism for detecting unbound variables would be enough for the whole inspection if the CFG builder was unaware of 'self'
          if (!resolves2LocalVariable) {
            if (PyControlFlowBuilder.isSelf(node)) {
              return;
            }
            if (owner instanceof PyClass) {
              if (ScopeUtil.getDeclarationScopeOwner(owner, name) != null || isBuiltin) {
                return;
              }
            }
            if (owner instanceof PyFile) {
              if (isBuiltin) {
                return;
              }
              registerProblem(node, PyBundle.message("INSP.unbound.name.not.defined", name));
            }
            else {
              registerUnboundLocal(node);
            }
            return;
          }

          final Ref<Boolean> readAccessSeen = new Ref<Boolean>(false);
          final Instruction[] instructions = ControlFlowCache.getControlFlow(owner).getInstructions();
          final int number = ControlFlowUtil.findInstructionNumberByElement(instructions, node);
          // Do not perform this check if we cannot find corresponding instruction
          if (number != -1) {
            ControlFlowUtil.iteratePrev(number, instructions, new Function<Instruction, ControlFlowUtil.Operation>() {
              public ControlFlowUtil.Operation fun(final Instruction inst) {
                try {
                  if (inst.num() == number) {
                    return ControlFlowUtil.Operation.NEXT;
                  }
                  if (inst instanceof ReadWriteInstruction) {
                    final ReadWriteInstruction rwInst = (ReadWriteInstruction)inst;
                    if (name.equals(rwInst.getName())) {
                      final PsiElement e = inst.getElement();
                      if (e != null && scope.getDeclaredVariable(e, name) != null) {
                        return ControlFlowUtil.Operation.BREAK;
                      }
                      if (rwInst.getAccess().isWriteAccess()) {
                        return ControlFlowUtil.Operation.CONTINUE;
                      }
                      else {
                        readAccessSeen.set(true);
                        return ControlFlowUtil.Operation.BREAK;
                      }
                    }
                  }
                  return ControlFlowUtil.Operation.NEXT;
                }
                catch (DFALimitExceededException e) {
                  largeFunctions.add(owner);
                  registerLargeFunction(owner);
                  return ControlFlowUtil.Operation.BREAK;
                }
              }
            });
          }
          // In this case we've already inspected prev read access and shouldn't warn about this one
          if (readAccessSeen.get()){
            return;
          }
          if (resolve2Scope) {
            if (owner instanceof PyFile){
              registerProblem(node, PyBundle.message("INSP.unbound.name.not.defined", name));
            }
            else {
              registerUnboundLocal(node);
            }
          }
          else if (owner instanceof PyFunction && PsiTreeUtil.getParentOfType(owner, PyClass.class, PyFile.class) instanceof PyFile){
            registerUnboundLocal(node);
          }
        }
      }

      @Override
      public void visitPyNonlocalStatement(final PyNonlocalStatement node) {
        if (CythonLanguageDialect._isDisabledFor(node)) {
          return;
        }
        for (PyTargetExpression var : node.getVariables()) {
          final String name = var.getName();
          final ScopeOwner owner = ScopeUtil.getDeclarationScopeOwner(var, name);
          if (owner == null || owner instanceof PyFile) {
            registerProblem(var, PyBundle.message("INSP.unbound.nonlocal.variable", name),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING, null);
          }
        }
      }

      private void registerUnboundLocal(PyReferenceExpression node) {
        registerProblem(node, PyBundle.message("INSP.unbound.local.variable", node.getName()),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        null,
                        new AddGlobalQuickFix());

      }

      private void registerLargeFunction(ScopeOwner owner) {
        registerProblem((owner instanceof PyFunction) ? ((PyFunction)owner).getNameIdentifier() : owner,
                        PyBundle.message("INSP.unbound.function.too.large", owner.getName()),
                        ProblemHighlightType.WEAK_WARNING,
                        null);
      }
    };
  }
}
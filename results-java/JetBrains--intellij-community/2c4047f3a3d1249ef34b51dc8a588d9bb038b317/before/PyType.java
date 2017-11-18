package com.jetbrains.python.psi.types;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.AccessDirection;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Represents a type of an expression.
 *
 * @author yole
 */
public interface PyType {

  /**
   * Resolves an attribute of type.
   *
   *
   * @param name      attribute name
   * @param location  the expression of type qualifierType on which the member is being resolved (optional)
    *@param direction
   * @param resolveContext   @return null if name definitely cannot be found (e.g. in a qualified reference),
   *         or an empty list if name is not found but other contexts are worth looking at,
   *         or a list of elements that define the name, a la multiResolve().
   */
  @Nullable
  List<? extends PsiElement> resolveMember(final String name, @Nullable PyExpression location, AccessDirection direction,
                                           PyResolveContext resolveContext);

  /**
   * Proposes completion variants from type's attributes.
   *
   * @param referenceExpression which is to be completed
   * @param context             to share state between nested invocations
   * @return completion variants good for {@link com.intellij.psi.PsiReference#getVariants} return value.
   */
  Object[] getCompletionVariants(String completionPrefix, PyExpression location, ProcessingContext context);

  /**
   * Context key for access to a set of names already found by variant search.
   */
  Key<Set<String>> CTX_NAMES = new Key<Set<String>>("Completion variants names");

  /**
   * @return name of the type
   */
  @Nullable
  String getName();

  /**
   * @return true if the type is a known built-in type.
   * @param context
   */
  boolean isBuiltin(TypeEvalContext context);
}
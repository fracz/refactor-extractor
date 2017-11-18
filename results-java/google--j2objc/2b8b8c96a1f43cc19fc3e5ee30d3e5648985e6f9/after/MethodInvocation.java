/*
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

package com.google.devtools.j2objc.ast;

import com.google.common.base.Preconditions;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.IMethodBinding;

import java.util.List;

/**
 * Method invocation node type.
 */
public class MethodInvocation extends Expression {

  private IMethodBinding methodBinding = null;
  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public MethodInvocation(org.eclipse.jdt.core.dom.MethodInvocation jdtNode) {
    super(jdtNode);
    methodBinding = Types.getMethodBinding(jdtNode);
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    for (Object argument : jdtNode.arguments()) {
      arguments.add((Expression) TreeConverter.convert(argument));
    }
  }

  public MethodInvocation(MethodInvocation other) {
    super(other);
    methodBinding = other.getMethodBinding();
    expression.copyFrom(other.getExpression());
    name.copyFrom(other.getName());
    arguments.copyFrom(other.getArguments());
  }

  public MethodInvocation(IMethodBinding binding, Expression expression) {
    super(binding.getReturnType());
    methodBinding = binding;
    this.expression.set(expression);
    name.set(new SimpleName(binding));
  }

  @Override
  public Kind getKind() {
    return Kind.METHOD_INVOCATION;
  }

  // TODO(kstanger): This should eventually be a PrefixExpression node.
  public static MethodInvocation newAddressOf(Expression expression) {
    MethodInvocation node = new MethodInvocation(
        IOSMethodBinding.newAddressOf(expression.getTypeBinding()), null);
    node.getArguments().add(expression);
    return node;
  }

  // TODO(kstanger): This should eventually be a PrefixExpression node.
  public static MethodInvocation newDereference(Expression expression) {
    MethodInvocation node = new MethodInvocation(
        IOSMethodBinding.newDereference(expression.getTypeBinding()), null);
    node.getArguments().add(expression);
    return node;
  }

  public IMethodBinding getMethodBinding() {
    return methodBinding;
  }

  public void setMethodBinding(IMethodBinding newMethodBinding) {
    methodBinding = newMethodBinding;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public void setExpression(Expression newExpression) {
    expression.set(newExpression);
  }

  public SimpleName getName() {
    return name.get();
  }

  public void setName(SimpleName newName) {
    name.set(newName);
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
      name.accept(visitor);
      arguments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public MethodInvocation copy() {
    return new MethodInvocation(this);
  }

  @Override
  public void validateInner() {
    super.validateInner();
    Preconditions.checkNotNull(methodBinding);
    Preconditions.checkNotNull(name.get());
  }
}
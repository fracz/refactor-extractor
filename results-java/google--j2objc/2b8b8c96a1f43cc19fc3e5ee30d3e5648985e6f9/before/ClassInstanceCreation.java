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

import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.IMethodBinding;

import java.util.List;

/**
 * Node type for constructing a new instance. (e.g. "new Foo()")
 */
public class ClassInstanceCreation extends Expression {

  private IMethodBinding methodBinding = null;
  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<Type> type = ChildLink.create(Type.class, this);
  private ChildList<Expression> arguments = ChildList.create(Expression.class, this);
  private ChildLink<AnonymousClassDeclaration> anonymousClassDeclaration =
      ChildLink.create(AnonymousClassDeclaration.class, this);

  public ClassInstanceCreation(org.eclipse.jdt.core.dom.ClassInstanceCreation jdtNode) {
    super(jdtNode);
    methodBinding = Types.getMethodBinding(jdtNode);
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    type.set((Type) TreeConverter.convert(jdtNode.getType()));
    for (Object argument : jdtNode.arguments()) {
      arguments.add((Expression) TreeConverter.convert(argument));
    }
    anonymousClassDeclaration.set(
        (AnonymousClassDeclaration) TreeConverter.convert(jdtNode.getAnonymousClassDeclaration()));
  }

  public ClassInstanceCreation(ClassInstanceCreation other) {
    super(other);
    methodBinding = other.getMethodBinding();
    expression.copyFrom(other.getExpression());
    type.copyFrom(other.getType());
    arguments.copyFrom(other.getArguments());
    anonymousClassDeclaration.copyFrom(other.getAnonymousClassDeclaration());
  }

  @Override
  public Kind getKind() {
    return Kind.CLASS_INSTANCE_CREATION;
  }

  public IMethodBinding getMethodBinding() {
    return methodBinding;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public Type getType() {
    return type.get();
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  public AnonymousClassDeclaration getAnonymousClassDeclaration() {
    return anonymousClassDeclaration.get();
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
      type.accept(visitor);
      arguments.accept(visitor);
      anonymousClassDeclaration.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public ClassInstanceCreation copy() {
    return new ClassInstanceCreation(this);
  }
}
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

package com.google.devtools.j2objc.translate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSArrayTypeBinding;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import java.util.List;
import java.util.Map;

/**
 * Rewrites array creation into a method invocation on an IOSArray class.
 * Must be run after JavaToIOSMethodTranslator because the varargs conversion
 * needs to know if the method is mapped.
 *
 * @author Keith Stanger
 */
public class ArrayRewriter extends ErrorReportingASTVisitor {

  private Map<IOSArrayTypeBinding, IOSMethodBinding> initMethods = Maps.newHashMap();
  private Map<IOSArrayTypeBinding, IOSMethodBinding> singleDimMethods = Maps.newHashMap();
  private Map<IOSArrayTypeBinding, IOSMethodBinding> multiDimMethods = Maps.newHashMap();

  private static final ImmutableMap<String, String> INIT_METHODS =
      ImmutableMap.<String, String>builder()
      .put("IOSBooleanArray", " arrayWithBooleans:(BOOL *)booleans count:(int)count")
      .put("IOSByteArray", " arrayWithBytes:(char *)bytes count:(int)count")
      .put("IOSCharArray", " arrayWithCharacters:(unichar *)chars count:(int)count")
      .put("IOSDoubleArray", " arrayWithDoubles:(double *)doubles count:(int)count")
      .put("IOSFloatArray", " arrayWithFloats:(float *)floats count:(int)count")
      .put("IOSIntArray", " arrayWithInts:(int *)ints count:(int)count")
      .put("IOSLongArray", " arrayWithLongs:(long long *)longs count:(int)count")
      .put("IOSShortArray", " arrayWithShorts:(shorts *)shorts count:(int)count")
      .put("IOSObjectArray",
           " arrayWithObjects:(id *)objects count:(int)count type:(IOSClass *)type")
      .build();

  public static final ImmutableMap<String, IOSMethod> ACCESS_METHODS =
      ImmutableMap.<String, IOSMethod>builder()
      .put("IOSBooleanArray", IOSMethod.create("IOSBooleanArray booleanAtIndex:(NSUInteger)index"))
      .put("IOSByteArray", IOSMethod.create("IOSByteArray byteAtIndex:(NSUInteger)index"))
      .put("IOSCharArray", IOSMethod.create("IOSCharArray charAtIndex:(NSUInteger)index"))
      .put("IOSDoubleArray", IOSMethod.create("IOSDoubleArray doubleAtIndex:(NSUInteger)index"))
      .put("IOSFloatArray", IOSMethod.create("IOSFloatArray floatAtIndex:(NSUInteger)index"))
      .put("IOSIntArray", IOSMethod.create("IOSIntArray intAtIndex:(NSUInteger)index"))
      .put("IOSLongArray", IOSMethod.create("IOSLongArray longAtIndex:(NSUInteger)index"))
      .put("IOSShortArray", IOSMethod.create("IOSShortArray shortAtIndex:(NSUInteger)index"))
      .put("IOSObjectArray", IOSMethod.create("IOSObjectArray objectAtIndex:(NSUInteger)index"))
      .build();

  public static final ImmutableMap<String, IOSMethod> ACCESS_REF_METHODS =
      ImmutableMap.<String, IOSMethod>builder()
      .put("IOSBooleanArray",
           IOSMethod.create("IOSBooleanArray *booleanRefAtIndex:(NSUInteger)index"))
      .put("IOSByteArray", IOSMethod.create("IOSByteArray *byteRefAtIndex:(NSUInteger)index"))
      .put("IOSCharArray", IOSMethod.create("IOSCharArray *charRefAtIndex:(NSUInteger)index"))
      .put("IOSDoubleArray", IOSMethod.create("IOSDoubleArray *doubleRefAtIndex:(NSUInteger)index"))
      .put("IOSFloatArray", IOSMethod.create("IOSFloatArray *floatRefAtIndex:(NSUInteger)index"))
      .put("IOSIntArray", IOSMethod.create("IOSIntArray *intRefAtIndex:(NSUInteger)index"))
      .put("IOSLongArray", IOSMethod.create("IOSLongArray *longRefAtIndex:(NSUInteger)index"))
      .put("IOSShortArray", IOSMethod.create("IOSShortArray *shortRefAtIndex:(NSUInteger)index"))
      .build();

  public static final IOSMethod OBJECT_ARRAY_ASSIGNMENT = IOSMethod.create(
      "IOSObjectArray replaceObjectAtIndex:(NSUInteger)index withObject:(id)object");

  @Override
  public void endVisit(ArrayCreation node) {
    ASTUtil.setProperty(node, createInvocation(node));
  }

  private MethodInvocation createInvocation(ArrayCreation node) {
    AST ast = node.getAST();
    ITypeBinding arrayType = Types.getTypeBinding(node);
    assert arrayType.isArray();
    ArrayInitializer initializer = node.getInitializer();
    if (initializer != null) {
      return newInitializedArrayInvocation(ast, arrayType, ASTUtil.getExpressions(initializer));
    } else {
      List<Expression> dimensions = ASTUtil.getDimensions(node);
      if (dimensions.size() == 1) {
        return newSingleDimensionArrayInvocation(ast, arrayType, dimensions.get(0));
      } else {
        return newMultiDimensionArrayInvocation(ast, arrayType, dimensions);
      }
    }
  }

  @Override
  public void endVisit(ArrayInitializer node) {
    ASTNode parent = node.getParent();
    if (!(parent instanceof ArrayCreation)) {
      ASTUtil.setProperty(node, newInitializedArrayInvocation(
          node.getAST(), Types.getTypeBinding(node), ASTUtil.getExpressions(node)));
    }
  }

  private void rewriteVarargs(IMethodBinding method, List<Expression> args, AST ast) {
    method = method.getMethodDeclaration();
    if (!method.isVarargs() || IOSMethodBinding.hasVarArgsTarget(method)) {
      return;
    }
    ITypeBinding[] paramTypes = method.getParameterTypes();
    ITypeBinding lastParam = paramTypes[paramTypes.length - 1];
    assert lastParam.isArray();
    int varargsSize = args.size() - paramTypes.length + 1;
    if (varargsSize == 1) {
      ITypeBinding lastArgType = Types.getTypeBinding(args.get(args.size() - 1));
      if (lastParam.getDimensions() == lastArgType.getDimensions()) {
        // Last argument is already an array.
        return;
      }
    }

    List<Expression> varargs = args.subList(paramTypes.length - 1, args.size());
    MethodInvocation newArg;
    if (varargs.isEmpty()) {
      newArg = newSingleDimensionArrayInvocation(ast, lastParam,
          ASTFactory.makeLiteral(ast, Integer.valueOf(0), Types.resolveJavaType("int")));
    } else {
      newArg = newInitializedArrayInvocation(ast, lastParam, varargs);
    }

    varargs.clear();
    args.add(newArg);
  }

  private MethodInvocation newInitializedArrayInvocation(
      AST ast, ITypeBinding arrayType, List<Expression> elements) {
    ITypeBinding componentType = arrayType.getComponentType();
    IOSArrayTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getInitializeMethod(iosArrayBinding);
    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, methodBinding, ASTFactory.newSimpleName(ast, iosArrayBinding));

    // Create the array initializer and add it as the first parameter.
    ArrayInitializer arrayInit = ast.newArrayInitializer();
    Types.addBinding(arrayInit, arrayType);
    for (Expression element : elements) {
      ASTUtil.getExpressions(arrayInit).add(NodeCopier.copySubtree(ast, element));
    }
    ASTUtil.getArguments(invocation).add(arrayInit);

    // Add the array size parameter.
    ASTUtil.getArguments(invocation).add(ASTFactory.makeLiteral(
        ast, Integer.valueOf(arrayInit.expressions().size()), Types.resolveJavaType("int")));

    // Add the type argument for object arrays.
    if (!componentType.isPrimitive()) {
      ASTUtil.getArguments(invocation).add(ASTFactory.newTypeLiteral(ast, componentType));
    }

    return invocation;
  }

  private IOSMethodBinding getInitializeMethod(IOSArrayTypeBinding arrayType) {
    IOSMethodBinding binding = initMethods.get(arrayType);
    if (binding != null) {
      return binding;
    }
    String methodName = INIT_METHODS.get(arrayType.getName());
    assert methodName != null;
    IOSMethod iosMethod = IOSMethod.create(arrayType.getName() + methodName);
    binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC | Modifier.STATIC, arrayType, arrayType);
    binding.addParameter(new GeneratedVariableBinding(arrayType, false, true, null, binding));
    binding.addParameter(new GeneratedVariableBinding(
        Types.resolveJavaType("int"), false, true, null, binding));
    if (arrayType.getName().equals("IOSObjectArray")) {
      binding.addParameter(new GeneratedVariableBinding(
          Types.getIOSClass(), false, true, null, binding));
    }
    initMethods.put(arrayType, binding);
    return binding;
  }

  private MethodInvocation newSingleDimensionArrayInvocation(
      AST ast, ITypeBinding arrayType, Expression dimensionExpr) {
    ITypeBinding componentType = arrayType.getComponentType();
    IOSArrayTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getSingleDimensionMethod(iosArrayBinding);
    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, methodBinding, ASTFactory.newSimpleName(ast, iosArrayBinding));

    // Add the array length argument.
    ASTUtil.getArguments(invocation).add(NodeCopier.copySubtree(ast, dimensionExpr));

    // Add the type argument for object arrays.
    if (!componentType.isPrimitive()) {
      ASTUtil.getArguments(invocation).add(ASTFactory.newTypeLiteral(ast, componentType));
    }

    return invocation;
  }

  private IOSMethodBinding getSingleDimensionMethod(IOSArrayTypeBinding arrayType) {
    IOSMethodBinding binding = singleDimMethods.get(arrayType);
    if (binding != null) {
      return binding;
    }
    boolean needsTypeParam = arrayType.getName().equals("IOSObjectArray");
    IOSMethod iosMethod = IOSMethod.create(
        arrayType.getName() + " arrayWithLength:(int)length"
        + (needsTypeParam ? " type:(IOSClass *)type" : ""));
    binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC | Modifier.STATIC, arrayType, arrayType);
    binding.addParameter(new GeneratedVariableBinding(
        Types.resolveJavaType("int"), false, true, null, binding));
    if (needsTypeParam) {
      binding.addParameter(new GeneratedVariableBinding(
          Types.getIOSClass(), false, true, null, binding));
    }
    singleDimMethods.put(arrayType, binding);
    return binding;
  }

  private MethodInvocation newMultiDimensionArrayInvocation(
      AST ast, ITypeBinding arrayType, List<Expression> dimensions) {
    assert dimensions.size() > 1;
    ITypeBinding componentType = arrayType;
    for (int i = 0; i < dimensions.size(); i++) {
      componentType = componentType.getComponentType();
    }
    IOSArrayTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getMultiDimensionMethod(iosArrayBinding);
    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, methodBinding, ASTFactory.newSimpleName(ast, iosArrayBinding));

    // Add the dimension count argument.
    ASTUtil.getArguments(invocation).add(ASTFactory.makeLiteral(
        ast, Integer.valueOf(dimensions.size()), Types.resolveJavaType("int")));

    // Create the dimensions array.
    ArrayInitializer dimensionsArg = ast.newArrayInitializer();
    Types.addBinding(dimensionsArg, Types.resolveIOSType("IOSIntArray"));
    for (Expression e : dimensions) {
      ASTUtil.getExpressions(dimensionsArg).add(NodeCopier.copySubtree(ast, e));
    }
    ASTUtil.getArguments(invocation).add(dimensionsArg);

    if (!componentType.isPrimitive()) {
      ASTUtil.getArguments(invocation).add(ASTFactory.newTypeLiteral(ast, componentType));
    }

    return invocation;
  }

  private IOSMethodBinding getMultiDimensionMethod(IOSArrayTypeBinding arrayType) {
    IOSMethodBinding binding = multiDimMethods.get(arrayType);
    if (binding != null) {
      return binding;
    }
    boolean needsTypeParam = arrayType.getName().equals("IOSObjectArray");
    IOSMethod iosMethod = IOSMethod.create(
        arrayType.getName()
        + " arrayWithDimensions:(int)dimensionCount lengths:(int *)dimensionLengths"
        + (needsTypeParam ? " type:(IOSClass *)type" : ""));
    binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC | Modifier.STATIC, Types.resolveIOSType("IOSObjectArray"),
        arrayType);
    binding.addParameter(new GeneratedVariableBinding(
        Types.resolveJavaType("int"), false, true, null, binding));
    binding.addParameter(new GeneratedVariableBinding(
        Types.resolveIOSType("IOSIntArray"), false, true, null, binding));
    if (needsTypeParam) {
      binding.addParameter(new GeneratedVariableBinding(
          Types.getIOSClass(), false, true, null, binding));
    }
    multiDimMethods.put(arrayType, binding);
    return binding;
  }

  @Override
  public void endVisit(ArrayAccess node) {
    AST ast = node.getAST();
    ITypeBinding arrayType = Types.getTypeBinding(node.getArray());
    assert arrayType.isArray();
    ITypeBinding componentType = arrayType.getComponentType();
    IOSArrayTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    Assignment assignment = getArrayAssignment(node);
    if (assignment != null && !componentType.isPrimitive()) {
      assignment.getRightHandSide().accept(this);
      ASTUtil.setProperty(assignment, newArrayAssignment(
          ast, assignment, node, componentType, iosArrayBinding));
    } else {
      boolean assignable = assignment != null || needsAssignableAccess(node);
      ASTUtil.setProperty(node, newArrayAccess(
          ast, node, componentType, iosArrayBinding, assignable));
    }
  }

  private static Assignment getArrayAssignment(ArrayAccess node) {
    ASTNode parent = node.getParent();
    if (parent instanceof Assignment) {
      Assignment assignment = (Assignment) parent;
      if (node == assignment.getLeftHandSide()) {
        return assignment;
      }
    }
    return null;
  }

  private static boolean needsAssignableAccess(ArrayAccess node) {
    ASTNode parent = node.getParent();
    if (parent instanceof PostfixExpression) {
      PostfixExpression.Operator op = ((PostfixExpression) parent).getOperator();
      if (op == PostfixExpression.Operator.INCREMENT
          || op == PostfixExpression.Operator.DECREMENT) {
        return true;
      }
    } else if (parent instanceof PrefixExpression) {
      PrefixExpression.Operator op = ((PrefixExpression) parent).getOperator();
      if (op == PrefixExpression.Operator.INCREMENT || op == PrefixExpression.Operator.DECREMENT) {
        return true;
      }
    }
    return false;
  }

  private static MethodInvocation newArrayAccess(
      AST ast, ArrayAccess arrayAccessNode, ITypeBinding componentType,
      IOSArrayTypeBinding iosArrayBinding, boolean assignable) {
    IOSMethod iosMethod = assignable ? ACCESS_REF_METHODS.get(iosArrayBinding.getName()) :
        ACCESS_METHODS.get(iosArrayBinding.getName());
    assert iosMethod != null;
    ITypeBinding declaredReturnType =
        componentType.isPrimitive() ? componentType : Types.resolveIOSType("id");
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC, declaredReturnType, iosArrayBinding);
    binding.addParameter(new GeneratedVariableBinding(
        Types.resolveJavaType("int"), false, true, null, binding));
    if (!componentType.isPrimitive()) {
      binding = IOSMethodBinding.newTypedInvocation(binding, componentType);
    }

    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, binding, NodeCopier.copySubtree(ast, arrayAccessNode.getArray()));
    ASTUtil.getArguments(invocation).add(NodeCopier.copySubtree(ast, arrayAccessNode.getIndex()));
    return invocation;
  }

  private static MethodInvocation newArrayAssignment(
      AST ast, Assignment assignmentNode, ArrayAccess arrayAccessNode, ITypeBinding componentType,
      IOSArrayTypeBinding iosArrayBinding) {
    Assignment.Operator op = assignmentNode.getOperator();
    assert !componentType.isPrimitive();
    assert op == Assignment.Operator.ASSIGN;

    ITypeBinding idType = Types.resolveIOSType("id");
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        OBJECT_ARRAY_ASSIGNMENT, Modifier.PUBLIC, idType, iosArrayBinding);
    binding.addParameter(new GeneratedVariableBinding(
        Types.resolveJavaType("int"), false, true, null, binding));
    binding.addParameter(new GeneratedVariableBinding(idType, false, true, null, binding));
    binding = IOSMethodBinding.newTypedInvocation(binding, componentType);

    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, binding, NodeCopier.copySubtree(ast, arrayAccessNode.getArray()));
    ASTUtil.getArguments(invocation).add(NodeCopier.copySubtree(ast, arrayAccessNode.getIndex()));
    ASTUtil.getArguments(invocation).add(
        NodeCopier.copySubtree(ast, assignmentNode.getRightHandSide()));
    return invocation;
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    rewriteVarargs(Types.getMethodBinding(node), ASTUtil.getArguments(node), node.getAST());
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    rewriteVarargs(Types.getMethodBinding(node), ASTUtil.getArguments(node), node.getAST());
  }

  @Override
  public void endVisit(EnumConstantDeclaration node) {
    rewriteVarargs(Types.getMethodBinding(node), ASTUtil.getArguments(node), node.getAST());
  }

  @Override
  public void endVisit(MethodInvocation node) {
    rewriteVarargs(Types.getMethodBinding(node), ASTUtil.getArguments(node), node.getAST());
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    rewriteVarargs(Types.getMethodBinding(node), ASTUtil.getArguments(node), node.getAST());
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    rewriteVarargs(Types.getMethodBinding(node), ASTUtil.getArguments(node), node.getAST());
  }
}
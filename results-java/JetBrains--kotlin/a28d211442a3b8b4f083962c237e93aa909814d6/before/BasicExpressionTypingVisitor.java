/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
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

package org.jetbrains.jet.lang.types.expressions;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.JetNodeTypes;
import org.jetbrains.jet.lang.PlatformToKotlinClassMap;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.descriptors.annotations.AnnotationDescriptor;
import org.jetbrains.jet.lang.descriptors.impl.FunctionDescriptorUtil;
import org.jetbrains.jet.lang.diagnostics.Errors;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.*;
import org.jetbrains.jet.lang.resolve.calls.CallExpressionResolver;
import org.jetbrains.jet.lang.resolve.calls.autocasts.DataFlowInfo;
import org.jetbrains.jet.lang.resolve.calls.autocasts.DataFlowValue;
import org.jetbrains.jet.lang.resolve.calls.autocasts.DataFlowValueFactory;
import org.jetbrains.jet.lang.resolve.calls.autocasts.Nullability;
import org.jetbrains.jet.lang.resolve.calls.context.*;
import org.jetbrains.jet.lang.resolve.calls.model.ResolvedCall;
import org.jetbrains.jet.lang.resolve.calls.model.ResolvedCallWithTrace;
import org.jetbrains.jet.lang.resolve.calls.model.VariableAsFunctionResolvedCall;
import org.jetbrains.jet.lang.resolve.calls.results.OverloadResolutionResults;
import org.jetbrains.jet.lang.resolve.calls.results.OverloadResolutionResultsImpl;
import org.jetbrains.jet.lang.resolve.calls.results.OverloadResolutionResultsUtil;
import org.jetbrains.jet.lang.resolve.calls.util.CallMaker;
import org.jetbrains.jet.lang.resolve.calls.util.ExpressionAsFunctionDescriptor;
import org.jetbrains.jet.lang.resolve.constants.*;
import org.jetbrains.jet.lang.resolve.constants.StringValue;
import org.jetbrains.jet.lang.resolve.name.LabelName;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.resolve.scopes.WritableScopeImpl;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ExpressionReceiver;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverValue;
import org.jetbrains.jet.lang.resolve.scopes.receivers.TransientReceiver;
import org.jetbrains.jet.lang.types.*;
import org.jetbrains.jet.lang.types.checker.JetTypeChecker;
import org.jetbrains.jet.lang.types.checker.TypeCheckingProcedure;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;
import org.jetbrains.jet.lexer.JetTokens;
import org.jetbrains.jet.utils.ThrowingList;

import java.util.*;

import static org.jetbrains.jet.lang.descriptors.ReceiverParameterDescriptor.NO_RECEIVER_PARAMETER;
import static org.jetbrains.jet.lang.diagnostics.Errors.*;
import static org.jetbrains.jet.lang.resolve.BindingContext.*;
import static org.jetbrains.jet.lang.resolve.DescriptorUtils.getStaticNestedClassesScope;
import static org.jetbrains.jet.lang.resolve.calls.context.ContextDependency.DEPENDENT;
import static org.jetbrains.jet.lang.resolve.calls.context.ContextDependency.INDEPENDENT;
import static org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverValue.NO_RECEIVER;
import static org.jetbrains.jet.lang.types.TypeUtils.NO_EXPECTED_TYPE;
import static org.jetbrains.jet.lang.types.TypeUtils.noExpectedType;
import static org.jetbrains.jet.lang.types.expressions.ControlStructureTypingUtils.*;
import static org.jetbrains.jet.lang.types.expressions.ExpressionTypingUtils.*;

@SuppressWarnings("SuspiciousMethodCalls")
public class BasicExpressionTypingVisitor extends ExpressionTypingVisitor {

    private final PlatformToKotlinClassMap platformToKotlinClassMap;

    protected BasicExpressionTypingVisitor(@NotNull ExpressionTypingInternals facade, @NotNull PlatformToKotlinClassMap platformToKotlinClassMap) {
        super(facade);
        this.platformToKotlinClassMap = platformToKotlinClassMap;
    }

    @Override
    public JetTypeInfo visitSimpleNameExpression(JetSimpleNameExpression expression, ExpressionTypingContext context) {
        // TODO : other members
        // TODO : type substitutions???
        CallExpressionResolver callExpressionResolver = context.expressionTypingServices.getCallExpressionResolver();
        JetTypeInfo typeInfo = callExpressionResolver.getSimpleNameExpressionTypeInfo(expression, NO_RECEIVER, null, context);
        JetType type = DataFlowUtils.checkType(typeInfo.getType(), expression, context);
        ExpressionTypingUtils.checkCapturingInClosure(expression, context.trace, context.scope);
        return JetTypeInfo.create(type, typeInfo.getDataFlowInfo()); // TODO : Extensions to this
    }

    @Override
    public JetTypeInfo visitParenthesizedExpression(JetParenthesizedExpression expression, ExpressionTypingContext context) {
        return visitParenthesizedExpression(expression, context, false);
    }

    public JetTypeInfo visitParenthesizedExpression(JetParenthesizedExpression expression, ExpressionTypingContext context, boolean isStatement) {
        JetExpression innerExpression = expression.getExpression();
        if (innerExpression == null) {
            return JetTypeInfo.create(null, context.dataFlowInfo);
        }
        JetTypeInfo typeInfo = facade.getTypeInfo(innerExpression, context.replaceScope(context.scope), isStatement);
        return DataFlowUtils.checkType(typeInfo, expression, context);
    }

    private static JetTypeInfo createNumberValueTypeInfo(
            @NotNull NumberValueTypeConstructor numberValueTypeConstructor,
            @NotNull Number value,
            @NotNull DataFlowInfo dataFlowInfo
    ) {
        return JetTypeInfo.create(new JetTypeImpl(
                Collections.<AnnotationDescriptor>emptyList(), numberValueTypeConstructor,
                false, Collections.<TypeProjection>emptyList(),
                ErrorUtils.createErrorScope("Scope for number value type (" + value + ")", true)), dataFlowInfo);
    }

    @Override
    public JetTypeInfo visitConstantExpression(JetConstantExpression expression, ExpressionTypingContext context) {
        IElementType elementType = expression.getNode().getElementType();
        String text = expression.getNode().getText();
        KotlinBuiltIns builtIns = KotlinBuiltIns.getInstance();
        CompileTimeConstantResolver compileTimeConstantResolver = context.getCompileTimeConstantResolver();

        if (noExpectedType(context.expectedType) && context.contextDependency == DEPENDENT) {
            if (elementType == JetNodeTypes.INTEGER_CONSTANT) {
                Long longValue = CompileTimeConstantResolver.parseLongValue(text);
                if (longValue != null) {
                    return createNumberValueTypeInfo(IntegerValueTypeConstructor.create(longValue), longValue, context.dataFlowInfo);
                }
            }
            else if (elementType == JetNodeTypes.FLOAT_CONSTANT) {
                Double doubleValue = CompileTimeConstantResolver.parseDoubleValue(text);
                if (doubleValue != null) {
                    return createNumberValueTypeInfo(DoubleValueTypeConstructor.create(doubleValue), doubleValue, context.dataFlowInfo);
                }
            }
        }

        CompileTimeConstant<?> value = compileTimeConstantResolver.getCompileTimeConstant(expression, context.expectedType);
        if (value instanceof ErrorValue) {
            // 'checkType' for 'ContextDependency.DEPENDENT' will take place in 'completeInferenceForArgument'
            if (context.contextDependency == INDEPENDENT) {
                context.trace.report(ERROR_COMPILE_TIME_VALUE.on(expression, ((ErrorValue) value).getMessage()));
            }
            return JetTypeInfo.create(getDefaultType(elementType), context.dataFlowInfo);
        }
        context.trace.record(BindingContext.COMPILE_TIME_VALUE, expression, value);
        return DataFlowUtils.checkType(value.getType(builtIns), expression, context, context.dataFlowInfo);
    }

    @Override
    public JetTypeInfo visitBinaryWithTypeRHSExpression(JetBinaryExpressionWithTypeRHS expression, ExpressionTypingContext context) {
        ExpressionTypingContext contextWithNoExpectedType =
                context.replaceExpectedType(NO_EXPECTED_TYPE).replaceContextDependency(INDEPENDENT);
        JetExpression left = expression.getLeft();
        JetTypeReference right = expression.getRight();
        if (right == null) {
            JetTypeInfo leftTypeInfo = facade.getTypeInfo(left, contextWithNoExpectedType);
            return JetTypeInfo.create(null, leftTypeInfo.getDataFlowInfo());
        }

        JetType targetType = context.expressionTypingServices.getTypeResolver().resolveType(context.scope, right, context.trace, true);
        IElementType operationType = expression.getOperationReference().getReferencedNameElementType();

        if (isTypeFlexible(left) || operationType == JetTokens.COLON) {

            JetTypeInfo typeInfo = facade.getTypeInfo(left, contextWithNoExpectedType.replaceExpectedType(targetType));
            checkBinaryWithTypeRHS(expression, context, targetType, typeInfo.getType());
            return DataFlowUtils.checkType(targetType, expression, context, typeInfo.getDataFlowInfo());
        }

        JetTypeInfo typeInfo = facade.getTypeInfo(left, contextWithNoExpectedType);

        DataFlowInfo dataFlowInfo = context.dataFlowInfo;
        if (typeInfo.getType() != null) {
            checkBinaryWithTypeRHS(expression, contextWithNoExpectedType, targetType, typeInfo.getType());
            dataFlowInfo = typeInfo.getDataFlowInfo();
            if (operationType == JetTokens.AS_KEYWORD) {
                DataFlowValue value = DataFlowValueFactory.INSTANCE.createDataFlowValue(left, typeInfo.getType(), context.trace.getBindingContext());
                dataFlowInfo = dataFlowInfo.establishSubtyping(value, targetType);
            }
        }
        JetType result = operationType == JetTokens.AS_SAFE ? TypeUtils.makeNullable(targetType) : targetType;
        return DataFlowUtils.checkType(result, expression, context, dataFlowInfo);
    }

    private static void checkBinaryWithTypeRHS(
            @NotNull JetBinaryExpressionWithTypeRHS expression,
            @NotNull ExpressionTypingContext context,
            @NotNull JetType targetType,
            @Nullable JetType actualType
    ) {
        if (actualType == null) return;
        JetSimpleNameExpression operationSign = expression.getOperationReference();
        IElementType operationType = operationSign.getReferencedNameElementType();
        if (operationType == JetTokens.COLON) {
            return;
        }
        if (operationType != JetTokens.AS_KEYWORD && operationType != JetTokens.AS_SAFE) {
            context.trace.report(UNSUPPORTED.on(operationSign, "binary operation with type RHS"));
            return;
        }
        checkForCastImpossibility(expression, actualType, targetType, context);
    }

    private static void checkForCastImpossibility(
            JetBinaryExpressionWithTypeRHS expression,
            JetType actualType,
            JetType targetType,
            ExpressionTypingContext context
    ) {
        if (actualType == null || noExpectedType(targetType)) return;

        JetTypeChecker typeChecker = JetTypeChecker.INSTANCE;
        if (!typeChecker.isSubtypeOf(targetType, actualType)) {
            if (typeChecker.isSubtypeOf(actualType, targetType)) {
                context.trace.report(USELESS_CAST_STATIC_ASSERT_IS_FINE.on(expression.getOperationReference()));
            }
            else {
                // See JET-58 Make 'as never succeeds' a warning, or even never check for Java (external) types
                context.trace.report(CAST_NEVER_SUCCEEDS.on(expression.getOperationReference()));
            }
        }
        else {
            if (typeChecker.isSubtypeOf(actualType, targetType)) {
                context.trace.report(USELESS_CAST.on(expression.getOperationReference()));
            }
            else {
                if (isCastErased(actualType, targetType, typeChecker)) {
                    context.trace.report(Errors.UNCHECKED_CAST.on(expression, actualType, targetType));
                }
            }
        }
    }

    /**
     * Check if assignment from supertype to subtype is erased.
     * It is an error in "is" statement and warning in "as".
     */
    public static boolean isCastErased(@NotNull JetType supertype, @NotNull JetType subtype, @NotNull JetTypeChecker typeChecker) {
        if (!(subtype.getConstructor().getDeclarationDescriptor() instanceof ClassDescriptor)) {
            // TODO: what if it is TypeParameterDescriptor?
            return false;
        }

        // do not crash on error types
        if (ErrorUtils.isErrorType(supertype) || ErrorUtils.isErrorType(subtype)) {
            return false;
        }

        return hasDowncastsInTypeArguments(supertype, subtype, typeChecker) || hasErasedTypeArguments(supertype, subtype);
    }

    /*
      Check if type arguments are downcasted, which cannot be checked at run-time.

      Examples:
      1. a: MutableList<out Any> is MutableList<String> - true, because 'String' is more specific
      2. a: Collection<String> is List<Any> - false, because 'Any' is less specific, and it is guaranteed by static checker
      3. a: MutableCollection<String> is MutableList<Any> - false, because these types have empty intersection (type parameter is invariant)
     */
    private static boolean hasDowncastsInTypeArguments(
            @NotNull JetType supertype,
            @NotNull JetType subtype,
            @NotNull JetTypeChecker typeChecker
    ) {
        List<TypeParameterDescriptor> superParameters = supertype.getConstructor().getParameters();

        // This map holds arguments for type parameters of all superclasses of a type (see method comment for sample)
        Multimap<TypeConstructor, TypeProjection> subtypeSubstitutionMap = SubstitutionUtils.buildDeepSubstitutionMultimap(subtype);

        for (int i = 0; i < superParameters.size(); i++) {
            TypeProjection superArgument = supertype.getArguments().get(i);
            TypeParameterDescriptor parameter = superParameters.get(i);

            if (parameter.isReified()) {
                continue;
            }

            Collection<TypeProjection> substituted = subtypeSubstitutionMap.get(parameter.getTypeConstructor());
            for (TypeProjection substitutedArgument : substituted) {
                // For sample #2 (a: Collection<String> is List<Any>):
                // parameter = E declared in Collection
                // superArgument = String
                // substitutedArgument = Any, because Collection<Any> is the supertype of List<Any>

                // 1. Any..Nothing
                // 2. String..Nothing
                // 3. String..String
                JetType superOut = TypeCheckingProcedure.getOutType(parameter, superArgument);
                JetType superIn = TypeCheckingProcedure.getInType(parameter, superArgument);

                // 1. String..String
                // 2. Any..Nothing
                // 3. Any..Any
                JetType subOut = TypeCheckingProcedure.getOutType(parameter, substitutedArgument);
                JetType subIn = TypeCheckingProcedure.getInType(parameter, substitutedArgument);

                // super type range must be a subset of sub type range
                if (typeChecker.isSubtypeOf(superOut, subOut) && typeChecker.isSubtypeOf(subIn, superIn)) {
                    // continue
                }
                else {
                    return true;
                }
            }
        }

        return false;
    }

    /*
      Check if type arguments are erased, that is they are not mapped to type parameters of supertype's class

      Examples (MyMap is defined like this: trait MyMap<T>: Map<String, T>):
      1. a: Any is List<String> - true
      2. a: Collection<CharSequence> is List<String> - false
      3. a: Map<String, String> is MyMap<String> - false
     */
    private static boolean hasErasedTypeArguments(
            @NotNull JetType supertype,
            @NotNull JetType subtype
    ) {
        // Erase all type arguments, replacing them with unsubstituted versions:
        // 1. List<E>
        // 2. List<E>
        // 3. MyMap<T>
        JetType subtypeCleared = TypeUtils.makeUnsubstitutedType(
                (ClassDescriptor) subtype.getConstructor().getDeclarationDescriptor(), null);

        // This map holds arguments for type parameters of all superclasses of a type (see method comment for sample)
        // For all "E" declared in Collection, Iterable, etc., value will be type "E", where the latter E is declared in List
        Multimap<TypeConstructor, TypeProjection> clearTypeSubstitutionMap =
                SubstitutionUtils.buildDeepSubstitutionMultimap(subtypeCleared);


        // This set will contain all arguments for type parameters of superclass which are mapped from type parameters of subtype's class
        // 1. empty
        // 2. [E declared in List]
        // 3. [T declared in MyMap]
        Set<JetType> clearSubstituted = new HashSet<JetType>();

        List<TypeParameterDescriptor> superParameters = supertype.getConstructor().getParameters();
        for (TypeParameterDescriptor superParameter : superParameters) {
            Collection<TypeProjection> substituted = clearTypeSubstitutionMap.get(superParameter.getTypeConstructor());
            for (TypeProjection substitutedProjection : substituted) {
                clearSubstituted.add(substitutedProjection.getType());
            }
        }

        // For each type parameter of subtype's class, we check that it is mapped to type parameters of supertype,
        // that is its type is present in clearSubstituted set
        List<TypeParameterDescriptor> subParameters = subtype.getConstructor().getParameters();
        for (int i = 0; i < subParameters.size(); i++) {
            TypeParameterDescriptor parameter = subParameters.get(i);
            TypeProjection argument = subtype.getArguments().get(i);

            if (parameter.isReified()) {
                continue;
            }

            // "is List<*>", no check for type argument, actually
            if (argument.equals(SubstitutionUtils.makeStarProjection(parameter))) {
                continue;
            }

            // if parameter is mapped to nothing then it is erased
            // 1. return from here
            // 2. contains = true, don't return
            // 3. contains = true, don't return
            if (!clearSubstituted.contains(parameter.getDefaultType())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public JetTypeInfo visitThisExpression(JetThisExpression expression, ExpressionTypingContext context) {
        JetType result = null;
        LabelResolver.LabeledReceiverResolutionResult resolutionResult = resolveToReceiver(expression, context, false);

        switch (resolutionResult.getCode()) {
            case LABEL_RESOLUTION_ERROR:
                // Do nothing, the error is already reported
                break;
            case NO_THIS:
                context.trace.report(NO_THIS.on(expression));
                break;
            case SUCCESS:
                result = resolutionResult.getReceiverParameterDescriptor().getType();
                context.trace.record(BindingContext.EXPRESSION_TYPE, expression.getInstanceReference(), result);
                break;
        }
        return DataFlowUtils.checkType(result, expression, context, context.dataFlowInfo);
    }

    @Override
    public JetTypeInfo visitSuperExpression(JetSuperExpression expression, ExpressionTypingContext context) {
        LabelResolver.LabeledReceiverResolutionResult resolutionResult = resolveToReceiver(expression, context, true);

        if (context.expressionPosition == ExpressionPosition.FREE) {
            context.trace.report(SUPER_IS_NOT_AN_EXPRESSION.on(expression, expression.getText()));
            return errorInSuper(expression, context);
        }
        switch (resolutionResult.getCode()) {
            case LABEL_RESOLUTION_ERROR:
                // The error is already reported
                return errorInSuper(expression, context);
            case NO_THIS:
                context.trace.report(SUPER_NOT_AVAILABLE.on(expression));
                return errorInSuper(expression, context);
            case SUCCESS:
                JetType result = checkPossiblyQualifiedSuper(expression, context, resolutionResult.getReceiverParameterDescriptor());
                if (result != null) {
                    context.trace.record(BindingContext.EXPRESSION_TYPE, expression.getInstanceReference(), result);
                }
                return DataFlowUtils.checkType(result, expression, context, context.dataFlowInfo);
        }
        throw new IllegalStateException("Unknown code: " + resolutionResult.getCode());
    }

    private static JetTypeInfo errorInSuper(JetSuperExpression expression, ExpressionTypingContext context) {
        JetTypeReference superTypeQualifier = expression.getSuperTypeQualifier();
        if (superTypeQualifier != null) {
            context.expressionTypingServices.getTypeResolver().resolveType(context.scope, superTypeQualifier, context.trace, true);
        }
        return JetTypeInfo.create(null, context.dataFlowInfo);
    }

    private static JetType checkPossiblyQualifiedSuper(
            JetSuperExpression expression,
            ExpressionTypingContext context,
            ReceiverParameterDescriptor thisReceiver
    ) {
        JetType result = null;
        JetType thisType = thisReceiver.getType();
        Collection<JetType> supertypes = thisType.getConstructor().getSupertypes();
        TypeSubstitutor substitutor = TypeSubstitutor.create(thisType);

        JetTypeReference superTypeQualifier = expression.getSuperTypeQualifier();
        if (superTypeQualifier != null) {
            JetTypeElement typeElement = superTypeQualifier.getTypeElement();

            DeclarationDescriptor classifierCandidate = null;
            JetType supertype = null;
            PsiElement redundantTypeArguments = null;
            if (typeElement instanceof JetUserType) {
                JetUserType userType = (JetUserType) typeElement;
                // This may be just a superclass name even if the superclass is generic
                if (userType.getTypeArguments().isEmpty()) {
                    classifierCandidate = context.expressionTypingServices.getTypeResolver().resolveClass(context.scope, userType, context.trace);
                }
                else {
                    supertype = context.expressionTypingServices.getTypeResolver().resolveType(context.scope, superTypeQualifier, context.trace, true);
                    redundantTypeArguments = userType.getTypeArgumentList();
                }
            }
            else {
                supertype = context.expressionTypingServices.getTypeResolver().resolveType(context.scope, superTypeQualifier, context.trace, true);
            }

            if (supertype != null) {
                if (supertypes.contains(supertype)) {
                    result = supertype;
                }
            }
            else if (classifierCandidate instanceof ClassDescriptor) {
                ClassDescriptor superclass = (ClassDescriptor) classifierCandidate;

                for (JetType declaredSupertype : supertypes) {
                    if (declaredSupertype.getConstructor().equals(superclass.getTypeConstructor())) {
                        result = substitutor.safeSubstitute(declaredSupertype, Variance.INVARIANT);
                        break;
                    }
                }
            }

            boolean validClassifier = classifierCandidate != null && !ErrorUtils.isError(classifierCandidate);
            boolean validType = supertype != null && !ErrorUtils.isErrorType(supertype);
            if (result == null && (validClassifier || validType)) {
                context.trace.report(NOT_A_SUPERTYPE.on(superTypeQualifier));
            }
            else if (redundantTypeArguments != null) {
                context.trace.report(TYPE_ARGUMENTS_REDUNDANT_IN_SUPER_QUALIFIER.on(redundantTypeArguments));
            }
        }
        else {
            if (supertypes.size() > 1) {
                context.trace.report(AMBIGUOUS_SUPER.on(expression));
            }
            else {
                // supertypes may be empty when all the supertypes are error types (are not resolved, for example)
                JetType type = supertypes.isEmpty()
                               ? KotlinBuiltIns.getInstance().getAnyType()
                               : supertypes.iterator().next();
                result = substitutor.substitute(type, Variance.INVARIANT);
            }
        }
        if (result != null) {
            if (DescriptorUtils.isKindOf(thisType, ClassKind.TRAIT)) {
                if (DescriptorUtils.isKindOf(result, ClassKind.CLASS)) {
                    context.trace.report(SUPERCLASS_NOT_ACCESSIBLE_FROM_TRAIT.on(expression));
                }
            }
            context.trace.record(BindingContext.EXPRESSION_TYPE, expression.getInstanceReference(), result);
            context.trace.record(BindingContext.REFERENCE_TARGET, expression.getInstanceReference(), result.getConstructor().getDeclarationDescriptor());
            if (superTypeQualifier != null) {
                context.trace.record(BindingContext.TYPE_RESOLUTION_SCOPE, superTypeQualifier, context.scope);
            }
        }
        return result;
    }

    @NotNull // No class receivers
    private static LabelResolver.LabeledReceiverResolutionResult resolveToReceiver(
            JetLabelQualifiedInstanceExpression expression,
            ExpressionTypingContext context,
            boolean onlyClassReceivers
    ) {
        String labelName = expression.getLabelName();
        if (labelName != null) {
            LabelResolver.LabeledReceiverResolutionResult resolutionResult = context.labelResolver.resolveThisLabel(
                    expression.getInstanceReference(), expression.getTargetLabel(), context, new LabelName(labelName));
            if (onlyClassReceivers && resolutionResult.success()) {
                if (!isDeclaredInClass(resolutionResult.getReceiverParameterDescriptor())) {
                    return LabelResolver.LabeledReceiverResolutionResult.labelResolutionSuccess(NO_RECEIVER_PARAMETER);
                }
            }
            return resolutionResult;
        }
        else {
            ReceiverParameterDescriptor result = NO_RECEIVER_PARAMETER;
            List<ReceiverParameterDescriptor> receivers = context.scope.getImplicitReceiversHierarchy();
            if (onlyClassReceivers) {
                for (ReceiverParameterDescriptor receiver : receivers) {
                    if (isDeclaredInClass(receiver)) {
                        result = receiver;
                        break;
                    }
                }
            }
            else if (!receivers.isEmpty()) {
                result = receivers.get(0);
            }
            if (result != NO_RECEIVER_PARAMETER) {
                context.trace.record(REFERENCE_TARGET, expression.getInstanceReference(), result.getContainingDeclaration());
            }
            return LabelResolver.LabeledReceiverResolutionResult.labelResolutionSuccess(result);
        }
    }

    private static boolean isDeclaredInClass(ReceiverParameterDescriptor receiver) {
        return receiver.getContainingDeclaration() instanceof ClassDescriptor;
    }

    @Override
    public JetTypeInfo visitBlockExpression(JetBlockExpression expression, ExpressionTypingContext context) {
        return visitBlockExpression(expression, context, false);
    }

    public static JetTypeInfo visitBlockExpression(JetBlockExpression expression, ExpressionTypingContext context, boolean isStatement) {
        return context.expressionTypingServices.getBlockReturnedType(
                expression, isStatement ? CoercionStrategy.COERCION_TO_UNIT : CoercionStrategy.NO_COERCION, context);
    }

    @Override
    public JetTypeInfo visitCallableReferenceExpression(JetCallableReferenceExpression expression, ExpressionTypingContext context) {
        JetTypeReference typeReference = expression.getTypeReference();

        JetType receiverType =
                typeReference == null
                ? null
                : context.expressionTypingServices.getTypeResolver().resolveType(context.scope, typeReference, context.trace, false);

        JetSimpleNameExpression callableReference = expression.getCallableReference();
        if (callableReference.getReferencedName().isEmpty()) {
            context.trace.report(UNRESOLVED_REFERENCE.on(callableReference, callableReference));
            JetType errorType = ErrorUtils.createErrorType("Empty callable reference");
            return DataFlowUtils.checkType(errorType, expression, context, context.dataFlowInfo);
        }

        JetType result = getCallableReferenceType(expression, receiverType, context);
        return DataFlowUtils.checkType(result, expression, context, context.dataFlowInfo);
    }

    @Nullable
    private static JetType getCallableReferenceType(
            @NotNull JetCallableReferenceExpression expression,
            @Nullable JetType lhsType,
            @NotNull ExpressionTypingContext context
    ) {
        JetSimpleNameExpression reference = expression.getCallableReference();

        boolean[] result = new boolean[1];
        FunctionDescriptor descriptor = resolveCallableReferenceTarget(lhsType, context, expression, result);

        if (!result[0]) {
            context.trace.report(UNRESOLVED_REFERENCE.on(reference, reference));
        }
        if (descriptor == null) return null;

        ReceiverParameterDescriptor receiverParameter = descriptor.getReceiverParameter();
        ReceiverParameterDescriptor expectedThisObject = descriptor.getExpectedThisObject();
        if (receiverParameter != null && expectedThisObject != null) {
            context.trace.report(EXTENSION_IN_CLASS_REFERENCE_NOT_ALLOWED.on(reference, descriptor));
            return null;
        }

        JetType receiverType = null;
        if (receiverParameter != null) {
            receiverType = receiverParameter.getType();
        }
        else if (expectedThisObject != null) {
            receiverType = expectedThisObject.getType();
        }

        //noinspection ConstantConditions
        JetType type = KotlinBuiltIns.getInstance().getKFunctionType(
                Collections.<AnnotationDescriptor>emptyList(),
                receiverType,
                DescriptorUtils.getValueParametersTypes(descriptor.getValueParameters()),
                descriptor.getReturnType(),
                receiverParameter != null
        );

        ExpressionAsFunctionDescriptor functionDescriptor = new ExpressionAsFunctionDescriptor(
                context.scope.getContainingDeclaration(),
                Name.special("<callable-reference>")
        );
        FunctionDescriptorUtil.initializeFromFunctionType(functionDescriptor, type, null, Modality.FINAL, Visibilities.PUBLIC);

        context.trace.record(CALLABLE_REFERENCE, expression, functionDescriptor);

        return type;
    }

    @Nullable
    private static FunctionDescriptor resolveCallableReferenceTarget(
            @Nullable JetType lhsType,
            @NotNull ExpressionTypingContext context,
            @NotNull JetCallableReferenceExpression expression,
            @NotNull boolean[] result
    ) {
        JetSimpleNameExpression reference = expression.getCallableReference();

        if (lhsType == null) {
            return resolveCallableNotCheckingArguments(reference, NO_RECEIVER, context, result);
        }

        ClassifierDescriptor classifier = lhsType.getConstructor().getDeclarationDescriptor();
        if (!(classifier instanceof ClassDescriptor)) {
            context.trace.report(CALLABLE_REFERENCE_LHS_NOT_A_CLASS.on(expression));
            return null;
        }

        ReceiverValue receiver = new TransientReceiver(lhsType);
        TemporaryTraceAndCache temporaryWithReceiver = TemporaryTraceAndCache.create(
                context, "trace to resolve callable reference with receiver", reference);
        FunctionDescriptor descriptor = resolveCallableNotCheckingArguments(
                reference, receiver, context.replaceTraceAndCache(temporaryWithReceiver), result);
        if (result[0]) {
            temporaryWithReceiver.commit();
            return descriptor;
        }

        JetScope staticScope = getStaticNestedClassesScope((ClassDescriptor) classifier);
        TemporaryTraceAndCache temporaryForStatic = TemporaryTraceAndCache.create(
                context, "trace to resolve callable reference in static scope", reference);
        FunctionDescriptor possibleStaticNestedClassConstructor = resolveCallableNotCheckingArguments(reference, NO_RECEIVER,
                context.replaceTraceAndCache(temporaryForStatic).replaceScope(staticScope), result);
        if (result[0]) {
            temporaryForStatic.commit();
            return possibleStaticNestedClassConstructor;
        }

        return null;
    }

    @Nullable
    private static FunctionDescriptor resolveCallableNotCheckingArguments(
            @NotNull JetSimpleNameExpression reference,
            @NotNull ReceiverValue receiver,
            @NotNull ExpressionTypingContext context,
            @NotNull boolean[] result
    ) {
        Call call = CallMaker.makeCall(reference, receiver, null, reference, ThrowingList.<ValueArgument>instance());

        TemporaryBindingTrace trace = TemporaryBindingTrace.create(context.trace, "trace to resolve as function", reference);

        ExpressionTypingContext contextForResolve = context.replaceBindingTrace(trace).replaceExpectedType(NO_EXPECTED_TYPE);
        ResolvedCallWithTrace<FunctionDescriptor> function = contextForResolve.expressionTypingServices.getCallExpressionResolver()
                .getResolvedCallForFunction(call, reference, contextForResolve, CheckValueArgumentsMode.DISABLED, result);
        if (!result[0]) return null;

        if (function instanceof VariableAsFunctionResolvedCall) {
            // TODO: KProperty
            context.trace.report(UNSUPPORTED.on(reference, "References to variables aren't supported yet"));
            context.trace.report(UNRESOLVED_REFERENCE.on(reference, reference));
            return null;
        }

        trace.commit();
        return function != null ? function.getResultingDescriptor() : null;
    }

    @Override
    public JetTypeInfo visitQualifiedExpression(JetQualifiedExpression expression, ExpressionTypingContext context) {
        CallExpressionResolver callExpressionResolver = context.expressionTypingServices.getCallExpressionResolver();
        return callExpressionResolver.getQualifiedExpressionTypeInfo(expression, context);
    }

    @Override
    public JetTypeInfo visitCallExpression(JetCallExpression expression, ExpressionTypingContext context) {
        CallExpressionResolver callExpressionResolver = context.expressionTypingServices.getCallExpressionResolver();
        return callExpressionResolver.getCallExpressionTypeInfo(expression, NO_RECEIVER, null, context);
    }

    @Override
    public JetTypeInfo visitUnaryExpression(JetUnaryExpression expression, ExpressionTypingContext context) {
        return visitUnaryExpression(expression, context, false);
    }

    public JetTypeInfo visitUnaryExpression(JetUnaryExpression expression, ExpressionTypingContext context, boolean isStatement) {
        JetExpression baseExpression = expression.getBaseExpression();
        if (baseExpression == null) return JetTypeInfo.create(null, context.dataFlowInfo);

        JetSimpleNameExpression operationSign = expression.getOperationReference();

        IElementType operationType = operationSign.getReferencedNameElementType();
        // If it's a labeled expression
        if (JetTokens.LABELS.contains(operationType)) {
            return visitLabeledExpression(expression, context, isStatement);
        }

        // Special case for expr!!
        if (operationType == JetTokens.EXCLEXCL) {
            return visitExclExclExpression(expression, context);
        }

        // Type check the base expression
        JetTypeInfo typeInfo = facade.getTypeInfo(
                baseExpression, context.replaceExpectedType(NO_EXPECTED_TYPE).replaceContextDependency(INDEPENDENT));
        JetType type = typeInfo.getType();
        if (type == null) {
            return typeInfo;
        }
        DataFlowInfo dataFlowInfo = typeInfo.getDataFlowInfo();

        // Conventions for unary operations
        Name name = OperatorConventions.UNARY_OPERATION_NAMES.get(operationType);
        if (name == null) {
            context.trace.report(UNSUPPORTED.on(operationSign, "visitUnaryExpression"));
            return JetTypeInfo.create(null, dataFlowInfo);
        }

        // a[i]++/-- takes special treatment because it is actually let j = i, arr = a in arr.set(j, a.get(j).inc())
        if ((operationType == JetTokens.PLUSPLUS || operationType == JetTokens.MINUSMINUS) && baseExpression instanceof JetArrayAccessExpression) {
            JetExpression stubExpression = ExpressionTypingUtils.createFakeExpressionOfType(baseExpression.getProject(), context.trace, "$e", type);
            resolveArrayAccessSetMethod((JetArrayAccessExpression) baseExpression,
                                        stubExpression,
                                        context.replaceExpectedType(NO_EXPECTED_TYPE).replaceBindingTrace(
                                                TemporaryBindingTrace.create(context.trace, "trace to resolve array access set method for unary expression", expression)),
                                        context.trace);
        }

        ExpressionReceiver receiver = new ExpressionReceiver(baseExpression, type);

        // Resolve the operation reference
        OverloadResolutionResults<FunctionDescriptor> resolutionResults = context.resolveCallWithGivenName(
                CallMaker.makeCall(receiver, expression),
                expression.getOperationReference(),
                name);

        if (!resolutionResults.isSuccess()) {
            return JetTypeInfo.create(null, dataFlowInfo);
        }

        // Computing the return type
        JetType returnType = resolutionResults.getResultingDescriptor().getReturnType();
        JetType result;
        if (operationType == JetTokens.PLUSPLUS || operationType == JetTokens.MINUSMINUS) {
            assert returnType != null : "returnType is null for " + resolutionResults.getResultingDescriptor();
            if (JetTypeChecker.INSTANCE.isSubtypeOf(returnType, KotlinBuiltIns.getInstance().getUnitType())) {
                result = ErrorUtils.createErrorType(KotlinBuiltIns.getInstance().getUnit().getName().asString());
                context.trace.report(INC_DEC_SHOULD_NOT_RETURN_UNIT.on(operationSign));
            }
            else {
                JetType receiverType = receiver.getType();
                if (!JetTypeChecker.INSTANCE.isSubtypeOf(returnType, receiverType)) {
                    context.trace.report(RESULT_TYPE_MISMATCH.on(operationSign, name.asString(), receiverType, returnType));
                }
                else {
                    context.trace.record(BindingContext.VARIABLE_REASSIGNMENT, expression);

                    checkLValue(context.trace, baseExpression);
                }
                // TODO : Maybe returnType?
                result = receiverType;
            }
        }
        else {
            result = returnType;
        }
        return DataFlowUtils.checkType(result, expression, context, dataFlowInfo);
    }

    private JetTypeInfo visitExclExclExpression(@NotNull JetUnaryExpression expression, @NotNull ExpressionTypingContext context) {
        JetExpression baseExpression = expression.getBaseExpression();
        assert baseExpression != null;
        JetSimpleNameExpression operationSign = expression.getOperationReference();
        assert operationSign.getReferencedNameElementType() == JetTokens.EXCLEXCL;

        Call call = createCallForSpecialConstruction(expression, Collections.singletonList(baseExpression));
        resolveSpecialConstructionAsCall(
                call, "ExclExcl", Collections.singletonList("baseExpr"), Collections.singletonList(true), context, null);
        JetTypeInfo baseTypeInfo = BindingContextUtils.getRecordedTypeInfo(baseExpression, context.trace.getBindingContext());
        assert baseTypeInfo != null : "Base expression was not processed: " + expression;
        JetType baseType = baseTypeInfo.getType();
        if (baseType == null) {
            return baseTypeInfo;
        }
        DataFlowInfo dataFlowInfo = baseTypeInfo.getDataFlowInfo();
        if (isKnownToBeNotNull(baseExpression, context) && !ErrorUtils.isErrorType(baseType)) {
            context.trace.report(UNNECESSARY_NOT_NULL_ASSERTION.on(operationSign, baseType));
        }
        else {
            DataFlowValue value = DataFlowValueFactory.INSTANCE.createDataFlowValue(baseExpression, baseType, context.trace.getBindingContext());
            dataFlowInfo = dataFlowInfo.disequate(value, DataFlowValue.NULL);
        }
        return JetTypeInfo.create(TypeUtils.makeNotNullable(baseType), dataFlowInfo);
    }

    private JetTypeInfo visitLabeledExpression(@NotNull JetUnaryExpression expression, @NotNull ExpressionTypingContext context,
            boolean isStatement) {
        JetExpression baseExpression = expression.getBaseExpression();
        assert baseExpression != null;
        JetSimpleNameExpression operationSign = expression.getOperationReference();
        assert JetTokens.LABELS.contains(operationSign.getReferencedNameElementType());

        String referencedName = operationSign.getReferencedName();
        context.labelResolver.enterLabeledElement(new LabelName(referencedName.substring(1)), baseExpression);
        // TODO : Some processing for the label?
        JetTypeInfo typeInfo = facade.getTypeInfo(baseExpression, context, isStatement);
        context.labelResolver.exitLabeledElement(baseExpression);
        return DataFlowUtils.checkType(typeInfo, expression, context);
    }

    private static boolean isKnownToBeNotNull(JetExpression expression, ExpressionTypingContext context) {
        JetType type = context.trace.get(EXPRESSION_TYPE, expression);
        assert type != null : "This method is only supposed to be called when the type is not null";
        return isKnownToBeNotNull(expression, type, context);
    }

    private static boolean isKnownToBeNotNull(JetExpression expression, JetType jetType, ExpressionTypingContext context) {
        DataFlowValue dataFlowValue = DataFlowValueFactory.INSTANCE.createDataFlowValue(expression, jetType, context.trace.getBindingContext());
        return !context.dataFlowInfo.getNullability(dataFlowValue).canBeNull();
    }

    public static void checkLValue(@NotNull BindingTrace trace, @NotNull JetExpression expression) {
        checkLValue(trace, expression, false);
    }

    private static void checkLValue(@NotNull BindingTrace trace, @NotNull JetExpression expressionWithParenthesis, boolean canBeThis) {
        JetExpression expression = JetPsiUtil.deparenthesize(expressionWithParenthesis);
        if (expression instanceof JetArrayAccessExpression) {
            JetExpression arrayExpression = ((JetArrayAccessExpression) expression).getArrayExpression();
            if (arrayExpression != null) {
                checkLValue(trace, arrayExpression, true);
            }
            return;
        }
        if (canBeThis && expression instanceof JetThisExpression) return;
        VariableDescriptor variable = BindingContextUtils.extractVariableDescriptorIfAny(trace.getBindingContext(), expression, true);
        if (variable == null) {
            trace.report(VARIABLE_EXPECTED.on(expression != null ? expression : expressionWithParenthesis));
        }
    }

    @Override
    public JetTypeInfo visitBinaryExpression(JetBinaryExpression expression, ExpressionTypingContext contextWithExpectedType) {
        ExpressionTypingContext context = isBinaryExpressionDependentOnExpectedType(expression)
                ? contextWithExpectedType
                : contextWithExpectedType.replaceContextDependency(INDEPENDENT).replaceExpectedType(NO_EXPECTED_TYPE);

        JetSimpleNameExpression operationSign = expression.getOperationReference();
        JetExpression left = expression.getLeft();
        JetExpression right = expression.getRight();
        IElementType operationType = operationSign.getReferencedNameElementType();

        JetTypeInfo result;

        //Expressions that can depend on expected type
        if (operationType == JetTokens.IDENTIFIER) {
            Name referencedName = operationSign.getReferencedNameAsName();
            result = getTypeInfoForBinaryCall(referencedName, context, expression);
        }
        else if (OperatorConventions.BINARY_OPERATION_NAMES.containsKey(operationType)) {
            Name referencedName = OperatorConventions.BINARY_OPERATION_NAMES.get(operationType);
            result = getTypeInfoForBinaryCall(referencedName, context, expression);
        }
        else if (operationType == JetTokens.ELVIS) {
            //base expression of elvis operator is checked for 'type mismatch', so the whole expression shouldn't be checked
            return visitElvisExpression(expression, context);
        }

        //Expressions that don't depend on expected type
        else if (operationType == JetTokens.EQ) {
            result = visitAssignment(expression, context);
        }
        else if (OperatorConventions.ASSIGNMENT_OPERATIONS.containsKey(operationType)) {
            result = visitAssignmentOperation(expression, context);
        }
        else if (OperatorConventions.COMPARISON_OPERATIONS.contains(operationType)) {
            result = visitComparison(expression, context, operationSign);
        }
        else if (OperatorConventions.EQUALS_OPERATIONS.contains(operationType)) {
            result = visitEquality(expression, context, operationSign, left, right);
        }
        else if (operationType == JetTokens.EQEQEQ || operationType == JetTokens.EXCLEQEQEQ) {
            ensureNonemptyIntersectionOfOperandTypes(expression, context);
            // TODO : Check comparison pointlessness
            result = JetTypeInfo.create(KotlinBuiltIns.getInstance().getBooleanType(), context.dataFlowInfo);
        }
        else if (OperatorConventions.IN_OPERATIONS.contains(operationType)) {
            result = checkInExpression(expression, operationSign, left, right, context);
        }
        else if (OperatorConventions.BOOLEAN_OPERATIONS.containsKey(operationType)) {
            result = visitBooleanOperationExpression(operationType, left, right, context);
        }
        else {
            context.trace.report(UNSUPPORTED.on(operationSign, "Unknown operation"));
            result = JetTypeInfo.create(null, context.dataFlowInfo);
        }
        return DataFlowUtils.checkType(result, expression, contextWithExpectedType);
    }

    private JetTypeInfo visitEquality(
            JetBinaryExpression expression,
            ExpressionTypingContext context,
            JetSimpleNameExpression operationSign,
            JetExpression left,
            JetExpression right
    ) {
        JetTypeInfo result;DataFlowInfo dataFlowInfo = context.dataFlowInfo;
        if (right != null && left != null) {
            ExpressionReceiver receiver = ExpressionTypingUtils.safeGetExpressionReceiver(facade, left, context);

            JetTypeInfo leftTypeInfo = getTypeInfoOrNullType(left, context, facade);

            dataFlowInfo = leftTypeInfo.getDataFlowInfo();
            ExpressionTypingContext contextWithDataFlow = context.replaceDataFlowInfo(dataFlowInfo);

            OverloadResolutionResults<FunctionDescriptor> resolutionResults = resolveFakeCall(
                    contextWithDataFlow, receiver, OperatorConventions.EQUALS, KotlinBuiltIns.getInstance().getNullableAnyType());

            dataFlowInfo = facade.getTypeInfo(right, contextWithDataFlow).getDataFlowInfo();

            if (resolutionResults.isSuccess()) {
                FunctionDescriptor equals = resolutionResults.getResultingCall().getResultingDescriptor();
                context.trace.record(REFERENCE_TARGET, operationSign, equals);
                context.trace.record(RESOLVED_CALL, operationSign, resolutionResults.getResultingCall());
                if (ensureBooleanResult(operationSign, OperatorConventions.EQUALS, equals.getReturnType(), context)) {
                    ensureNonemptyIntersectionOfOperandTypes(expression, context);
                }
            }
            else {
                if (resolutionResults.isAmbiguity()) {
                    context.trace.report(OVERLOAD_RESOLUTION_AMBIGUITY.on(operationSign, resolutionResults.getResultingCalls()));
                }
                else {
                    context.trace.report(EQUALS_MISSING.on(operationSign));
                }
            }
        }
        result = JetTypeInfo.create(KotlinBuiltIns.getInstance().getBooleanType(), dataFlowInfo);
        return result;
    }

    @NotNull
    private JetTypeInfo visitComparison(
            @NotNull JetBinaryExpression expression,
            @NotNull ExpressionTypingContext context,
            @NotNull JetSimpleNameExpression operationSign
    ) {
        JetTypeInfo typeInfo = getTypeInfoForBinaryCall(OperatorConventions.COMPARE_TO, context, expression);
        DataFlowInfo dataFlowInfo = typeInfo.getDataFlowInfo();
        JetType compareToReturnType = typeInfo.getType();
        JetType type = null;
        if (compareToReturnType != null && !ErrorUtils.isErrorType(compareToReturnType)) {
            TypeConstructor constructor = compareToReturnType.getConstructor();
            KotlinBuiltIns builtIns = KotlinBuiltIns.getInstance();
            TypeConstructor intTypeConstructor = builtIns.getInt().getTypeConstructor();
            if (constructor.equals(intTypeConstructor)) {
                type = builtIns.getBooleanType();
            }
            else {
                context.trace.report(COMPARE_TO_TYPE_MISMATCH.on(operationSign, compareToReturnType));
            }
        }
        return JetTypeInfo.create(type, dataFlowInfo);
    }

    @NotNull
    private JetTypeInfo visitBooleanOperationExpression(
            @Nullable IElementType operationType,
            @Nullable JetExpression left,
            @Nullable JetExpression right,
            @NotNull ExpressionTypingContext context
    ) {
        JetType booleanType = KotlinBuiltIns.getInstance().getBooleanType();
        JetTypeInfo leftTypeInfo = getTypeInfoOrNullType(left, context, facade);

        JetType leftType = leftTypeInfo.getType();
        DataFlowInfo dataFlowInfo = leftTypeInfo.getDataFlowInfo();

        WritableScopeImpl leftScope = newWritableScopeImpl(context, "Left scope of && or ||");
        // TODO: This gets computed twice: here and in extractDataFlowInfoFromCondition() for the whole condition
        boolean isAnd = operationType == JetTokens.ANDAND;
        DataFlowInfo flowInfoLeft = DataFlowUtils.extractDataFlowInfoFromCondition(left, isAnd, context).and(dataFlowInfo);
        WritableScopeImpl rightScope = isAnd ? leftScope : newWritableScopeImpl(context, "Right scope of && or ||");

        ExpressionTypingContext contextForRightExpr = context.replaceDataFlowInfo(flowInfoLeft).replaceScope(rightScope);
        JetType rightType = right != null ? facade.getTypeInfo(right, contextForRightExpr).getType() : null;
        if (left != null && leftType != null && !isBoolean(leftType)) {
            context.trace.report(TYPE_MISMATCH.on(left, booleanType, leftType));
        }
        if (rightType != null && !isBoolean(rightType)) {
            context.trace.report(TYPE_MISMATCH.on(right, booleanType, rightType));
        }
        return JetTypeInfo.create(booleanType, dataFlowInfo);
    }

    @NotNull
    private JetTypeInfo visitElvisExpression(
            @NotNull JetBinaryExpression expression,
            @NotNull ExpressionTypingContext contextWithExpectedType
    ) {
        ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(NO_EXPECTED_TYPE);
        JetExpression left = expression.getLeft();
        JetExpression right = expression.getRight();

        if (left == null || right == null) {
            getTypeInfoOrNullType(left, context, facade);
            return JetTypeInfo.create(null, context.dataFlowInfo);
        }

        Call call = createCallForSpecialConstruction(expression, Lists.newArrayList(left, right));
        ResolvedCall<FunctionDescriptor> resolvedCall = resolveSpecialConstructionAsCall(
                call, "Elvis", Lists.newArrayList("left", "right"), Lists.newArrayList(true, false), contextWithExpectedType, null);
        JetTypeInfo leftTypeInfo = BindingContextUtils.getRecordedTypeInfo(left, context.trace.getBindingContext());
        assert leftTypeInfo != null : "Left expression was not processed: " + expression;
        JetType leftType = leftTypeInfo.getType();
        if (leftType != null && isKnownToBeNotNull(left, leftType, context)) {
            context.trace.report(USELESS_ELVIS.on(left, leftType));
        }
        JetTypeInfo rightTypeInfo = BindingContextUtils.getRecordedTypeInfo(right, context.trace.getBindingContext());
        assert rightTypeInfo != null : "Right expression was not processed: " + expression;
        JetType rightType = rightTypeInfo.getType();

        DataFlowInfo dataFlowInfo = resolvedCall.getDataFlowInfoForArguments().getResultInfo();
        JetType type = resolvedCall.getResultingDescriptor().getReturnType();
        if (type == null || rightType == null) return JetTypeInfo.create(null, dataFlowInfo);

        return JetTypeInfo.create(TypeUtils.makeNullableAsSpecified(type, rightType.isNullable()), dataFlowInfo);
    }

    @NotNull
    public JetTypeInfo checkInExpression(
            @NotNull JetElement callElement,
            @NotNull JetSimpleNameExpression operationSign,
            @Nullable JetExpression left,
            @Nullable JetExpression right,
            @NotNull ExpressionTypingContext context
    ) {
        ExpressionTypingContext contextWithNoExpectedType = context.replaceExpectedType(NO_EXPECTED_TYPE);
        if (right == null) {
            if (left != null) facade.getTypeInfo(left, contextWithNoExpectedType);
            return JetTypeInfo.create(null, context.dataFlowInfo);
        }

        DataFlowInfo dataFlowInfo = facade.getTypeInfo(right, contextWithNoExpectedType).getDataFlowInfo();

        ExpressionReceiver receiver = safeGetExpressionReceiver(facade, right, contextWithNoExpectedType);
        ExpressionTypingContext contextWithDataFlow = context.replaceDataFlowInfo(dataFlowInfo);

        OverloadResolutionResults<FunctionDescriptor> resolutionResult = contextWithDataFlow.resolveCallWithGivenName(
                CallMaker.makeCallWithExpressions(callElement, receiver, null, operationSign, Collections.singletonList(left)),
                operationSign,
                OperatorConventions.CONTAINS);
        JetType containsType = OverloadResolutionResultsUtil.getResultingType(resolutionResult, context.contextDependency);
        ensureBooleanResult(operationSign, OperatorConventions.CONTAINS, containsType, context);

        if (left != null) {
            dataFlowInfo = facade.getTypeInfo(left, contextWithDataFlow).getDataFlowInfo().and(dataFlowInfo);
        }

        return JetTypeInfo.create(resolutionResult.isSuccess() ? KotlinBuiltIns.getInstance().getBooleanType() : null, dataFlowInfo);
    }

    private void ensureNonemptyIntersectionOfOperandTypes(JetBinaryExpression expression, ExpressionTypingContext context) {
        JetExpression left = expression.getLeft();
        if (left == null) return;

        JetExpression right = expression.getRight();

        // TODO : duplicated effort for == and !=
        JetType leftType = facade.getTypeInfo(left, context).getType();
        if (leftType != null && right != null) {
            JetType rightType = facade.getTypeInfo(right, context).getType();

            if (rightType != null) {
                if (TypeUtils.isIntersectionEmpty(leftType, rightType)) {
                    context.trace.report(EQUALITY_NOT_APPLICABLE.on(expression, expression.getOperationReference(), leftType, rightType));
                }
                checkSenselessComparisonWithNull(expression, left, right, context);
            }
        }
    }

    private void checkSenselessComparisonWithNull(@NotNull JetBinaryExpression expression, @NotNull JetExpression left, @NotNull JetExpression right, @NotNull ExpressionTypingContext context) {
        JetExpression expr;
        if (JetPsiUtil.isNullConstant(left)) {
            expr = right;
        }
        else if (JetPsiUtil.isNullConstant(right)) {
            expr = left;
        }
        else return;

        JetSimpleNameExpression operationSign = expression.getOperationReference();
        JetType type = facade.getTypeInfo(expr, context).getType();
        if (type == null || ErrorUtils.isErrorType(type)) return;

        DataFlowValue value = DataFlowValueFactory.INSTANCE.createDataFlowValue(expr, type, context.trace.getBindingContext());
        Nullability nullability = context.dataFlowInfo.getNullability(value);

        boolean expressionIsAlways;
        boolean equality = operationSign.getReferencedNameElementType() == JetTokens.EQEQ || operationSign.getReferencedNameElementType() == JetTokens.EQEQEQ;

        if (nullability == Nullability.NULL) {
            expressionIsAlways = equality;
        }
        else if (nullability == Nullability.NOT_NULL) {
            expressionIsAlways = !equality;
        }
        else return;

        context.trace.report(SENSELESS_COMPARISON.on(expression, expression, expressionIsAlways));
    }

    @NotNull
    private JetTypeInfo visitAssignmentOperation(JetBinaryExpression expression, ExpressionTypingContext context) {
        return assignmentIsNotAnExpressionError(expression, context);
    }

    @NotNull
    private JetTypeInfo visitAssignment(JetBinaryExpression expression, ExpressionTypingContext context) {
        return assignmentIsNotAnExpressionError(expression, context);
    }

    @NotNull
    private JetTypeInfo assignmentIsNotAnExpressionError(JetBinaryExpression expression, ExpressionTypingContext context) {
        facade.checkStatementType(expression, context);
        context.trace.report(ASSIGNMENT_IN_EXPRESSION_CONTEXT.on(expression));
        return JetTypeInfo.create(null, context.dataFlowInfo);
    }

    @Override
    public JetTypeInfo visitArrayAccessExpression(JetArrayAccessExpression expression, ExpressionTypingContext context) {
        JetTypeInfo typeInfo = resolveArrayAccessGetMethod(expression, context);
        return DataFlowUtils.checkType(typeInfo, expression, context);
    }

    @NotNull
    public JetTypeInfo getTypeInfoForBinaryCall(
            @NotNull Name name,
            @NotNull ExpressionTypingContext context,
            @NotNull JetBinaryExpression binaryExpression
    ) {
        JetExpression left = binaryExpression.getLeft();
        DataFlowInfo dataFlowInfo = context.dataFlowInfo;
        if (left != null) {
            //left here is a receiver, so it doesn't depend on expected type
            dataFlowInfo = facade.getTypeInfo(
                    left, context.replaceContextDependency(INDEPENDENT).replaceExpectedType(NO_EXPECTED_TYPE)).getDataFlowInfo();
        }
        ExpressionTypingContext contextWithDataFlow = context.replaceDataFlowInfo(dataFlowInfo);

        OverloadResolutionResults<FunctionDescriptor> resolutionResults;
        if (left != null) {
            ExpressionReceiver receiver = safeGetExpressionReceiver(facade, left, context);
            resolutionResults = getResolutionResultsForBinaryCall(context.scope, name, contextWithDataFlow, binaryExpression, receiver);
        }
        else {
            resolutionResults = OverloadResolutionResultsImpl.nameNotFound();
        }

        JetExpression right = binaryExpression.getRight();
        if (right != null) {
            dataFlowInfo = facade.getTypeInfo(right, contextWithDataFlow).getDataFlowInfo();
        }

        return JetTypeInfo.create(OverloadResolutionResultsUtil.getResultingType(resolutionResults, context.contextDependency), dataFlowInfo);
    }

    @NotNull
    static
        /*package*/ OverloadResolutionResults<FunctionDescriptor> getResolutionResultsForBinaryCall(
            JetScope scope,
            Name name,
            ExpressionTypingContext context,
            JetBinaryExpression binaryExpression,
            ExpressionReceiver receiver
    ) {
//        ExpressionReceiver receiver = safeGetExpressionReceiver(facade, binaryExpression.getLeft(), context.replaceScope(scope));
        return context.replaceScope(scope).resolveCallWithGivenName(
                CallMaker.makeCall(receiver, binaryExpression),
                binaryExpression.getOperationReference(),
                name
        );
    }

    @Override
    public JetTypeInfo visitDeclaration(JetDeclaration dcl, ExpressionTypingContext context) {
        context.trace.report(DECLARATION_IN_ILLEGAL_CONTEXT.on(dcl));
        return JetTypeInfo.create(null, context.dataFlowInfo);
    }

    @Override
    public JetTypeInfo visitRootNamespaceExpression(JetRootNamespaceExpression expression, ExpressionTypingContext context) {
        if (context.expressionPosition == ExpressionPosition.LHS_OF_DOT) {
            return DataFlowUtils.checkType(JetModuleUtil.getRootNamespaceType(expression), expression, context, context.dataFlowInfo);
        }
        context.trace.report(NAMESPACE_IS_NOT_AN_EXPRESSION.on(expression));
        return JetTypeInfo.create(null, context.dataFlowInfo);
    }


    @Override
    public JetTypeInfo visitStringTemplateExpression(JetStringTemplateExpression expression, ExpressionTypingContext contextWithExpectedType) {
        final ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(NO_EXPECTED_TYPE).replaceContextDependency(INDEPENDENT);
        final StringBuilder builder = new StringBuilder();
        final CompileTimeConstant<?>[] value = new CompileTimeConstant<?>[1];
        final DataFlowInfo[] dataFlowInfo = new DataFlowInfo[1];
        dataFlowInfo[0] = context.dataFlowInfo;

        for (JetStringTemplateEntry entry : expression.getEntries()) {
            entry.accept(new JetVisitorVoid() {

                @Override
                public void visitStringTemplateEntryWithExpression(JetStringTemplateEntryWithExpression entry) {
                    JetExpression entryExpression = entry.getExpression();
                    if (entryExpression != null) {
                        JetTypeInfo typeInfo = facade.getTypeInfo(entryExpression, context.replaceDataFlowInfo(dataFlowInfo[0]));
                        dataFlowInfo[0] = typeInfo.getDataFlowInfo();
                    }
                    value[0] = CompileTimeConstantResolver.OUT_OF_RANGE;
                }

                @Override
                public void visitLiteralStringTemplateEntry(JetLiteralStringTemplateEntry entry) {
                    builder.append(entry.getText());
                }

                @Override
                public void visitEscapeStringTemplateEntry(JetEscapeStringTemplateEntry entry) {
                    String text = entry.getText();

                    CompileTimeConstant<?> character = CompileTimeConstantResolver.escapedStringToCharValue(text);
                    if (character instanceof ErrorValue) {
                        context.trace.report(ILLEGAL_ESCAPE_SEQUENCE.on(entry));
                        value[0] = CompileTimeConstantResolver.OUT_OF_RANGE;
                    }
                    else {
                        builder.append(((CharValue) character).getValue());
                    }
                }
            });
        }
        if (value[0] != CompileTimeConstantResolver.OUT_OF_RANGE) {
            context.trace.record(BindingContext.COMPILE_TIME_VALUE, expression, new StringValue(builder.toString()));
        }
        return DataFlowUtils.checkType(KotlinBuiltIns.getInstance().getStringType(), expression, contextWithExpectedType, dataFlowInfo[0]);
    }

    @Override
    public JetTypeInfo visitAnnotatedExpression(JetAnnotatedExpression expression, ExpressionTypingContext data) {
        JetExpression baseExpression = expression.getBaseExpression();
        if (baseExpression == null) {
            return JetTypeInfo.create(null, data.dataFlowInfo);
        }
        return facade.getTypeInfo(baseExpression, data);
    }

    @Override
    public JetTypeInfo visitJetElement(JetElement element, ExpressionTypingContext context) {
        context.trace.report(UNSUPPORTED.on(element, getClass().getCanonicalName()));
        return JetTypeInfo.create(null, context.dataFlowInfo);
    }

    @NotNull
        /*package*/ JetTypeInfo resolveArrayAccessSetMethod(@NotNull JetArrayAccessExpression arrayAccessExpression, @NotNull JetExpression rightHandSide, @NotNull ExpressionTypingContext context, @NotNull BindingTrace traceForResolveResult) {
        return resolveArrayAccessSpecialMethod(arrayAccessExpression, rightHandSide, context, traceForResolveResult, false);
    }

    @NotNull
        /*package*/ JetTypeInfo resolveArrayAccessGetMethod(@NotNull JetArrayAccessExpression arrayAccessExpression, @NotNull ExpressionTypingContext context) {
        return resolveArrayAccessSpecialMethod(arrayAccessExpression, null, context, context.trace, true);
    }

    @NotNull
    private JetTypeInfo resolveArrayAccessSpecialMethod(@NotNull JetArrayAccessExpression arrayAccessExpression,
                                                    @Nullable JetExpression rightHandSide, //only for 'set' method
                                                    @NotNull ExpressionTypingContext oldContext,
                                                    @NotNull BindingTrace traceForResolveResult,
                                                    boolean isGet) {
        JetExpression arrayExpression = arrayAccessExpression.getArrayExpression();
        if (arrayExpression == null) return JetTypeInfo.create(null, oldContext.dataFlowInfo);

        JetTypeInfo arrayTypeInfo = facade.getTypeInfo(arrayExpression, oldContext.replaceExpectedType(NO_EXPECTED_TYPE)
                .replaceContextDependency(INDEPENDENT));
        JetType arrayType = arrayTypeInfo.getType();
        if (arrayType == null) return arrayTypeInfo;

        DataFlowInfo dataFlowInfo = arrayTypeInfo.getDataFlowInfo();
        ExpressionTypingContext context = oldContext.replaceDataFlowInfo(dataFlowInfo);
        ExpressionReceiver receiver = new ExpressionReceiver(arrayExpression, arrayType);
        if (!isGet) assert rightHandSide != null;

        OverloadResolutionResults<FunctionDescriptor> functionResults = context.resolveCallWithGivenName(
                isGet
                ? CallMaker.makeArrayGetCall(receiver, arrayAccessExpression, Call.CallType.ARRAY_GET_METHOD)
                : CallMaker.makeArraySetCall(receiver, arrayAccessExpression, rightHandSide, Call.CallType.ARRAY_SET_METHOD),
                arrayAccessExpression,
                Name.identifier(isGet ? "get" : "set"));

        List<JetExpression> indices = arrayAccessExpression.getIndexExpressions();
        // The accumulated data flow info of all index expressions is saved on the last index
        if (!indices.isEmpty()) {
            dataFlowInfo = facade.getTypeInfo(indices.get(indices.size() - 1), context).getDataFlowInfo();
        }

        if (!isGet) {
            dataFlowInfo = facade.getTypeInfo(rightHandSide, context.replaceDataFlowInfo(dataFlowInfo)).getDataFlowInfo();
        }

        if (!functionResults.isSingleResult()) {
            traceForResolveResult.report(isGet ? NO_GET_METHOD.on(arrayAccessExpression) : NO_SET_METHOD.on(arrayAccessExpression));
            return JetTypeInfo.create(null, dataFlowInfo);
        }
        traceForResolveResult.record(isGet ? INDEXED_LVALUE_GET : INDEXED_LVALUE_SET, arrayAccessExpression, functionResults.getResultingCall());
        return JetTypeInfo.create(functionResults.getResultingDescriptor().getReturnType(), dataFlowInfo);
    }
}
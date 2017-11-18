package org.jetbrains.jet.lang.types.expressions;

import com.google.common.collect.Lists;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.JetNodeTypes;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.*;
import org.jetbrains.jet.lang.resolve.calls.CallMaker;
import org.jetbrains.jet.lang.resolve.calls.DataFlowInfo;
import org.jetbrains.jet.lang.resolve.calls.OverloadResolutionResults;
import org.jetbrains.jet.lang.resolve.constants.*;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.resolve.scopes.WritableScopeImpl;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ExpressionReceiver;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverDescriptor;
import org.jetbrains.jet.lang.types.*;
import org.jetbrains.jet.lexer.JetTokens;

import java.util.*;

import static org.jetbrains.jet.lang.diagnostics.Errors.*;
import static org.jetbrains.jet.lang.resolve.BindingContext.*;
import static org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverDescriptor.NO_RECEIVER;
import static org.jetbrains.jet.lang.types.TypeUtils.NO_EXPECTED_TYPE;
import static org.jetbrains.jet.lang.types.expressions.ExpressionTypingUtils.*;

/**
* @author abreslav
*/
public class BasicExpressionTypingVisitor extends ExpressionTypingVisitor {
    protected BasicExpressionTypingVisitor(@NotNull ExpressionTypingInternals facade) {
        super(facade);
    }

    @Override
    public JetType visitSimpleNameExpression(JetSimpleNameExpression expression, ExpressionTypingContext context) {
        // TODO : other members
        // TODO : type substitutions???
        String referencedName = expression.getReferencedName();
        if (expression.getReferencedNameElementType() == JetTokens.FIELD_IDENTIFIER
                && referencedName != null) {
            PropertyDescriptor property = context.scope.getPropertyByFieldReference(referencedName);
            if (property == null) {
                context.trace.report(UNRESOLVED_REFERENCE.on(expression));
            }
            else {
                context.trace.record(REFERENCE_TARGET, expression, property);
                return checkType(property.getOutType(), expression, context);
            }
        }
        else {
            return getSelectorReturnType(NO_RECEIVER, null, expression, context); // TODO : Extensions to this
        }
        return null;
    }

    private JetType lookupNamespaceOrClassObject(JetSimpleNameExpression expression, String referencedName, ExpressionTypingContext context) {
        ClassifierDescriptor classifier = context.scope.getClassifier(referencedName);
        if (classifier != null) {
            JetType classObjectType = classifier.getClassObjectType();
            JetType result = null;
            if (classObjectType != null) {
                if (context.namespacesAllowed || classifier.isClassObjectAValue()) {
                    result = classObjectType;
                }
                else {
                    context.trace.report(NO_CLASS_OBJECT.on(expression, classifier));
                }
                context.trace.record(REFERENCE_TARGET, expression, classifier);
                if (result == null) {
                    return ErrorUtils.createErrorType("No class object in " + expression.getReferencedName());
                }
                return checkType(result, expression, context);
            }
        }
        JetType[] result = new JetType[1];
        TemporaryBindingTrace temporaryTrace = TemporaryBindingTrace.create(context.trace);
        if (furtherNameLookup(expression, referencedName, result, context.replaceBindingTrace(temporaryTrace))) {
            temporaryTrace.commit();
            return checkType(result[0], expression, context);
        }
        // To report NO_CLASS_OBJECT when no namespace found
        if (classifier != null) {
            context.trace.report(NO_CLASS_OBJECT.on(expression, classifier));
            context.trace.record(REFERENCE_TARGET, expression, classifier);
            return ErrorUtils.createErrorType("No class object in " + expression.getReferencedName());
        }
        temporaryTrace.commit();
        return result[0];
    }

    protected boolean furtherNameLookup(@NotNull JetSimpleNameExpression expression, @NotNull String referencedName, @NotNull JetType[] result, ExpressionTypingContext context) {
        if (context.namespacesAllowed) {
            result[0] = lookupNamespaceType(expression, referencedName, context);
            return result[0] != null;
        }
        NamespaceType namespaceType = lookupNamespaceType(expression, referencedName, context);
        if (namespaceType != null) {
            context.trace.report(EXPRESSION_EXPECTED_NAMESPACE_FOUND.on(expression));
            result[0] = ErrorUtils.createErrorType("Type for " + referencedName);
        }
        return false;
    }

    @Nullable
    protected NamespaceType lookupNamespaceType(@NotNull JetSimpleNameExpression expression, @NotNull String referencedName, ExpressionTypingContext context) {
        NamespaceDescriptor namespace = context.scope.getNamespace(referencedName);
        if (namespace == null) {
            return null;
        }
        context.trace.record(REFERENCE_TARGET, expression, namespace);
        return namespace.getNamespaceType();
    }

    @Override
    public JetType visitParenthesizedExpression(JetParenthesizedExpression expression, ExpressionTypingContext context) {
        JetExpression innerExpression = expression.getExpression();
        if (innerExpression == null) {
            return null;
        }
        return checkType(facade.getType(innerExpression, context.replaceScope(context.scope)), expression, context);
    }

    @Override
    public JetType visitConstantExpression(JetConstantExpression expression, ExpressionTypingContext context) {
        ASTNode node = expression.getNode();
        IElementType elementType = node.getElementType();
        String text = node.getText();
        JetStandardLibrary standardLibrary = context.semanticServices.getStandardLibrary();
        CompileTimeConstantResolver compileTimeConstantResolver = context.getCompileTimeConstantResolver();

        CompileTimeConstant<?> value;
        if (elementType == JetNodeTypes.INTEGER_CONSTANT) {
            value = compileTimeConstantResolver.getIntegerValue(text, context.expectedType);
        }
        else if (elementType == JetNodeTypes.FLOAT_CONSTANT) {
            value = compileTimeConstantResolver.getFloatValue(text, context.expectedType);
        }
        else if (elementType == JetNodeTypes.BOOLEAN_CONSTANT) {
            value = compileTimeConstantResolver.getBooleanValue(text, context.expectedType);
        }
        else if (elementType == JetNodeTypes.CHARACTER_CONSTANT) {
            value = compileTimeConstantResolver.getCharValue(text, context.expectedType);
        }
        else if (elementType == JetNodeTypes.RAW_STRING_CONSTANT) {
            value = compileTimeConstantResolver.getRawStringValue(text, context.expectedType);
        }
        else if (elementType == JetNodeTypes.NULL) {
            value = compileTimeConstantResolver.getNullValue(context.expectedType);
        }
        else {
            throw new IllegalArgumentException("Unsupported constant: " + expression);
        }
        if (value instanceof ErrorValue) {
            ErrorValue errorValue = (ErrorValue) value;
            context.trace.report(ERROR_COMPILE_TIME_VALUE.on(node, errorValue.getMessage()));
            return getDefaultType(context.semanticServices, elementType);
        }
        else {
            context.trace.record(BindingContext.COMPILE_TIME_VALUE, expression, value);
            return checkType(value.getType(standardLibrary), expression, context);
        }
    }

    @Override
    public JetType visitBinaryWithTypeRHSExpression(JetBinaryExpressionWithTypeRHS expression, ExpressionTypingContext context) {
        JetTypeReference right = expression.getRight();
        JetType result = null;
        if (right != null) {
            JetType targetType = context.getTypeResolver().resolveType(context.scope, right);

            if (isTypeFlexible(expression.getLeft())) {
                TemporaryBindingTrace temporaryTraceWithExpectedType = TemporaryBindingTrace.create(context.trace);
                boolean success = checkBinaryWithTypeRHS(expression, context, targetType, targetType, temporaryTraceWithExpectedType);
                if (success) {
                    temporaryTraceWithExpectedType.commit();
                }
                else {
                    TemporaryBindingTrace temporaryTraceWithoutExpectedType = TemporaryBindingTrace.create(context.trace);
                    checkBinaryWithTypeRHS(expression, context, targetType, NO_EXPECTED_TYPE, temporaryTraceWithoutExpectedType);
                    temporaryTraceWithoutExpectedType.commit();
                }
            }
            else {
                TemporaryBindingTrace temporaryTraceWithoutExpectedType = TemporaryBindingTrace.create(context.trace);
                checkBinaryWithTypeRHS(expression, context, targetType, NO_EXPECTED_TYPE, temporaryTraceWithoutExpectedType);
                temporaryTraceWithoutExpectedType.commit();
            }

            IElementType operationType = expression.getOperationSign().getReferencedNameElementType();
            result = operationType == JetTokens.AS_SAFE ? TypeUtils.makeNullable(targetType) : targetType;
        }
        else {
            facade.getType(expression.getLeft(), context.replaceExpectedType(NO_EXPECTED_TYPE));
        }
        return checkType(result, expression, context);
    }

    private boolean checkBinaryWithTypeRHS(JetBinaryExpressionWithTypeRHS expression, ExpressionTypingContext context, @NotNull JetType targetType, @NotNull JetType expectedType, TemporaryBindingTrace temporaryTrace) {
        ExpressionTypingContext newContext = context.replaceExpectedType(expectedType).replaceBindingTrace(temporaryTrace);

        JetType actualType = facade.getType(expression.getLeft(), newContext);
        if (actualType == null) return false;

        JetSimpleNameExpression operationSign = expression.getOperationSign();
        IElementType operationType = operationSign.getReferencedNameElementType();
        if (operationType == JetTokens.COLON) {
            if (targetType != NO_EXPECTED_TYPE && !context.semanticServices.getTypeChecker().isSubtypeOf(actualType, targetType)) {
                context.trace.report(TYPE_MISMATCH.on(expression.getLeft(), targetType, actualType));
                return false;
            }
            return true;
        }
        else if (operationType == JetTokens.AS_KEYWORD || operationType == JetTokens.AS_SAFE) {
            checkForCastImpossibility(expression, actualType, targetType, context);
            return true;
        }
        else {
            context.trace.report(UNSUPPORTED.on(operationSign, "binary operation with type RHS"));
            return false;
        }
    }

    private void checkForCastImpossibility(JetBinaryExpressionWithTypeRHS expression, JetType actualType, JetType targetType, ExpressionTypingContext context) {
        if (actualType == null || targetType == NO_EXPECTED_TYPE) return;

        JetTypeChecker typeChecker = context.semanticServices.getTypeChecker();
        if (!typeChecker.isSubtypeOf(targetType, actualType)) {
            if (typeChecker.isSubtypeOf(actualType, targetType)) {
                context.trace.report(USELESS_CAST_STATIC_ASSERT_IS_FINE.on(expression, expression.getOperationSign()));
            }
            else {
                // See JET-58 Make 'as never succeeds' a warning, or even never check for Java (external) types
                context.trace.report(CAST_NEVER_SUCCEEDS.on(expression.getOperationSign()));
            }
        }
        else {
            if (typeChecker.isSubtypeOf(actualType, targetType)) {
                context.trace.report(USELESS_CAST.on(expression, expression.getOperationSign()));
            }
        }
    }

    @Override
    public JetType visitTupleExpression(JetTupleExpression expression, ExpressionTypingContext context) {
        List<JetExpression> entries = expression.getEntries();
        List<JetType> types = new ArrayList<JetType>();
        for (JetExpression entry : entries) {
            types.add(context.getServices().safeGetType(context.scope, entry, NO_EXPECTED_TYPE)); // TODO
        }
        if (context.expectedType != NO_EXPECTED_TYPE && JetStandardClasses.isTupleType(context.expectedType)) {
            List<JetType> enrichedTypes = checkArgumentTypes(types, entries, context.expectedType.getArguments(), context);
            if (enrichedTypes != types) {
                return JetStandardClasses.getTupleType(enrichedTypes);
            }
        }
        // TODO : labels
        return checkType(JetStandardClasses.getTupleType(types), expression, context);
    }

    @NotNull
    private List<JetType> checkArgumentTypes(@NotNull List<JetType> argumentTypes, @NotNull List<JetExpression> arguments, @NotNull List<TypeProjection> expectedArgumentTypes, @NotNull ExpressionTypingContext context) {
        if (arguments.size() == 0 || argumentTypes.size() != arguments.size() || expectedArgumentTypes.size() != arguments.size()) {
            return argumentTypes;
        }
        List<JetType> result = Lists.newArrayListWithCapacity(arguments.size());
        for (int i = 0, argumentTypesSize = argumentTypes.size(); i < argumentTypesSize; i++) {
            result.add(checkType(argumentTypes.get(i), arguments.get(i), context.replaceExpectedType(expectedArgumentTypes.get(i).getType())));
        }
        return result;
    }

    @Override
    public JetType visitThisExpression(JetThisExpression expression, ExpressionTypingContext context) {
        JetType result = null;
        ReceiverDescriptor thisReceiver = null;
        String labelName = expression.getLabelName();
        if (labelName != null) {
            thisReceiver = context.labelResolver.resolveThisLabel(expression, context, thisReceiver, labelName);
        }
        else {
            thisReceiver = context.scope.getImplicitReceiver();

            DeclarationDescriptor declarationDescriptorForUnqualifiedThis = context.scope.getDeclarationDescriptorForUnqualifiedThis();
            if (declarationDescriptorForUnqualifiedThis != null) {
                context.trace.record(REFERENCE_TARGET, expression.getThisReference(), declarationDescriptorForUnqualifiedThis);
            }
        }

        if (thisReceiver != null) {
            if (!thisReceiver.exists()) {
                context.trace.report(NO_THIS.on(expression));
            }
            else {
                JetTypeReference superTypeQualifier = expression.getSuperTypeQualifier();
                if (superTypeQualifier != null) {
                    JetTypeElement superTypeElement = superTypeQualifier.getTypeElement();
                    // Errors are reported by the parser
                    if (superTypeElement instanceof JetUserType) {
                        JetUserType typeElement = (JetUserType) superTypeElement;

                        ClassifierDescriptor classifierCandidate = context.getTypeResolver().resolveClass(context.scope, typeElement);
                        if (classifierCandidate instanceof ClassDescriptor) {
                            ClassDescriptor superclass = (ClassDescriptor) classifierCandidate;

                            JetType thisType = thisReceiver.getType();
                            Collection<? extends JetType> supertypes = thisType.getConstructor().getSupertypes();
                            TypeSubstitutor substitutor = TypeSubstitutor.create(thisType);
                            for (JetType declaredSupertype : supertypes) {
                                if (declaredSupertype.getConstructor().equals(superclass.getTypeConstructor())) {
                                    result = substitutor.safeSubstitute(declaredSupertype, Variance.INVARIANT);
                                    break;
                                }
                            }
                            if (result == null) {
                                context.trace.report(NOT_A_SUPERTYPE.on(superTypeElement));
                            }
                        }
                    }
                }
                else {
                    result = thisReceiver.getType();
                }
                if (result != null) {
                    context.trace.record(BindingContext.EXPRESSION_TYPE, expression.getThisReference(), result);
                }
            }
        }
        return checkType(result, expression, context);
    }

    @Override
    public JetType visitBlockExpression(JetBlockExpression expression, ExpressionTypingContext context) {
        return context.getServices().getBlockReturnedType(context.scope, expression, CoercionStrategy.NO_COERCION, context);
    }

    @Override
    public JetType visitHashQualifiedExpression(JetHashQualifiedExpression expression, ExpressionTypingContext context) {
        context.trace.report(UNSUPPORTED.on(expression, getClass().getCanonicalName()));
        return null;
    }

    @Override
    public JetType visitQualifiedExpression(JetQualifiedExpression expression, ExpressionTypingContext contextWithExpectedType) {
        ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(NO_EXPECTED_TYPE);
        // TODO : functions as values
        JetExpression selectorExpression = expression.getSelectorExpression();
        JetExpression receiverExpression = expression.getReceiverExpression();
        JetType receiverType = facade.getType(receiverExpression,
                                              context
                                                      .replaceExpectedType(NO_EXPECTED_TYPE)
                                                      .replaceExpectedReturnType(NO_EXPECTED_TYPE)
                                                      .replaceNamespacesAllowed(true));
        if (selectorExpression == null) return null;
        if (receiverType == null) receiverType = ErrorUtils.createErrorType("Type for " + expression.getText());

        if (selectorExpression instanceof JetSimpleNameExpression) {
            propagateConstantValues(expression, context, (JetSimpleNameExpression) selectorExpression);
        }

        JetType selectorReturnType = getSelectorReturnType(new ExpressionReceiver(receiverExpression, receiverType), expression.getOperationTokenNode(), selectorExpression, context);

        //TODO move further
        if (expression.getOperationSign() == JetTokens.SAFE_ACCESS) {
            if (selectorReturnType != null && !selectorReturnType.isNullable() && !JetStandardClasses.isUnit(selectorReturnType)) {
                selectorReturnType = TypeUtils.makeNullable(selectorReturnType);
            }
        }

        JetType result;
        if (expression.getOperationSign() == JetTokens.QUEST) {
            if (selectorReturnType != null && !isBoolean(context.semanticServices, selectorReturnType)) {
                // TODO : more comprehensible error message
                context.trace.report(TYPE_MISMATCH.on(selectorExpression, context.semanticServices.getStandardLibrary().getBooleanType(), selectorReturnType));
            }
            result = TypeUtils.makeNullable(receiverType);
        }
        else {
            result = selectorReturnType;
        }
        // TODO : this is suspicious: remove this code?
        if (result != null) {
            context.trace.record(BindingContext.EXPRESSION_TYPE, selectorExpression, result);
        }
        return checkType(result, expression, contextWithExpectedType);
    }

    private void propagateConstantValues(JetQualifiedExpression expression, ExpressionTypingContext context, JetSimpleNameExpression selectorExpression) {
        JetExpression receiverExpression = expression.getReceiverExpression();
        CompileTimeConstant<?> receiverValue = context.trace.getBindingContext().get(BindingContext.COMPILE_TIME_VALUE, receiverExpression);
        CompileTimeConstant<?> wholeExpressionValue = context.trace.getBindingContext().get(BindingContext.COMPILE_TIME_VALUE, expression);
        if (wholeExpressionValue == null && receiverValue != null && !(receiverValue instanceof ErrorValue) && receiverValue.getValue() instanceof Number) {
            Number value = (Number) receiverValue.getValue();
            String referencedName = selectorExpression.getReferencedName();
            if (OperatorConventions.NUMBER_CONVERSIONS.contains(referencedName)) {
                if ("dbl".equals(referencedName)) {
                    context.trace.record(BindingContext.COMPILE_TIME_VALUE, expression, new DoubleValue(value.doubleValue()));
                }
                else if ("flt".equals(referencedName)) {
                    context.trace.record(BindingContext.COMPILE_TIME_VALUE, expression, new FloatValue(value.floatValue()));
                }
                else if ("lng".equals(referencedName)) {
                    context.trace.record(BindingContext.COMPILE_TIME_VALUE, expression, new LongValue(value.longValue()));
                }
                else if ("sht".equals(referencedName)) {
                    context.trace.record(BindingContext.COMPILE_TIME_VALUE, expression, new ShortValue(value.shortValue()));
                }
                else if ("byt".equals(referencedName)) {
                    context.trace.record(BindingContext.COMPILE_TIME_VALUE, expression, new ByteValue(value.byteValue()));
                }
                else if ("int".equals(referencedName)) {
                    context.trace.record(BindingContext.COMPILE_TIME_VALUE, expression, new IntValue(value.intValue()));
                }
            }
        }
    }

    @Nullable
    public JetType getSelectorReturnType(@NotNull ReceiverDescriptor receiver, @Nullable ASTNode callOperationNode, @NotNull JetExpression selectorExpression, @NotNull ExpressionTypingContext context) {
        if (selectorExpression instanceof JetCallExpression) {
            JetCallExpression callExpression = (JetCallExpression) selectorExpression;
            return context.resolveCall(receiver, callOperationNode, callExpression);
        }
        else if (selectorExpression instanceof JetSimpleNameExpression) {
            JetSimpleNameExpression nameExpression = (JetSimpleNameExpression) selectorExpression;

            TemporaryBindingTrace temporaryTrace = TemporaryBindingTrace.create(context.trace);
            VariableDescriptor variableDescriptor = context.replaceBindingTrace(temporaryTrace).resolveSimpleProperty(receiver, callOperationNode, nameExpression);
            if (variableDescriptor != null) {
                temporaryTrace.commit();
                return checkType(variableDescriptor.getOutType(), nameExpression, context);
            }
            ExpressionTypingContext newContext = receiver.exists() ? context.replaceScope(receiver.getType().getMemberScope()) : context;
            JetType jetType = lookupNamespaceOrClassObject(nameExpression, nameExpression.getReferencedName(), newContext);
            if (jetType == null) {
                context.trace.report(UNRESOLVED_REFERENCE.on(nameExpression));
            }
            return checkType(jetType, nameExpression, context);
        }
        else if (selectorExpression instanceof JetQualifiedExpression) {
            JetQualifiedExpression qualifiedExpression = (JetQualifiedExpression) selectorExpression;
            JetExpression newReceiverExpression = qualifiedExpression.getReceiverExpression();
            JetType newReceiverType = getSelectorReturnType(receiver, callOperationNode, newReceiverExpression, context.replaceExpectedType(NO_EXPECTED_TYPE));
            JetExpression newSelectorExpression = qualifiedExpression.getSelectorExpression();
            if (newReceiverType != null && newSelectorExpression != null) {
                return getSelectorReturnType(new ExpressionReceiver(newReceiverExpression, newReceiverType), qualifiedExpression.getOperationTokenNode(), newSelectorExpression, context);
            }
        }
        else {
            // TODO : not a simple name -> resolve in scope, expect property type or a function type
            context.trace.report(UNSUPPORTED.on(selectorExpression, "getSelectorReturnType"));
        }
        return null;
    }

    @Override
    public JetType visitCallExpression(JetCallExpression expression, ExpressionTypingContext context) {
        JetType expressionType = context.resolveCall(NO_RECEIVER, null, expression);
        return checkType(expressionType, expression, context);
    }

    @Override
    public JetType visitUnaryExpression(JetUnaryExpression expression, ExpressionTypingContext context) {
        JetExpression baseExpression = expression.getBaseExpression();
        if (baseExpression == null) return null;
        JetSimpleNameExpression operationSign = expression.getOperationSign();
        if (JetTokens.LABELS.contains(operationSign.getReferencedNameElementType())) {
            String referencedName = operationSign.getReferencedName();
            referencedName = referencedName == null ? " <?>" : referencedName;
            context.labelResolver.enterLabeledElement(referencedName.substring(1), baseExpression);
            // TODO : Some processing for the label?
            JetType type = checkType(facade.getType(baseExpression, context.replaceExpectedReturnType(context.expectedType)), expression, context);
            context.labelResolver.exitLabeledElement(baseExpression);
            return type;
        }
        IElementType operationType = operationSign.getReferencedNameElementType();
        String name = OperatorConventions.UNARY_OPERATION_NAMES.get(operationType);
        if (name == null) {
            context.trace.report(UNSUPPORTED.on(operationSign, "visitUnaryExpression"));
            return null;
        }
        ExpressionReceiver receiver = getExpressionReceiver(facade, baseExpression, context.replaceExpectedType(NO_EXPECTED_TYPE).replaceScope(context.scope));
        if (receiver == null) return null;

        FunctionDescriptor functionDescriptor = context.resolveCallWithGivenName(
                CallMaker.makeCall(receiver, expression),
                expression.getOperationSign(),
                name,
                receiver);

        if (functionDescriptor == null) return null;
        JetType returnType = functionDescriptor.getReturnType();
        JetType result;
        if (operationType == JetTokens.PLUSPLUS || operationType == JetTokens.MINUSMINUS) {
            if (context.semanticServices.getTypeChecker().isSubtypeOf(returnType, JetStandardClasses.getUnitType())) {
                result = JetStandardClasses.getUnitType();
            }
            else {
                JetType receiverType = receiver.getType();
                if (!context.semanticServices.getTypeChecker().isSubtypeOf(returnType, receiverType)) {
                    context.trace.report(RESULT_TYPE_MISMATCH.on(operationSign, name, receiverType, returnType));
                }
                else {
                    context.trace.record(BindingContext.VARIABLE_REASSIGNMENT, expression);
                }
                // TODO : Maybe returnType?
                result = receiverType;
            }
        }
        else {
            result = returnType;
        }

        return checkType(result, expression, context);
    }

    @Override
    public JetType visitBinaryExpression(JetBinaryExpression expression, ExpressionTypingContext contextWithExpectedType) {
        ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(NO_EXPECTED_TYPE);
        JetSimpleNameExpression operationSign = expression.getOperationReference();

        JetExpression left = expression.getLeft();
        JetExpression right = expression.getRight();

        JetType result = null;
        IElementType operationType = operationSign.getReferencedNameElementType();
        if (operationType == JetTokens.IDENTIFIER) {
            String referencedName = operationSign.getReferencedName();
            if (referencedName != null) {
                result = getTypeForBinaryCall(context.scope, referencedName, context, expression);
            }
        }
        else if (OperatorConventions.BINARY_OPERATION_NAMES.containsKey(operationType)) {
            result = getTypeForBinaryCall(context.scope, OperatorConventions.BINARY_OPERATION_NAMES.get(operationType), context, expression);
        }
        else if (operationType == JetTokens.EQ) {
            result = visitAssignment(expression, contextWithExpectedType);
        }
        else if (OperatorConventions.ASSIGNMENT_OPERATIONS.containsKey(operationType)) {
            result = visitAssignmentOperation(expression, contextWithExpectedType);
        }
        else if (OperatorConventions.COMPARISON_OPERATIONS.contains(operationType)) {
            JetType compareToReturnType = getTypeForBinaryCall(context.scope, "compareTo", context, expression);
            if (compareToReturnType != null) {
                TypeConstructor constructor = compareToReturnType.getConstructor();
                JetStandardLibrary standardLibrary = context.semanticServices.getStandardLibrary();
                TypeConstructor intTypeConstructor = standardLibrary.getInt().getTypeConstructor();
                if (constructor.equals(intTypeConstructor)) {
                    result = standardLibrary.getBooleanType();
                } else {
                    context.trace.report(COMPARE_TO_TYPE_MISMATCH.on(operationSign, compareToReturnType));
                }
            }
        }
        else if (OperatorConventions.EQUALS_OPERATIONS.contains(operationType)) {
            String name = "equals";
            if (right != null) {
                ExpressionReceiver receiver = ExpressionTypingUtils.safeGetExpressionReceiver(facade, left, context.replaceScope(context.scope));
                OverloadResolutionResults<FunctionDescriptor> resolutionResults = context.resolveExactSignature(
                        receiver, "equals",
                        Collections.singletonList(JetStandardClasses.getNullableAnyType()));
                if (resolutionResults.isSuccess()) {
                    FunctionDescriptor equals = resolutionResults.getResult().getResultingDescriptor();
                    context.trace.record(REFERENCE_TARGET, operationSign, equals);
                    if (ensureBooleanResult(operationSign, name, equals.getReturnType(), context)) {
                        ensureNonemptyIntersectionOfOperandTypes(expression, context);
                    }
                }
                else {
                    if (resolutionResults.isAmbiguity()) {
                        context.trace.report(OVERLOAD_RESOLUTION_AMBIGUITY.on(operationSign, resolutionResults.getResults()));
                    }
                    else {
                        context.trace.report(EQUALS_MISSING.on(operationSign));
                    }
                }
            }
            result = context.semanticServices.getStandardLibrary().getBooleanType();
        }
        else if (operationType == JetTokens.EQEQEQ || operationType == JetTokens.EXCLEQEQEQ) {
            ensureNonemptyIntersectionOfOperandTypes(expression, context);

            // TODO : Check comparison pointlessness
            result = context.semanticServices.getStandardLibrary().getBooleanType();
        }
        else if (OperatorConventions.IN_OPERATIONS.contains(operationType)) {
            if (right == null) {
                result = ErrorUtils.createErrorType("No right argument"); // TODO
                return null;
            }
            checkInExpression(expression, expression.getOperationReference(), expression.getLeft(), expression.getRight(), context);
            result = context.semanticServices.getStandardLibrary().getBooleanType();
        }
        else if (operationType == JetTokens.ANDAND || operationType == JetTokens.OROR) {
            JetType leftType = facade.getType(left, context.replaceScope(context.scope));
            WritableScopeImpl leftScope = newWritableScopeImpl(context).setDebugName("Left scope of && or ||");
            DataFlowInfo flowInfoLeft = extractDataFlowInfoFromCondition(left, operationType == JetTokens.ANDAND, leftScope, context);  // TODO: This gets computed twice: here and in extractDataFlowInfoFromCondition() for the whole condition
            WritableScopeImpl rightScope = operationType == JetTokens.ANDAND ? leftScope : newWritableScopeImpl(context).setDebugName("Right scope of && or ||");
            JetType rightType = right == null ? null : facade.getType(right, context.replaceDataFlowInfo(flowInfoLeft).replaceScope(rightScope));
            if (leftType != null && !isBoolean(context.semanticServices, leftType)) {
                context.trace.report(TYPE_MISMATCH.on(left, context.semanticServices.getStandardLibrary().getBooleanType(), leftType));
            }
            if (rightType != null && !isBoolean(context.semanticServices, rightType)) {
                context.trace.report(TYPE_MISMATCH.on(right, context.semanticServices.getStandardLibrary().getBooleanType(), rightType));
            }
            result = context.semanticServices.getStandardLibrary().getBooleanType();
        }
        else if (operationType == JetTokens.ELVIS) {
            JetType leftType = facade.getType(left, context.replaceScope(context.scope));
            JetType rightType = right == null ? null : facade.getType(right, contextWithExpectedType.replaceScope(context.scope));
            if (leftType != null) {
                if (!leftType.isNullable()) {
                    context.trace.report(USELESS_ELVIS.on(expression, left, leftType));
                }
                if (rightType != null) {
                    checkType(TypeUtils.makeNullableAsSpecified(leftType, rightType.isNullable()), left, contextWithExpectedType);
                    return TypeUtils.makeNullableAsSpecified(context.semanticServices.getTypeChecker().commonSupertype(leftType, rightType), rightType.isNullable());
                }
            }
        }
        else {
            context.trace.report(UNSUPPORTED.on(operationSign, "Unknown operation"));
        }
        return checkType(result, expression, contextWithExpectedType);
    }

    public void checkInExpression(JetElement callElement, @NotNull JetSimpleNameExpression operationSign, @NotNull JetExpression left, @NotNull JetExpression right, ExpressionTypingContext context) {
        String name = "contains";
        ExpressionReceiver receiver = safeGetExpressionReceiver(facade, right, context.replaceExpectedType(NO_EXPECTED_TYPE));
        FunctionDescriptor functionDescriptor = context.resolveCallWithGivenName(
                CallMaker.makeCallWithExpressions(callElement, receiver, null, operationSign, Collections.singletonList(left)),
                operationSign,
                name, receiver);
        JetType containsType = functionDescriptor != null ? functionDescriptor.getReturnType() : null;
        ensureBooleanResult(operationSign, name, containsType, context);
    }

    private void ensureNonemptyIntersectionOfOperandTypes(JetBinaryExpression expression, ExpressionTypingContext context) {
        JetSimpleNameExpression operationSign = expression.getOperationReference();
        JetExpression left = expression.getLeft();
        JetExpression right = expression.getRight();

        // TODO : duplicated effort for == and !=
        JetType leftType = facade.getType(left, context.replaceScope(context.scope));
        if (leftType != null && right != null) {
            JetType rightType = facade.getType(right, context.replaceScope(context.scope));

            if (rightType != null) {
                JetType intersect = TypeUtils.intersect(context.semanticServices.getTypeChecker(), new HashSet<JetType>(Arrays.asList(leftType, rightType)));
                if (intersect == null) {
                    context.trace.report(EQUALITY_NOT_APPLICABLE.on(expression, operationSign, leftType, rightType));
                }
            }
        }
    }

    protected JetType visitAssignmentOperation(JetBinaryExpression expression, ExpressionTypingContext context) {
        return assignmentIsNotAnExpressionError(expression, context);
    }

    protected JetType visitAssignment(JetBinaryExpression expression, ExpressionTypingContext context) {
        return assignmentIsNotAnExpressionError(expression, context);
    }

    private JetType assignmentIsNotAnExpressionError(JetBinaryExpression expression, ExpressionTypingContext context) {
        context.trace.report(ASSIGNMENT_IN_EXPRESSION_CONTEXT.on(expression));
        return null;
    }

    @Override
    public JetType visitArrayAccessExpression(JetArrayAccessExpression expression, ExpressionTypingContext contextWithExpectedType) {
        ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(NO_EXPECTED_TYPE);
        JetExpression arrayExpression = expression.getArrayExpression();
        ExpressionReceiver receiver = getExpressionReceiver(facade, arrayExpression, context.replaceScope(context.scope));

        if (receiver != null) {
            FunctionDescriptor functionDescriptor = context.resolveCallWithGivenName(
                    CallMaker.makeCallWithExpressions(expression, receiver, null, expression, expression.getIndexExpressions()),
                    expression,
                    "get",
                    receiver);
            if (functionDescriptor != null) {
                return checkType(functionDescriptor.getReturnType(), expression, contextWithExpectedType);
            }
        }
        return null;
    }

    @Nullable
    protected JetType getTypeForBinaryCall(JetScope scope, String name, ExpressionTypingContext context, JetBinaryExpression binaryExpression) {
        ExpressionReceiver receiver = safeGetExpressionReceiver(facade, binaryExpression.getLeft(), context.replaceScope(scope));
        FunctionDescriptor functionDescriptor = context.replaceScope(scope).resolveCallWithGivenName(
                CallMaker.makeCall(receiver, binaryExpression),
                binaryExpression.getOperationReference(),
                name,
                receiver);
        if (functionDescriptor != null) {
            return functionDescriptor.getReturnType();
        }
        return null;
    }

    @Override
    public JetType visitDeclaration(JetDeclaration dcl, ExpressionTypingContext context) {
        context.trace.report(DECLARATION_IN_ILLEGAL_CONTEXT.on(dcl));
        return null;
    }

    @Override
    public JetType visitRootNamespaceExpression(JetRootNamespaceExpression expression, ExpressionTypingContext context) {
        if (context.namespacesAllowed) {
            return ExpressionTypingUtils.checkType(JetModuleUtil.getRootNamespaceType(expression), expression, context);
        }
        context.trace.report(NAMESPACE_IS_NOT_AN_EXPRESSION.on(expression));
        return null;
    }


    @Override
    public JetType visitStringTemplateExpression(JetStringTemplateExpression expression, ExpressionTypingContext contextWithExpectedType) {
        final ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(NO_EXPECTED_TYPE);
        final StringBuilder builder = new StringBuilder();
        final CompileTimeConstant<?>[] value = new CompileTimeConstant<?>[1];
        for (JetStringTemplateEntry entry : expression.getEntries()) {
            entry.accept(new JetVisitorVoid() {

                @Override
                public void visitStringTemplateEntryWithExpression(JetStringTemplateEntryWithExpression entry) {
                    JetExpression entryExpression = entry.getExpression();
                    if (entryExpression != null) {
                        facade.getType(entryExpression, context.replaceScope(context.scope));
                    }
                    value[0] = CompileTimeConstantResolver.OUT_OF_RANGE;
                }

                @Override
                public void visitLiteralStringTemplateEntry(JetLiteralStringTemplateEntry entry) {
                    builder.append(entry.getText());
                }

                @Override
                public void visitEscapeStringTemplateEntry(JetEscapeStringTemplateEntry entry) {
                    // TODO : Check escape
                    String text = entry.getText();
                    assert text.length() == 2 && text.charAt(0) == '\\';
                    char escaped = text.charAt(1);

                    Character character = CompileTimeConstantResolver.translateEscape(escaped);
                    if (character == null) {
                        context.trace.report(ILLEGAL_ESCAPE_SEQUENCE.on(entry));
                        value[0] = CompileTimeConstantResolver.OUT_OF_RANGE;
                    }
                    else {
                        builder.append(character);
                    }
                }
            });
        }
        if (value[0] != CompileTimeConstantResolver.OUT_OF_RANGE) {
            context.trace.record(BindingContext.COMPILE_TIME_VALUE, expression, new StringValue(builder.toString()));
        }
        return checkType(context.semanticServices.getStandardLibrary().getStringType(), expression, contextWithExpectedType);
    }

    @Override
    public JetType visitAnnotatedExpression(JetAnnotatedExpression expression, ExpressionTypingContext data) {
        return facade.getType(expression.getBaseExpression(), data);
    }

    @Override
    public JetType visitJetElement(JetElement element, ExpressionTypingContext context) {
        context.trace.report(UNSUPPORTED.on(element, getClass().getCanonicalName()));
        return null;
    }
}
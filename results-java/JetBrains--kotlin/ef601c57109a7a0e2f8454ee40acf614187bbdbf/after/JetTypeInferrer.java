package org.jetbrains.jet.lang.types;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.JetNodeTypes;
import org.jetbrains.jet.lang.ErrorHandlerWithRegions;
import org.jetbrains.jet.lang.JetSemanticServices;
import org.jetbrains.jet.lang.cfg.JetFlowInformationProvider;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.*;
import org.jetbrains.jet.lexer.JetTokens;
import org.jetbrains.jet.resolve.DescriptorRenderer;

import java.util.*;

/**
 * @author abreslav
 */
public class JetTypeInferrer {

    private static final JetType FORBIDDEN = new JetType() {
        @NotNull
        @Override
        public TypeConstructor getConstructor() {
            throw new UnsupportedOperationException(); // TODO
        }

        @NotNull
        @Override
        public List<TypeProjection> getArguments() {
            throw new UnsupportedOperationException(); // TODO
        }

        @Override
        public boolean isNullable() {
            throw new UnsupportedOperationException(); // TODO
        }

        @NotNull
        @Override
        public JetScope getMemberScope() {
            throw new UnsupportedOperationException(); // TODO
        }

        @Override
        public List<Annotation> getAnnotations() {
            throw new UnsupportedOperationException(); // TODO
        }
    };
    public static final JetType NO_EXPECTED_TYPE = new JetType() {
        @NotNull
        @Override
        public TypeConstructor getConstructor() {
            throw new UnsupportedOperationException(); // TODO
        }

        @NotNull
        @Override
        public List<TypeProjection> getArguments() {
            throw new UnsupportedOperationException(); // TODO
        }

        @Override
        public boolean isNullable() {
            throw new UnsupportedOperationException(); // TODO
        }

        @NotNull
        @Override
        public JetScope getMemberScope() {
            throw new UnsupportedOperationException(); // TODO
        }

        @Override
        public List<Annotation> getAnnotations() {
            throw new UnsupportedOperationException(); // TODO
        }
    };

    private static final ImmutableMap<IElementType, String> unaryOperationNames = ImmutableMap.<IElementType, String>builder()
            .put(JetTokens.PLUSPLUS, "inc")
            .put(JetTokens.MINUSMINUS, "dec")
            .put(JetTokens.PLUS, "plus")
            .put(JetTokens.MINUS, "minus")
            .put(JetTokens.EXCL, "not")
            .build();
    private static final ImmutableMap<IElementType, String> binaryOperationNames = ImmutableMap.<IElementType, String>builder()
            .put(JetTokens.MUL, "times")
            .put(JetTokens.PLUS, "plus")
            .put(JetTokens.MINUS, "minus")
            .put(JetTokens.DIV, "div")
            .put(JetTokens.PERC, "mod")
            .put(JetTokens.ARROW, "arrow")
            .put(JetTokens.RANGE, "rangeTo")
            .build();

    private static final Set<IElementType> comparisonOperations = Sets.<IElementType>newHashSet(JetTokens.LT, JetTokens.GT, JetTokens.LTEQ, JetTokens.GTEQ);
    private static final Set<IElementType> equalsOperations = Sets.<IElementType>newHashSet(JetTokens.EQEQ, JetTokens.EXCLEQ);

    private static final Set<IElementType> inOperations = Sets.<IElementType>newHashSet(JetTokens.IN_KEYWORD, JetTokens.NOT_IN);
    public static final ImmutableMap<IElementType, String> assignmentOperationNames = ImmutableMap.<IElementType, String>builder()
            .put(JetTokens.MULTEQ, "timesAssign")
            .put(JetTokens.DIVEQ, "divAssign")
            .put(JetTokens.PERCEQ, "modAssign")
            .put(JetTokens.PLUSEQ, "plusAssign")
            .put(JetTokens.MINUSEQ, "minusAssign")
            .build();

    private static final ImmutableMap<IElementType, IElementType> assignmentOperationCounterparts = ImmutableMap.<IElementType, IElementType>builder()
            .put(JetTokens.MULTEQ, JetTokens.MUL)
            .put(JetTokens.DIVEQ, JetTokens.DIV)
            .put(JetTokens.PERCEQ, JetTokens.PERC)
            .put(JetTokens.PLUSEQ, JetTokens.PLUS)
            .put(JetTokens.MINUSEQ, JetTokens.MINUS)
            .build();

    private final JetSemanticServices semanticServices;
    private final JetFlowInformationProvider flowInformationProvider;
    private final Map<JetPattern, DataFlowInfo> patternsToDataFlowInfo = Maps.newHashMap();
    private final Map<JetPattern, List<VariableDescriptor>> patternsToBoundVariableLists = Maps.newHashMap();

    public JetTypeInferrer(@NotNull JetFlowInformationProvider flowInformationProvider, @NotNull JetSemanticServices semanticServices) {
        this.semanticServices = semanticServices;
        this.flowInformationProvider = flowInformationProvider;
    }

    public Services getServices(@NotNull BindingTrace trace) {
        return new Services(trace);
    }

    public class Services {
        private final BindingTrace trace;
        private final ClassDescriptorResolver classDescriptorResolver;
        private final TypeResolver typeResolver;

        private Services(BindingTrace trace) {
            this.trace = trace;
            this.typeResolver = new TypeResolver(semanticServices, trace, true);
            this.classDescriptorResolver = semanticServices.getClassDescriptorResolver(trace);
        }

        @NotNull
        public JetType safeGetType(@NotNull final JetScope scope, @NotNull JetExpression expression, boolean preferBlock, @NotNull JetType expectedType) {
            JetType type = getType(scope, expression, preferBlock, expectedType);
            if (type != null) {
                return type;
            }
            return ErrorUtils.createErrorType("Type for " + expression.getText());
        }

        @Nullable
        public JetType getType(@NotNull final JetScope scope, @NotNull JetExpression expression, boolean preferBlock, @NotNull JetType expectedType) {
            return new TypeInferrerVisitor(new TypeInferenceContext(trace, scope, preferBlock, DataFlowInfo.getEmpty(), expectedType, FORBIDDEN)).getType(expression);
        }

        public JetType getTypeWithNamespaces(@NotNull final JetScope scope, @NotNull JetExpression expression, boolean preferBlock) {
            return new TypeInferrerVisitorWithNamespaces(new TypeInferenceContext(trace, scope, preferBlock, DataFlowInfo.getEmpty(), NO_EXPECTED_TYPE, null)).getType(expression);
        }

        @Nullable
        private FunctionDescriptor lookupFunction(
                @NotNull JetScope scope,
                @NotNull JetReferenceExpression reference,
                @NotNull String name,
                @NotNull JetType receiverType,
                @NotNull List<JetType> argumentTypes,
                boolean reportUnresolved) {
            OverloadDomain overloadDomain = semanticServices.getOverloadResolver().getOverloadDomain(receiverType, scope, name);
            // No generics. Guaranteed
            overloadDomain = wrapForTracing(overloadDomain, reference, null, reportUnresolved);
            OverloadResolutionResult resolutionResult = overloadDomain.getFunctionDescriptorForPositionedArguments(Collections.<JetType>emptyList(), argumentTypes);
            return resolutionResult.isSuccess() ? resolutionResult.getFunctionDescriptor() : null;
        }

        @NotNull
        private OverloadResolutionResult resolveNoParametersFunction(@NotNull JetType receiverType, @NotNull JetScope scope, @NotNull String name) {
            OverloadDomain overloadDomain = semanticServices.getOverloadResolver().getOverloadDomain(receiverType, scope, name);
            // No generics. Guaranteed
            return overloadDomain.getFunctionDescriptorForPositionedArguments(Collections.<JetType>emptyList(), Collections.<JetType>emptyList());
        }

        private OverloadDomain getOverloadDomain(
                @Nullable final JetType receiverType,
                @NotNull final JetScope scope,
                @NotNull JetExpression calleeExpression,
                @Nullable PsiElement argumentList
        ) {
            final OverloadDomain[] result = new OverloadDomain[1];
            final JetSimpleNameExpression[] reference = new JetSimpleNameExpression[1];
            calleeExpression.accept(new JetVisitor() {

                @Override
                public void visitHashQualifiedExpression(JetHashQualifiedExpression expression) {
                    // a#b -- create a domain for all overloads of b in a
                    throw new UnsupportedOperationException(); // TODO
                }

                @Override
                public void visitPredicateExpression(JetPredicateExpression expression) {
                    // overload lookup for checking, but the type is receiver's type + nullable
                    throw new UnsupportedOperationException(); // TODO
                }

                @Override
                public void visitQualifiedExpression(JetQualifiedExpression expression) {
                    trace.getErrorHandler().genericError(expression.getNode(), "Unsupported [JetTypeInferrer]");

                    // . or ?.
    //                JetType receiverType = getType(scope, expression.getReceiverExpression(), false);
    //                checkNullSafety(receiverType, expression.getOperationTokenNode());
    //
    //                JetExpression selectorExpression = expression.getSelectorExpression();
    //                if (selectorExpression instanceof JetSimpleNameExpression) {
    //                    JetSimpleNameExpression referenceExpression = (JetSimpleNameExpression) selectorExpression;
    //                    String referencedName = referenceExpression.getReferencedName();
    //
    //                    if (receiverType != null && referencedName != null) {
    //                        // No generics. Guaranteed
    //                        result[0] = semanticServices.getOverloadResolver().getOverloadDomain(receiverType, scope, referencedName);
    //                        reference[0] = referenceExpression;
    //                    }
    //                } else {
    //                    throw new UnsupportedOperationException(); // TODO
    //                }
                }

                @Override
                public void visitSimpleNameExpression(JetSimpleNameExpression expression) {
                    // a -- create a hierarchical lookup domain for this.a
                    String referencedName = expression.getReferencedName();
                    if (referencedName != null) {
                        // No generics. Guaranteed
                        result[0] = semanticServices.getOverloadResolver().getOverloadDomain(receiverType, scope, referencedName);
                        reference[0] = expression;
                    }
                }

                @Override
                public void visitExpression(JetExpression expression) {
                    // <e> create a dummy domain for the type of e
                    throw new UnsupportedOperationException(expression.getText()); // TODO
                }

                @Override
                public void visitJetElement(JetElement element) {
                    trace.getErrorHandler().genericError(element.getNode(), "Unsupported in call element"); // TODO : Message
                }
            });
            return wrapForTracing(result[0], reference[0], argumentList, true);
        }

        private void checkNullSafety(@Nullable JetType receiverType, @NotNull ASTNode operationTokenNode, @Nullable FunctionDescriptor callee) {
            if (receiverType != null && callee != null) {
                boolean namespaceType = receiverType instanceof NamespaceType;
                JetType calleeReceiverType = callee.getReceiverType();
                boolean nullableReceiver = !namespaceType && receiverType.isNullable();
                boolean calleeForbidsNullableReceiver = calleeReceiverType == null || !calleeReceiverType.isNullable();

                IElementType operationSign = operationTokenNode.getElementType();
                if (nullableReceiver && calleeForbidsNullableReceiver && operationSign == JetTokens.DOT) {
                    trace.getErrorHandler().genericError(operationTokenNode, "Only safe calls (?.) are allowed on a nullable receiver of type " + receiverType);
                }
                else if ((!nullableReceiver || !calleeForbidsNullableReceiver) && operationSign == JetTokens.SAFE_ACCESS) {
                    if (namespaceType) {
                        trace.getErrorHandler().genericError(operationTokenNode, "Safe calls are not allowed on namespaces");
                    }
                    else {
                        trace.getErrorHandler().genericWarning(operationTokenNode, "Unnecessary safe call on a non-null receiver of type  " + receiverType);
                    }
                }
            }
        }

        private OverloadDomain wrapForTracing(
                @NotNull final OverloadDomain overloadDomain,
                @NotNull final JetReferenceExpression referenceExpression,
                @Nullable final PsiElement argumentList,
                final boolean reportErrors) {
            return new OverloadDomain() {
                @NotNull
                @Override
                public OverloadResolutionResult getFunctionDescriptorForNamedArguments(@NotNull List<JetType> typeArguments, @NotNull Map<String, JetType> valueArgumentTypes, @Nullable JetType functionLiteralArgumentType) {
                    OverloadResolutionResult resolutionResult = overloadDomain.getFunctionDescriptorForNamedArguments(typeArguments, valueArgumentTypes, functionLiteralArgumentType);
                    report(resolutionResult);
                    return resolutionResult;
                }

                @NotNull
                @Override
                public OverloadResolutionResult getFunctionDescriptorForPositionedArguments(@NotNull List<JetType> typeArguments, @NotNull List<JetType> positionedValueArgumentTypes) {
                    OverloadResolutionResult resolutionResult = overloadDomain.getFunctionDescriptorForPositionedArguments(typeArguments, positionedValueArgumentTypes);
                    report(resolutionResult);
                    return resolutionResult;
                }

                private void report(OverloadResolutionResult resolutionResult) {
                    if (resolutionResult.isSuccess() || resolutionResult.singleFunction()) {
                        trace.recordReferenceResolution(referenceExpression, resolutionResult.getFunctionDescriptor());
                    }
                    if (reportErrors) {
                        switch (resolutionResult.getResultCode()) {
                            case NAME_NOT_FOUND:
                                trace.getErrorHandler().unresolvedReference(referenceExpression);
                                break;
                            case SINGLE_FUNCTION_ARGUMENT_MISMATCH:
                                if (argumentList != null) {
                                    // TODO : More helpful message. NOTE: there's a separate handling for this for constructors
                                    trace.getErrorHandler().genericError(argumentList.getNode(), "Arguments do not match " + DescriptorRenderer.TEXT.render(resolutionResult.getFunctionDescriptor()));
                                }
                                else {
                                    trace.getErrorHandler().unresolvedReference(referenceExpression);
                                }
                                break;
                            case AMBIGUITY:
                                if (argumentList != null) {
                                    // TODO : More helpful message. NOTE: there's a separate handling for this for constructors
                                    trace.getErrorHandler().genericError(argumentList.getNode(), "Overload ambiguity [TODO : more helpful message]");
                                }
                                else {
                                    trace.getErrorHandler().unresolvedReference(referenceExpression);
                                }
                                break;
                            default:
                                // Not a success
                        }
                    }
                }

                @Override
                public boolean isEmpty() {
                    return overloadDomain.isEmpty();
                }
            };
        }

        public void checkFunctionReturnType(@NotNull JetScope outerScope, @NotNull JetDeclarationWithBody function, @NotNull FunctionDescriptor functionDescriptor) {
            final JetType expectedReturnType = functionDescriptor.getUnsubstitutedReturnType();
            JetScope functionInnerScope = FunctionDescriptorUtil.getFunctionInnerScope(outerScope, functionDescriptor, trace);
            checkFunctionReturnType(functionInnerScope, function, functionDescriptor, expectedReturnType);
    //        Map<JetElement, JetType> typeMap = collectReturnedExpressionsWithTypes(outerScope, function, functionDescriptor, expectedReturnType);
    //        if (typeMap.isEmpty()) {
    //            return; // The function returns Nothing
    //        }
    //        for (Map.Entry<JetElement, JetType> entry : typeMap.entrySet()) {
    //            JetType actualType = entry.getValue();
    //            JetElement element = entry.getKey();
    //            JetTypeChecker typeChecker = semanticServices.getTypeChecker();
    //            if (!typeChecker.isSubtypeOf(actualType, expectedReturnType)) {
    //                if (typeChecker.isConvertibleBySpecialConversion(actualType, expectedReturnType)) {
    //                    if (expectedReturnType.getConstructor().equals(JetStandardClasses.getUnitType().getConstructor())
    //                        && element.getParent() instanceof JetReturnExpression) {
    //                        context.trace.getErrorHandler().genericError(element.getNode(), "This function must return a value of type Unit");
    //                    }
    //                }
    //                else {
    //                    if (element == function) {
    //                        JetExpression bodyExpression = function.getBodyExpression();
    //                        assert bodyExpression != null;
    //                        context.trace.getErrorHandler().genericError(bodyExpression.getNode(), "This function must return a value of type " + expectedReturnType);
    //                    }
    //                    else if (element instanceof JetExpression) {
    //                        JetExpression expression = (JetExpression) element;
    //                        context.trace.getErrorHandler().typeMismatch(expression, expectedReturnType, actualType);
    //                    }
    //                    else {
    //                        context.trace.getErrorHandler().genericError(element.getNode(), "This function must return a value of type " + expectedReturnType);
    //                    }
    //                }
    //            }
    //        }
        }

        public void checkFunctionReturnType(JetScope functionInnerScope, JetDeclarationWithBody function, FunctionDescriptor functionDescriptor, @Nullable final JetType expectedReturnType) {
            JetExpression bodyExpression = function.getBodyExpression();
            assert bodyExpression != null;
            new TypeInferrerVisitor(new TypeInferenceContext(trace, functionInnerScope, function.hasBlockBody(), DataFlowInfo.getEmpty(), NO_EXPECTED_TYPE, expectedReturnType)).getType(bodyExpression);

            List<JetElement> unreachableElements = Lists.newArrayList();
            flowInformationProvider.collectUnreachableExpressions(function.asElement(), unreachableElements);

            // This is needed in order to highlight only '1 < 2' and not '1', '<' and '2' as well
            final Set<JetElement> rootUnreachableElements = JetPsiUtil.findRootExpressions(unreachableElements);

            // TODO : (return 1) || (return 2) -- only || and right of it is unreachable
            // TODO : try {return 1} finally {return 2}. Currently 'return 1' is reported as unreachable,
            //        though it'd better be reported more specifically

            for (JetElement element : rootUnreachableElements) {
                trace.getErrorHandler().genericError(element.getNode(), "Unreachable code");
            }

            final boolean blockBody = function.hasBlockBody();
            List<JetExpression> returnedExpressions = Lists.newArrayList();
            flowInformationProvider.collectReturnExpressions(function.asElement(), returnedExpressions);

            boolean nothingReturned = returnedExpressions.isEmpty();

            returnedExpressions.remove(function); // This will be the only "expression" if the body is empty

            if (!JetStandardClasses.isUnit(expectedReturnType) && returnedExpressions.isEmpty() && !nothingReturned) {
                trace.getErrorHandler().genericError(bodyExpression.getNode(), "This function must return a value of type " + expectedReturnType);
            }

            for (JetExpression returnedExpression : returnedExpressions) {
                returnedExpression.accept(new JetVisitor() {
                    @Override
                    public void visitReturnExpression(JetReturnExpression expression) {
                        if (!blockBody) {
                            trace.getErrorHandler().genericError(expression.getNode(), "Returns are not allowed for functions with expression body. Use block body in '{...}'");
                        }
                    }

                    @Override
                    public void visitExpression(JetExpression expression) {
                        if (blockBody && !JetStandardClasses.isUnit(expectedReturnType) && !rootUnreachableElements.contains(expression)) {
                            trace.getErrorHandler().genericError(expression.getNode(), "A 'return' expression required in a function with a block body ('{...}')");
                        }
                    }
                });
            }
        }

        @NotNull
        public JetType inferFunctionReturnType(@NotNull JetScope outerScope, JetDeclarationWithBody function, FunctionDescriptor functionDescriptor) {
            Map<JetElement, JetType> typeMap = collectReturnedExpressionsWithTypes(trace, outerScope, function, functionDescriptor);
            Collection<JetType> types = typeMap.values();
            return types.isEmpty()
                    ? JetStandardClasses.getNothingType()
                    : semanticServices.getTypeChecker().commonSupertype(types);
        }

        private Map<JetElement, JetType> collectReturnedExpressionsWithTypes(
                @NotNull BindingTrace trace,
                JetScope outerScope,
                JetDeclarationWithBody function,
                FunctionDescriptor functionDescriptor) {
            JetExpression bodyExpression = function.getBodyExpression();
            assert bodyExpression != null;
            JetScope functionInnerScope = FunctionDescriptorUtil.getFunctionInnerScope(outerScope, functionDescriptor, trace);
            new TypeInferrerVisitor(new TypeInferenceContext(trace, functionInnerScope, function.hasBlockBody(), DataFlowInfo.getEmpty(), NO_EXPECTED_TYPE, FORBIDDEN)).getType(bodyExpression);
            Collection<JetExpression> returnedExpressions = new ArrayList<JetExpression>();
            Collection<JetElement> elementsReturningUnit = new ArrayList<JetElement>();
            flowInformationProvider.collectReturnedInformation(function.asElement(), returnedExpressions, elementsReturningUnit);
            Map<JetElement,JetType> typeMap = new HashMap<JetElement, JetType>();
            for (JetExpression returnedExpression : returnedExpressions) {
                JetType cachedType = trace.getBindingContext().getExpressionType(returnedExpression);
                trace.removeStatementRecord(returnedExpression);
                if (cachedType != null) {
                    typeMap.put(returnedExpression, cachedType);
                }
            }
            for (JetElement jetElement : elementsReturningUnit) {
                typeMap.put(jetElement, JetStandardClasses.getUnitType());
            }
            return typeMap;
        }

        private JetType getBlockReturnedTypeWithWritableScope(@NotNull WritableScope scope, @NotNull List<? extends JetElement> block, DataFlowInfo dataFlowInfo, JetType extectedReturnType) {
            if (block.isEmpty()) {
                return JetStandardClasses.getUnitType();
            }

            TypeInferrerVisitorWithWritableScope blockLevelVisitor = new TypeInferrerVisitorWithWritableScope(scope, new TypeInferenceContext(trace, scope, true, dataFlowInfo, NO_EXPECTED_TYPE, extectedReturnType));

            JetType result = null;
            for (JetElement statement : block) {
                trace.recordStatement(statement);
                JetExpression statementExpression = (JetExpression) statement;
                result = blockLevelVisitor.getType(statementExpression);
                DataFlowInfo newDataFlowInfo = blockLevelVisitor.getResultingDataFlowInfo();
                if (newDataFlowInfo == null) {
                    newDataFlowInfo = dataFlowInfo;
                }
    //            WritableScope newScope = blockLevelVisitor.getResultScope();
    //            if (newScope == null) {
    //                newScope = scope;
    //            }
                if (newDataFlowInfo != dataFlowInfo) {// || newScope != scope) {
                    blockLevelVisitor = new TypeInferrerVisitorWithWritableScope(scope, new TypeInferenceContext(trace, scope, true, newDataFlowInfo, NO_EXPECTED_TYPE, extectedReturnType));
                }
                else {
                    blockLevelVisitor.resetResult(); // TODO : maybe it's better to recreate the visitors with the same scope?
                }
            }
            return result;
        }

        @Nullable
        private JetType resolveCall(
                @NotNull JetScope scope,
                @NotNull OverloadDomain overloadDomain,
                @NotNull JetCall call) {
            // 1) ends with a name -> (scope, name) to look up
            // 2) ends with something else -> just check types

            final List<JetTypeProjection> jetTypeArguments = call.getTypeArguments();

            for (JetTypeProjection typeArgument : jetTypeArguments) {
                if (typeArgument.getProjectionKind() != JetProjectionKind.NONE) {
                    trace.getErrorHandler().genericError(typeArgument.getNode(), "Projections are not allowed on type parameters for methods"); // TODO : better positioning
                }
            }

            List<JetType> typeArguments = new ArrayList<JetType>();
            for (JetTypeProjection projection : jetTypeArguments) {
                // TODO : check that there's no projection
                JetTypeReference typeReference = projection.getTypeReference();
                if (typeReference != null) {
                    typeArguments.add(new TypeResolver(semanticServices, trace, true).resolveType(scope, typeReference));
                }
            }

            return resolveCallWithTypeArguments(scope, overloadDomain, call, typeArguments);
        }

        private JetType resolveCallWithTypeArguments(JetScope scope, OverloadDomain overloadDomain, JetCall call, List<JetType> typeArguments) {
            final List<JetArgument> valueArguments = call.getValueArguments();

            boolean someNamed = false;
            for (JetArgument argument : valueArguments) {
                if (argument.isNamed()) {
                    someNamed = true;
                    break;
                }
            }

            final List<JetExpression> functionLiteralArguments = call.getFunctionLiteralArguments();

            // TODO : must be a check
            assert functionLiteralArguments.size() <= 1;

            if (someNamed) {
                // TODO : check that all are named
                throw new UnsupportedOperationException(); // TODO

    //                    result = overloadDomain.getFunctionDescriptorForNamedArguments(typeArguments, valueArguments, functionLiteralArgument);
            } else {
                List<JetExpression> positionedValueArguments = new ArrayList<JetExpression>();
                for (JetArgument argument : valueArguments) {
                    JetExpression argumentExpression = argument.getArgumentExpression();
                    if (argumentExpression != null) {
                        positionedValueArguments.add(argumentExpression);
                    }
                }

                positionedValueArguments.addAll(functionLiteralArguments);

                List<JetType> valueArgumentTypes = new ArrayList<JetType>();
                for (JetExpression valueArgument : positionedValueArguments) {
                    valueArgumentTypes.add(safeGetType(scope, valueArgument, false, NO_EXPECTED_TYPE)); // TODO
                }

                OverloadResolutionResult resolutionResult = overloadDomain.getFunctionDescriptorForPositionedArguments(typeArguments, valueArgumentTypes);
                if (resolutionResult.isSuccess()) {
                    final FunctionDescriptor functionDescriptor = resolutionResult.getFunctionDescriptor();

                    checkGenericBoundsInAFunctionCall(call.getTypeArguments(), typeArguments, functionDescriptor);
                    return functionDescriptor.getUnsubstitutedReturnType();
                }
            }
            return null;
        }

        private void checkGenericBoundsInAFunctionCall(List<JetTypeProjection> jetTypeArguments, List<JetType> typeArguments, FunctionDescriptor functionDescriptor) {
            Map<TypeConstructor, TypeProjection> context = Maps.newHashMap();

            List<TypeParameterDescriptor> typeParameters = functionDescriptor.getOriginal().getTypeParameters();
            for (int i = 0, typeParametersSize = typeParameters.size(); i < typeParametersSize; i++) {
                TypeParameterDescriptor typeParameter = typeParameters.get(i);
                JetType typeArgument = typeArguments.get(i);
                context.put(typeParameter.getTypeConstructor(), new TypeProjection(typeArgument));
            }
            TypeSubstitutor substitutor = TypeSubstitutor.create(context);
            for (int i = 0, typeParametersSize = typeParameters.size(); i < typeParametersSize; i++) {
                TypeParameterDescriptor typeParameterDescriptor = typeParameters.get(i);
                JetType typeArgument = typeArguments.get(i);
                JetTypeReference typeReference = jetTypeArguments.get(i).getTypeReference();
                assert typeReference != null;
                classDescriptorResolver.checkBounds(typeReference, typeArgument, typeParameterDescriptor, substitutor);
            }
        }

        @Nullable
        public JetType checkTypeInitializerCall(JetScope scope, @NotNull JetTypeReference typeReference, @NotNull JetCall call) {
            JetTypeElement typeElement = typeReference.getTypeElement();
            if (typeElement instanceof JetUserType) {
                JetUserType userType = (JetUserType) typeElement;
                // TODO : to infer constructor parameters, one will need to
                //  1) resolve a _class_ from the typeReference
                //  2) rely on the overload domain of constructors of this class to infer type arguments
                // For now we assume that the type arguments are provided, and thus the typeReference can be
                // resolved into a valid type
                JetType receiverType = typeResolver.resolveType(scope, typeReference);
                DeclarationDescriptor declarationDescriptor = receiverType.getConstructor().getDeclarationDescriptor();
                if (declarationDescriptor instanceof ClassDescriptor) {
                    ClassDescriptor classDescriptor = (ClassDescriptor) declarationDescriptor;

                    for (JetTypeProjection typeProjection : userType.getTypeArguments()) {
                        switch (typeProjection.getProjectionKind()) {
                            case IN:
                            case OUT:
                            case STAR:
                                // TODO : Bug in the editor
                                trace.getErrorHandler().genericError(typeProjection.getProjectionNode(), "Projections are not allowed in constructor type arguments");
                                break;
                            case NONE:
                                break;
                        }
                    }

                    JetSimpleNameExpression referenceExpression = userType.getReferenceExpression();
                    if (referenceExpression != null) {
                        return checkClassConstructorCall(scope, referenceExpression, classDescriptor, receiverType, call);
                    }
                }
                else {
                    trace.getErrorHandler().genericError(((JetElement) call).getNode(), "Calling a constructor is only supported for ordinary classes"); // TODO : review the message
                }
                return null;
            }
            else {
                if (typeElement != null) {
                    trace.getErrorHandler().genericError(typeElement.getNode(), "Calling a constructor is only supported for ordinary classes"); // TODO : Better message
                }
            }
            return null;
        }

        @Nullable
        public JetType checkClassConstructorCall(
                @NotNull JetScope scope,
                @NotNull JetReferenceExpression referenceExpression,
                @NotNull ClassDescriptor classDescriptor,
                @NotNull JetType receiverType,
                @NotNull JetCall call) {
            // When one writes 'new Array<in T>(...)' this does not make much sense, and an instance
            // of 'Array<T>' must be created anyway.
            // Thus, we should either prohibit projections in type arguments in such contexts,
            // or treat them as an automatic upcast to the desired type, i.e. for the user not
            // to be forced to write
            //   val a : Array<in T> = new Array<T>(...)
            // NOTE: Array may be a bad example here, some classes may have substantial functionality
            //       not involving their type parameters
            //
            // The code below upcasts the type automatically


            FunctionGroup constructors = classDescriptor.getConstructors();
            OverloadDomain constructorsOverloadDomain = semanticServices.getOverloadResolver().getOverloadDomain(null, constructors);

            JetType constructorReturnedType;
            if (call instanceof JetDelegatorToThisCall) {
                List<TypeProjection> typeArguments = receiverType.getArguments();

                List<JetType> projectionsStripped = Lists.newArrayList();
                for (TypeProjection typeArgument : typeArguments) {
                    projectionsStripped.add(typeArgument.getType());
                }

                constructorReturnedType = resolveCallWithTypeArguments(
                        scope,
                        wrapForTracing(constructorsOverloadDomain, referenceExpression, call.getValueArgumentList(), false),
                        call, projectionsStripped);
            }
            else {
                constructorReturnedType = resolveCall(
                        scope,
                        wrapForTracing(constructorsOverloadDomain, referenceExpression, call.getValueArgumentList(), false),
                        call);
            }
            if (constructorReturnedType == null && !ErrorUtils.isErrorType(receiverType)) {
                DeclarationDescriptor declarationDescriptor = receiverType.getConstructor().getDeclarationDescriptor();
                assert declarationDescriptor != null;
                trace.recordReferenceResolution(referenceExpression, declarationDescriptor);
                // TODO : more helpful message
                JetArgumentList argumentList = call.getValueArgumentList();
                final String errorMessage = "Cannot find a constructor overload for class " + classDescriptor.getName() + " with these arguments";
                if (argumentList != null) {
                    trace.getErrorHandler().genericError(argumentList.getNode(), errorMessage);
                }
                else {
                    trace.getErrorHandler().genericError(call.asElement().getNode(), errorMessage);
                }
                constructorReturnedType = receiverType;
            }
            // If no upcast needed:
            return constructorReturnedType;

            // Automatic upcast:
    //                            result = receiverType;
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class TypeInferenceContext {
        public final BindingTrace trace;
        public final TypeResolver typeResolver;
        public final ClassDescriptorResolver classDescriptorResolver;

        public final JetScope scope;
        public final boolean preferBlock;
        public final DataFlowInfo dataFlowInfo;

        public final JetType expectedType;
        public final JetType expectedReturnType;

        private TypeInferenceContext(
                @NotNull BindingTrace trace,
                @NotNull JetScope scope,
                boolean preferBlock,
                @NotNull DataFlowInfo dataFlowInfo,
                @NotNull JetType expectedType,
                @Nullable JetType expectedReturnType) {
            this.trace = trace;
            this.typeResolver = new TypeResolver(semanticServices, trace, true);
            this.classDescriptorResolver = semanticServices.getClassDescriptorResolver(trace);
            this.scope = scope;
            this.preferBlock = preferBlock;
            this.dataFlowInfo = dataFlowInfo;
            this.expectedType = expectedType;
            this.expectedReturnType = expectedReturnType;
        }

        public TypeInferenceContext replaceDataFlowInfo(DataFlowInfo newDataFlowInfo) {
            return new TypeInferenceContext(trace, scope, preferBlock, newDataFlowInfo, expectedType, expectedReturnType);
        }
    }

    private class TypeInferrerVisitor extends JetVisitor {

        protected final TypeInferenceContext context;
        protected final Services services;

        protected JetType result;
        protected DataFlowInfo resultDataFlowInfo;


        private TypeInferrerVisitor(@NotNull TypeInferenceContext context) {
            this.context = context;
            this.services = new Services(context.trace);
        }

        @NotNull
        public TypeInferrerVisitor createNew(@NotNull TypeInferenceContext context) {
            return new TypeInferrerVisitor(context);
        }

        @Nullable
        public DataFlowInfo getResultingDataFlowInfo() {
            return resultDataFlowInfo;
        }

        @Nullable
        public JetType getType(@NotNull JetScope scope, @NotNull JetExpression expression, boolean preferBlock) {
            return getTypeWithNewContext(expression, new TypeInferenceContext(context.trace, scope, preferBlock, context.dataFlowInfo, context.expectedType, context.expectedReturnType));
        }

        private JetType getTypeWithNewDataFlowInfo(JetScope scope, JetExpression expression, boolean preferBlock, DataFlowInfo newDataFlowInfo) {
            return getTypeWithNewContext(expression, new TypeInferenceContext(context.trace, scope, preferBlock, newDataFlowInfo, context.expectedType, context.expectedReturnType));
        }

        @Nullable
        public JetType getTypeWithNewContext(@NotNull JetExpression expression, @NotNull TypeInferenceContext newContext) {
            TypeInferrerVisitor visitor;
            if (newContext.equals(context)) {
                visitor = this;
            }
            else {
                visitor = createNew(newContext);
            }
            JetType type = visitor.getType(expression);
            visitor.result = null;
            return type;
        }

        @Nullable
        public final JetType getType(@NotNull JetExpression expression) {
            assert result == null;
            if (context.trace.isProcessed(expression)) {
                return context.trace.getBindingContext().getExpressionType(expression);
            }
            try {
                expression.accept(this);
                // Some recursive definitions (object expressions) must put their types in the cache manually:
                if (context.trace.isProcessed(expression)) {
                    return context.trace.getBindingContext().getExpressionType(expression);
                }

                if (result instanceof DeferredType) {
                    result = ((DeferredType) result).getActualType();
                }
                if (result != null) {
                    context.trace.recordExpressionType(expression, result);
                    if (JetStandardClasses.isNothing(result) && !result.isNullable()) {
                        markDominatedExpressionsAsUnreachable(expression);
                    }
                }
            }
            catch (ReenteringLazyValueComputationException e) {
                context.trace.getErrorHandler().genericError(expression.getNode(), "Type inference has run into a recursive problem"); // TODO : message
                result = null;
            }

            if (!context.trace.isProcessed(expression)) {
                context.trace.recordResolutionScope(expression, context.scope);
            }
            context.trace.markAsProcessed(expression);
            return result;
        }

        public void resetResult() {
            result = null;
            resultDataFlowInfo = null;
//            resultScope = null;
        }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        private void markDominatedExpressionsAsUnreachable(JetExpression expression) {
            List<JetElement> dominated = new ArrayList<JetElement>();
            flowInformationProvider.collectDominatedExpressions(expression, dominated);
            Set<JetElement> rootExpressions = JetPsiUtil.findRootExpressions(dominated);
            for (JetElement rootExpression : rootExpressions) {
                context.trace.getErrorHandler().genericError(rootExpression.getNode(),
                        "This code is unreachable, because '" + expression.getText() + "' never terminates normally");
            }
        }

        @Nullable
        private JetType getBlockReturnedType(@NotNull JetScope outerScope, @NotNull List<JetElement> block, DataFlowInfo dataFlowInfo, JetType extectedReturnType) {
            if (block.isEmpty()) {
                return JetStandardClasses.getUnitType();
            }

            DeclarationDescriptor containingDescriptor = outerScope.getContainingDeclaration();
            WritableScope scope = new WritableScopeImpl(outerScope, containingDescriptor, context.trace.getErrorHandler()).setDebugName("getBlockReturnedType");
            return services.getBlockReturnedTypeWithWritableScope(scope, block, dataFlowInfo, extectedReturnType);
        }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        @Override
        public void visitSimpleNameExpression(JetSimpleNameExpression expression) {
            // TODO : other members
            // TODO : type substitutions???
            String referencedName = expression.getReferencedName();
            if (expression.getReferencedNameElementType() == JetTokens.FIELD_IDENTIFIER
                    && referencedName != null) {
                PropertyDescriptor property = context.scope.getPropertyByFieldReference(referencedName);
                if (property == null) {
                    context.trace.getErrorHandler().unresolvedReference(expression);
                }
                else {
                    context.trace.recordReferenceResolution(expression, property);
                    result = property.getOutType();
                }
            }
            else {
                assert  expression.getReferencedNameElementType() == JetTokens.IDENTIFIER;
                if (referencedName != null) {
                    VariableDescriptor variable = context.scope.getVariable(referencedName);
                    if (variable != null) {
                        context.trace.recordReferenceResolution(expression, variable);
                        result = variable.getOutType();
                        if (result == null) {
                            context.trace.getErrorHandler().genericError(expression.getNode(), "This variable is not readable in this context");
                        }
                        return;
                    }
                    else {
                        ClassifierDescriptor classifier = context.scope.getClassifier(referencedName);
                        if (classifier != null) {
                            JetType classObjectType = classifier.getClassObjectType();
                            if (classObjectType != null && (isNamespacePosition() || classifier.isClassObjectAValue())) {
                                result = classObjectType;
                            }
                            else {
                                context.trace.getErrorHandler().genericError(expression.getNode(), "Classifier " + classifier.getName() +  " does not have a class object");
                            }
                            context.trace.recordReferenceResolution(expression, classifier);
                            return;
                        }
                        else if (furtherNameLookup(expression, referencedName)) {
                            return;
                        }
                    }
                    context.trace.getErrorHandler().unresolvedReference(expression);
                }
            }
        }

        public boolean isNamespacePosition() {
            return false;
        }

        protected boolean furtherNameLookup(@NotNull JetSimpleNameExpression expression, @NotNull String referencedName) {
            NamespaceType namespaceType = lookupNamespaceType(expression, referencedName);
            if (namespaceType != null) {
                context.trace.getErrorHandler().genericError(expression.getNode(), "Expression expected, but a namespace name found");
                return true;
            }
            return false;
        }

        @Nullable
        protected NamespaceType lookupNamespaceType(@NotNull JetSimpleNameExpression expression, @NotNull String referencedName) {
            NamespaceDescriptor namespace = context.scope.getNamespace(referencedName);
            if (namespace == null) {
                return null;
            }
            context.trace.recordReferenceResolution(expression, namespace);
            return namespace.getNamespaceType();
        }

        @Override
        public void visitObjectLiteralExpression(final JetObjectLiteralExpression expression) {
            TopDownAnalyzer topDownAnalyzer = new TopDownAnalyzer(semanticServices, new BindingTraceAdapter(context.trace) {
                @Override
                public void recordDeclarationResolution(@NotNull PsiElement declaration, @NotNull final DeclarationDescriptor descriptor) {
                    if (declaration == expression.getObjectDeclaration()) {
                        JetType defaultType = new DeferredType(new LazyValue<JetType>() {
                            @Override
                            protected JetType compute() {
                                return ((ClassDescriptor) descriptor).getDefaultType();
                            }
                        });
                        result = defaultType;
                        if (!context.trace.isProcessed(expression)) {
                            recordExpressionType(expression, defaultType);
                            markAsProcessed(expression);
                        }
                    }
                    super.recordDeclarationResolution(declaration, descriptor);
                }
            });
            topDownAnalyzer.processObject(context.scope, context.scope.getContainingDeclaration(), expression.getObjectDeclaration());
        }

        @Override
        public void visitFunctionLiteralExpression(JetFunctionLiteralExpression expression) {
            if (context.preferBlock && !expression.hasParameterSpecification()) {
                context.trace.recordBlock(expression);
                result = getBlockReturnedType(context.scope, expression.getBody(), context.dataFlowInfo, context.expectedReturnType);
                return;
            }

            JetTypeReference receiverTypeRef = expression.getReceiverTypeRef();
            final JetType receiverType;
            if (receiverTypeRef != null) {
                receiverType = context.typeResolver.resolveType(context.scope, receiverTypeRef);
            } else {
                receiverType = context.scope.getThisType();
            }

            FunctionDescriptorImpl functionDescriptor = new FunctionDescriptorImpl(
                    context.scope.getContainingDeclaration(), Collections.<Annotation>emptyList(), "<anonymous>");

            List<JetType> parameterTypes = new ArrayList<JetType>();
            List<ValueParameterDescriptor> valueParameterDescriptors = Lists.newArrayList();
            List<JetParameter> parameters = expression.getParameters();
            for (int i = 0, parametersSize = parameters.size(); i < parametersSize; i++) {
                JetParameter parameter = parameters.get(i);
                JetTypeReference typeReference = parameter.getTypeReference();

                JetType type;
                if (typeReference != null) {
                    type = context.typeResolver.resolveType(context.scope, typeReference);
                }
                else {
                    context.trace.getErrorHandler().genericError(parameter.getNode(), "Type inference for parameters is not implemented yet");
                    type = ErrorUtils.createErrorType("Not inferred");
                }
                ValueParameterDescriptor valueParameterDescriptor = context.classDescriptorResolver.resolveValueParameterDescriptor(functionDescriptor, parameter, i, type);
                parameterTypes.add(valueParameterDescriptor.getOutType());
                valueParameterDescriptors.add(valueParameterDescriptor);
            }

            JetType effectiveReceiverType = receiverTypeRef == null ? null : receiverType;
            functionDescriptor.initialize(effectiveReceiverType, Collections.<TypeParameterDescriptor>emptyList(), valueParameterDescriptors, null);
            context.trace.recordDeclarationResolution(expression, functionDescriptor);

            JetTypeReference returnTypeRef = expression.getReturnTypeRef();
            JetType returnType;
            if (returnTypeRef != null) {
                returnType = context.typeResolver.resolveType(context.scope, returnTypeRef);
            } else {
                WritableScope writableScope = new WritableScopeImpl(context.scope, functionDescriptor, context.trace.getErrorHandler()).setDebugName("Inner scope of a function literal");
                for (VariableDescriptor variableDescriptor : valueParameterDescriptors) {
                    writableScope.addVariableDescriptor(variableDescriptor);
                }
                writableScope.setThisType(receiverType);
                returnType = getBlockReturnedType(writableScope, expression.getBody(), context.dataFlowInfo, context.expectedReturnType);
            }
            JetType safeReturnType = returnType == null ? ErrorUtils.createErrorType("<return type>") : returnType;
            functionDescriptor.setReturnType(safeReturnType);


            result = JetStandardClasses.getFunctionType(Collections.<Annotation>emptyList(), effectiveReceiverType, parameterTypes, safeReturnType);
        }

        @Override
        public void visitParenthesizedExpression(JetParenthesizedExpression expression) {
            JetExpression inner = expression.getExpression();
            if (inner != null) {
                result = getType(context.scope, inner, false);
            }
        }

        @Override
        public void visitConstantExpression(JetConstantExpression expression) {
            IElementType elementType = expression.getNode().getElementType();
            JetStandardLibrary standardLibrary = semanticServices.getStandardLibrary();
            if (elementType == JetNodeTypes.INTEGER_CONSTANT) {
                Object value = expression.getValue();
                if (value == null) {
                    context.trace.getErrorHandler().genericError(expression.getNode(), "Number is of range for Long");
                }
                else if (value instanceof Long) {
                    result = standardLibrary.getLongType();
                }
                else {
                    result = standardLibrary.getIntType();
                }
                // TODO : other ranges
            }
            else if (elementType == JetNodeTypes.LONG_CONSTANT) {
                result = standardLibrary.getLongType();
            }
            else if (elementType == JetNodeTypes.FLOAT_CONSTANT) {
                String text = expression.getText();
                assert text.length() > 0;
                char lastChar = text.charAt(text.length() - 1);
                if (lastChar == 'f' || lastChar == 'F') {
                    result = standardLibrary.getFloatType();
                }
                else {
                    result = standardLibrary.getDoubleType();
                }
            }
            else if (elementType == JetNodeTypes.BOOLEAN_CONSTANT) {
                result = standardLibrary.getBooleanType();
            }
            else if (elementType == JetNodeTypes.CHARACTER_CONSTANT) {
                result = standardLibrary.getCharType();
            }
            else if (elementType == JetNodeTypes.STRING_CONSTANT) {
                result = standardLibrary.getStringType();
            }
            else if (elementType == JetNodeTypes.NULL) {
                result = JetStandardClasses.getNullableNothingType();
            }
            else {
                throw new IllegalArgumentException("Unsupported constant: " + expression);
            }
        }

        @Override
        public void visitThrowExpression(JetThrowExpression expression) {
            JetExpression thrownExpression = expression.getThrownExpression();
            if (thrownExpression != null) {
                JetType type = getType(context.scope, thrownExpression, false);
                // TODO : check that it inherits Throwable
            }
            result = JetStandardClasses.getNothingType();
        }

        @Override
        public void visitReturnExpression(JetReturnExpression expression) {
            if (context.expectedReturnType == FORBIDDEN) {
                context.trace.getErrorHandler().genericError(expression.getNode(), "'return' is not allowed here");
                return;
            }
            JetExpression returnedExpression = expression.getReturnedExpression();

            JetType returnedType = JetStandardClasses.getUnitType();
            if (returnedExpression != null) {
                returnedType = getType(context.scope, returnedExpression, false);
            }
            else {
                if (context.expectedReturnType != null && !JetStandardClasses.isUnit(context.expectedReturnType)) {
                    context.trace.getErrorHandler().genericError(expression.getNode(), "This function must return a value of type " + context.expectedReturnType);
                }
            }

            if (context.expectedReturnType != null && returnedType != null) {
                if (!semanticServices.getTypeChecker().isSubtypeOf(returnedType, context.expectedReturnType)) {
                    context.trace.getErrorHandler().typeMismatch(returnedExpression == null ? expression : returnedExpression, context.expectedReturnType, returnedType);
                }
            }

            result = JetStandardClasses.getNothingType();
        }

        @Override
        public void visitBreakExpression(JetBreakExpression expression) {
            result = JetStandardClasses.getNothingType();
        }

        @Override
        public void visitContinueExpression(JetContinueExpression expression) {
            result = JetStandardClasses.getNothingType();
        }

        @Override
        public void visitTypeofExpression(JetTypeofExpression expression) {
            JetType type = services.safeGetType(context.scope, expression.getBaseExpression(), false, NO_EXPECTED_TYPE); // TODO
            result = semanticServices.getStandardLibrary().getTypeInfoType(type);
        }

        @Override
        public void visitBinaryWithTypeRHSExpression(JetBinaryExpressionWithTypeRHS expression) {
            IElementType operationType = expression.getOperationSign().getReferencedNameElementType();
            JetType actualType = getType(context.scope, expression.getLeft(), false);
            JetTypeReference right = expression.getRight();
            if (right != null) {
                JetType targetType = context.typeResolver.resolveType(context.scope, right);
                if (operationType == JetTokens.COLON) {
                    if (actualType != null && !semanticServices.getTypeChecker().isSubtypeOf(actualType, targetType)) {
                        context.trace.getErrorHandler().typeMismatch(expression.getLeft(), targetType, actualType);
                    }
                    result = targetType;
                }
                else if (operationType == JetTokens.AS_KEYWORD) {
                    checkForCastImpossibility(expression, actualType, targetType);
                    result = targetType;
                }
                else if (operationType == JetTokens.AS_SAFE) {
                    checkForCastImpossibility(expression, actualType, targetType);
                    result = TypeUtils.makeNullable(targetType);
                }
                else {
                    context.trace.getErrorHandler().genericError(expression.getOperationSign().getNode(), "Unsupported binary operation");
                }
            }
        }

        private void checkForCastImpossibility(JetBinaryExpressionWithTypeRHS expression, JetType actualType, JetType targetType) {
            if (actualType == null) return;

            JetTypeChecker typeChecker = semanticServices.getTypeChecker();
            if (!typeChecker.isSubtypeOf(targetType, actualType)) {
                if (typeChecker.isSubtypeOf(actualType, targetType)) {
                    context.trace.getErrorHandler().genericWarning(expression.getOperationSign().getNode(), "No cast needed, use ':' instead");
                }
                else {
                    // See JET-58 Make 'as never succeeds' a warning, or even never check for Java (external) types
                    context.trace.getErrorHandler().genericWarning(expression.getOperationSign().getNode(), "This cast can never succeed");
                }
            }
            else {
                if (typeChecker.isSubtypeOf(actualType, targetType)) {
                    context.trace.getErrorHandler().genericWarning(expression.getOperationSign().getNode(), "No cast needed");
                }
            }
        }

        @Override
        public void visitTupleExpression(JetTupleExpression expression) {
            List<JetExpression> entries = expression.getEntries();
            List<JetType> types = new ArrayList<JetType>();
            for (JetExpression entry : entries) {
                types.add(services.safeGetType(context.scope, entry, false, NO_EXPECTED_TYPE)); // TODO
            }
            // TODO : labels
            result = JetStandardClasses.getTupleType(types);
        }

        @Override
        public void visitThisExpression(JetThisExpression expression) {
            JetType thisType = null;
            String labelName = expression.getLabelName();
            if (labelName != null) {
                Collection<DeclarationDescriptor> declarationsByLabel = context.scope.getDeclarationsByLabel(labelName);
                int size = declarationsByLabel.size();
                final JetSimpleNameExpression targetLabel = expression.getTargetLabel();
                assert targetLabel != null;
                if (size == 1) {
                    DeclarationDescriptor declarationDescriptor = declarationsByLabel.iterator().next();
                    if (declarationDescriptor instanceof ClassDescriptor) {
                        ClassDescriptor classDescriptor = (ClassDescriptor) declarationDescriptor;
                        thisType = classDescriptor.getDefaultType();
                    }
                    else if (declarationDescriptor instanceof FunctionDescriptor) {
                        FunctionDescriptor functionDescriptor = (FunctionDescriptor) declarationDescriptor;
                        thisType = functionDescriptor.getReceiverType();
                    }
                    else {
                        throw new UnsupportedOperationException(); // TODO
                    }
                    context.trace.recordReferenceResolution(targetLabel, declarationDescriptor);
                    context.trace.recordReferenceResolution(expression.getThisReference(), declarationDescriptor);
                }
                else if (size == 0) {
                    // This uses the info written by the control flow processor
                    PsiElement psiElement = context.trace.getBindingContext().resolveToDeclarationPsiElement(targetLabel);
                    if (psiElement instanceof JetFunctionLiteralExpression) {
                        DeclarationDescriptor declarationDescriptor = context.trace.getBindingContext().getDeclarationDescriptor(psiElement);
                        if (declarationDescriptor instanceof FunctionDescriptor) {
                            thisType = ((FunctionDescriptor) declarationDescriptor).getReceiverType();
                            if (thisType == null) {
                                thisType = JetStandardClasses.getNothingType();
                            }
                            else {
                                context.trace.recordReferenceResolution(targetLabel, declarationDescriptor);
                                context.trace.recordReferenceResolution(expression.getThisReference(), declarationDescriptor);
                            }
                        }
                        else {
                            context.trace.getErrorHandler().unresolvedReference(targetLabel);
                        }
                    }
                    else {
                        context.trace.getErrorHandler().unresolvedReference(targetLabel);
                    }
                }
                else {
                    context.trace.getErrorHandler().genericError(targetLabel.getNode(), "Ambiguous label");
                }
            }
            else {
                thisType = context.scope.getThisType();

                DeclarationDescriptor declarationDescriptorForUnqualifiedThis = context.scope.getDeclarationDescriptorForUnqualifiedThis();
                if (declarationDescriptorForUnqualifiedThis != null) {
                    context.trace.recordReferenceResolution(expression.getThisReference(), declarationDescriptorForUnqualifiedThis);
                }
            }

            if (thisType != null) {
                if (JetStandardClasses.isNothing(thisType)) {
                    context.trace.getErrorHandler().genericError(expression.getNode(), "'this' is not defined in this context");
                }
                else {
                    JetTypeReference superTypeQualifier = expression.getSuperTypeQualifier();
                    if (superTypeQualifier != null) {
                        JetTypeElement superTypeElement = superTypeQualifier.getTypeElement();
                        // Errors are reported by the parser
                        if (superTypeElement instanceof JetUserType) {
                            JetUserType typeElement = (JetUserType) superTypeElement;

                            ClassifierDescriptor classifierCandidate = context.typeResolver.resolveClass(context.scope, typeElement);
                            if (classifierCandidate instanceof ClassDescriptor) {
                                ClassDescriptor superclass = (ClassDescriptor) classifierCandidate;

                                Collection<? extends JetType> supertypes = thisType.getConstructor().getSupertypes();
                                TypeSubstitutor substitutor = TypeSubstitutor.create(thisType);
                                for (JetType declaredSupertype : supertypes) {
                                    if (declaredSupertype.getConstructor().equals(superclass.getTypeConstructor())) {
                                        result = substitutor.safeSubstitute(declaredSupertype, Variance.INVARIANT);
                                        break;
                                    }
                                }
                                if (result == null) {
                                    context.trace.getErrorHandler().genericError(superTypeElement.getNode(), "Not a superclass");
                                }
                            }
                        }
                    } else {
                        result = thisType;
                    }
                    if (result != null) {
                        context.trace.recordExpressionType(expression.getThisReference(), result);
                    }
                }
            }
        }

        @Override
        public void visitBlockExpression(JetBlockExpression expression) {
            result = getBlockReturnedType(context.scope, expression.getStatements(), context.dataFlowInfo, context.expectedReturnType);
        }

        @Override
        public void visitWhenExpression(final JetWhenExpression expression) {
            // TODO :change scope according to the bound value in the when header
            final JetExpression subjectExpression = expression.getSubjectExpression();

            final JetType subjectType = subjectExpression != null ? services.safeGetType(context.scope, subjectExpression, false, NO_EXPECTED_TYPE) : ErrorUtils.createErrorType("Unknown type");
            final VariableDescriptor variableDescriptor = subjectExpression != null ? getVariableDescriptorFromSimpleName(subjectExpression) : null;

            // TODO : exhaustive patterns

            Set<JetType> expressionTypes = Sets.newHashSet();
            for (JetWhenEntry whenEntry : expression.getEntries()) {
                JetWhenCondition condition = whenEntry.getCondition();
                WritableScope scopeToExtend = newWritableScopeImpl().setDebugName("Scope extended in when entry");
                DataFlowInfo newDataFlowInfo = context.dataFlowInfo;
                if (condition != null) {
                    newDataFlowInfo = checkWhenCondition(subjectExpression, subjectType, condition, scopeToExtend, variableDescriptor);
                }
                JetWhenExpression subWhen = whenEntry.getSubWhen();
                JetExpression bodyExpression = subWhen == null ? whenEntry.getExpression() : subWhen;
                if (bodyExpression != null) {
                    JetType type = getTypeWithNewDataFlowInfo(scopeToExtend, bodyExpression, true, newDataFlowInfo);
                    if (type != null) {
                        expressionTypes.add(type);
                    }
                }
            }

            if (!expressionTypes.isEmpty()) {
                result = semanticServices.getTypeChecker().commonSupertype(expressionTypes);
            }
            else if (expression.getEntries().isEmpty()) {
                context.trace.getErrorHandler().genericError(expression.getNode(), "Entries required for when-expression"); // TODO : Scope, and maybe this should not an error
            }
        }

        private DataFlowInfo checkWhenCondition(@Nullable final JetExpression subjectExpression, final JetType subjectType, JetWhenCondition condition, final WritableScope scopeToExtend, final VariableDescriptor... subjectVariables) {
            final DataFlowInfo[] newDataFlowInfo = new DataFlowInfo[]{context.dataFlowInfo};
            condition.accept(new JetVisitor() {
                @Override
                public void visitWhenConditionWithExpression(JetWhenConditionWithExpression condition) {
                    JetExpression conditionExpression = condition.getExpression();
                    if (conditionExpression != null) {
                        JetType type = getType(context.scope, conditionExpression, false);
                        if (type != null && subjectType != null) {
                            if (TypeUtils.intersect(semanticServices.getTypeChecker(), Sets.newHashSet(subjectType, type)) == null) {
                                context.trace.getErrorHandler().genericError(conditionExpression.getNode(), "This condition can never hold");
                            }
                        }
                    }
                }

                @Override
                public void visitWhenConditionCall(JetWhenConditionCall condition) {
                    JetExpression callSuffixExpression = condition.getCallSuffixExpression();
                    JetScope compositeScope = new ScopeWithReceiver(context.scope, subjectType, semanticServices.getTypeChecker());
                    if (callSuffixExpression != null) {
                        JetType selectorReturnType = getType(compositeScope, callSuffixExpression, false);
                        ensureBooleanResultWithCustomSubject(callSuffixExpression, selectorReturnType, "This expression");
                        services.checkNullSafety(subjectType, condition.getOperationTokenNode(), getCalleeFunctionDescriptor(callSuffixExpression));
                    }
                }

                @Override
                public void visitWhenConditionInRange(JetWhenConditionInRange condition) {
                    JetExpression rangeExpression = condition.getRangeExpression();
                    if (rangeExpression != null) {
                        assert subjectExpression != null;
                        checkInExpression(condition.getOperationReference(), subjectExpression, rangeExpression);
                    }
                }

                @Override
                public void visitWhenConditionIsPattern(JetWhenConditionIsPattern condition) {
                    JetPattern pattern = condition.getPattern();
                    if (pattern != null) {
                        newDataFlowInfo[0] = checkPatternType(pattern, subjectType, scopeToExtend, subjectVariables);
                    }
                }

                @Override
                public void visitJetElement(JetElement element) {
                    context.trace.getErrorHandler().genericError(element.getNode(), "Unsupported [JetTypeInferrer] : " + element);
                }
            });
            return newDataFlowInfo[0];
        }

        private DataFlowInfo checkPatternType(@NotNull JetPattern pattern, @NotNull final JetType subjectType, @NotNull final WritableScope scopeToExtend, @NotNull final VariableDescriptor... subjectVariables) {
            final DataFlowInfo[] result = new DataFlowInfo[] {context.dataFlowInfo};
            pattern.accept(new JetVisitor() {
                @Override
                public void visitTypePattern(JetTypePattern typePattern) {
                    JetTypeReference typeReference = typePattern.getTypeReference();
                    if (typeReference != null) {
                        JetType type = context.typeResolver.resolveType(context.scope, typeReference);
                        checkTypeCompatibility(type, subjectType, typePattern);
                        result[0] = context.dataFlowInfo.isInstanceOf(subjectVariables, type);
                    }
                }

                @Override
                public void visitTuplePattern(JetTuplePattern pattern) {
                    List<JetTuplePatternEntry> entries = pattern.getEntries();
                    TypeConstructor typeConstructor = subjectType.getConstructor();
                    if (!JetStandardClasses.getTuple(entries.size()).getTypeConstructor().equals(typeConstructor)
                        || typeConstructor.getParameters().size() != entries.size()) {
                        context.trace.getErrorHandler().genericError(pattern.getNode(), "Type mismatch: subject is of type " + subjectType + " but the pattern is of type Tuple" + entries.size()); // TODO : message
                    }
                    else {
                        for (int i = 0, entriesSize = entries.size(); i < entriesSize; i++) {
                            JetTuplePatternEntry entry = entries.get(i);
                            JetType type = subjectType.getArguments().get(i).getType();

                            // TODO : is a name always allowed, ie for tuple patterns, not decomposer arg lists?
                            ASTNode nameLabelNode = entry.getNameLabelNode();
                            if (nameLabelNode != null) {
                                context.trace.getErrorHandler().genericError(nameLabelNode, "Unsupported [JetTypeInferrer]");
                            }

                            JetPattern entryPattern = entry.getPattern();
                            if (entryPattern != null) {
                                result[0] = checkPatternType(entryPattern, type, scopeToExtend);
                            }
                        }
                    }
                }

                @Override
                public void visitDecomposerPattern(JetDecomposerPattern pattern) {
                    JetType selectorReturnType = getSelectorReturnType(subjectType, pattern.getDecomposerExpression());

                    result[0] = checkPatternType(pattern.getArgumentList(), selectorReturnType == null ? ErrorUtils.createErrorType("No type") : selectorReturnType, scopeToExtend);
                }

                @Override
                public void visitWildcardPattern(JetWildcardPattern pattern) {
                    // Nothing
                }

                @Override
                public void visitExpressionPattern(JetExpressionPattern pattern) {
                    JetType type = getType(scopeToExtend, pattern.getExpression(), false);
                    checkTypeCompatibility(type, subjectType, pattern);
                }

                @Override
                public void visitBindingPattern(JetBindingPattern pattern) {
                    JetProperty variableDeclaration = pattern.getVariableDeclaration();
                    JetTypeReference propertyTypeRef = variableDeclaration.getPropertyTypeRef();
                    JetType type = propertyTypeRef == null ? subjectType : context.typeResolver.resolveType(context.scope, propertyTypeRef);
                    VariableDescriptor variableDescriptor = context.classDescriptorResolver.resolveLocalVariableDescriptorWithType(context.scope.getContainingDeclaration(), variableDeclaration, type);
                    scopeToExtend.addVariableDescriptor(variableDescriptor);
                    if (propertyTypeRef != null) {
                        if (!semanticServices.getTypeChecker().isSubtypeOf(subjectType, type)) {
                            context.trace.getErrorHandler().genericError(propertyTypeRef.getNode(), type + " must be a supertype of " + subjectType + ". Use 'is' to match against " + type);
                        }
                    }

                    JetWhenCondition condition = pattern.getCondition();
                    if (condition != null) {
                        int oldLength = subjectVariables.length;
                        VariableDescriptor[] newSubjectVariables = new VariableDescriptor[oldLength + 1];
                        System.arraycopy(subjectVariables, 0, newSubjectVariables, 0, oldLength);
                        newSubjectVariables[oldLength] = variableDescriptor;
                        result[0] = checkWhenCondition(null, subjectType, condition, scopeToExtend, newSubjectVariables);
                    }
                }

                private void checkTypeCompatibility(@Nullable JetType type, @NotNull JetType subjectType, @NotNull JetElement reportErrorOn) {
                    // TODO : Take auto casts into account?
                    if (type == null) {
                        return;
                    }
                    if (TypeUtils.intersect(semanticServices.getTypeChecker(), Sets.newHashSet(type, subjectType)) == null) {
                        context.trace.getErrorHandler().genericError(reportErrorOn.getNode(), "Incompatible types: " + type + " and " + subjectType); // TODO : message
                    }
                }

                @Override
                public void visitJetElement(JetElement element) {
                    context.trace.getErrorHandler().genericError(element.getNode(), "Unsupported [JetTypeInferrer]");
                }
            });
            return result[0];
        }

        @Override
        public void visitTryExpression(JetTryExpression expression) {
            JetExpression tryBlock = expression.getTryBlock();
            List<JetCatchClause> catchClauses = expression.getCatchClauses();
            JetFinallySection finallyBlock = expression.getFinallyBlock();
            List<JetType> types = new ArrayList<JetType>();
            for (JetCatchClause catchClause : catchClauses) {
                JetParameter catchParameter = catchClause.getCatchParameter();
                JetExpression catchBody = catchClause.getCatchBody();
                if (catchParameter != null) {
                    VariableDescriptor variableDescriptor = context.classDescriptorResolver.resolveLocalVariableDescriptor(context.scope.getContainingDeclaration(), context.scope, catchParameter);
                    if (catchBody != null) {
                        WritableScope catchScope = newWritableScopeImpl().setDebugName("Catch scope");
                        catchScope.addVariableDescriptor(variableDescriptor);
                        JetType type = getType(catchScope, catchBody, true);
                        if (type != null) {
                            types.add(type);
                        }
                    }
                }
            }
            if (finallyBlock != null) {
                types.clear(); // Do not need the list for the check, but need the code above to typecheck catch bodies
                JetType type = getType(context.scope, finallyBlock.getFinalExpression(), true);
                if (type != null) {
                    types.add(type);
                }
            }
            JetType type = getType(context.scope, tryBlock, true);
            if (type != null) {
                types.add(type);
            }
            if (types.isEmpty()) {
                result = null;
            }
            else {
                result = semanticServices.getTypeChecker().commonSupertype(types);
            }
        }

        @Override
        public void visitIfExpression(JetIfExpression expression) {
            JetExpression condition = expression.getCondition();
            checkCondition(context.scope, condition);

            JetExpression elseBranch = expression.getElse();
            JetExpression thenBranch = expression.getThen();

            WritableScopeImpl thenScope = newWritableScopeImpl().setDebugName("Then scope");
            DataFlowInfo thenInfo = extractDataFlowInfoFromCondition(condition, true, thenScope);
            DataFlowInfo elseInfo = extractDataFlowInfoFromCondition(condition, false, null);

            if (elseBranch == null) {
                if (thenBranch != null) {
                    JetType type = getTypeWithNewDataFlowInfo(thenScope, thenBranch, true, thenInfo);
                    if (type != null && JetStandardClasses.isNothing(type)) {
                        resultDataFlowInfo = elseInfo;
//                        resultScope = elseScope;
                    }
                    result = JetStandardClasses.getUnitType();
                }
            }
            else if (thenBranch == null) {
                JetType type = getTypeWithNewDataFlowInfo(context.scope, elseBranch, true, elseInfo);
                if (type != null && JetStandardClasses.isNothing(type)) {
                    resultDataFlowInfo = thenInfo;
//                    resultScope = thenScope;
                }
                result = JetStandardClasses.getUnitType();
            }
            else {
                JetType thenType = getTypeWithNewDataFlowInfo(thenScope, thenBranch, true, thenInfo);
                JetType elseType = getTypeWithNewDataFlowInfo(context.scope, elseBranch, true, elseInfo);

                if (thenType == null) {
                    result = elseType;
                }
                else if (elseType == null) {
                    result = thenType;
                }
                else {
                    result = semanticServices.getTypeChecker().commonSupertype(Arrays.asList(thenType, elseType));
                }

                boolean jumpInThen = thenType != null && JetStandardClasses.isNothing(thenType);
                boolean jumpInElse = elseType != null && JetStandardClasses.isNothing(elseType);

                if (jumpInThen && !jumpInElse) {
                    resultDataFlowInfo = elseInfo;
//                    resultScope = elseScope;
                }
                else if (jumpInElse && !jumpInThen) {
                    resultDataFlowInfo = thenInfo;
//                    resultScope = thenScope;
                }
            }
        }

        private DataFlowInfo extractDataFlowInfoFromCondition(@Nullable JetExpression condition, final boolean conditionValue, @Nullable final WritableScope scopeToExtend) {
            if (condition == null) return context.dataFlowInfo;
            final DataFlowInfo[] result = new DataFlowInfo[] {context.dataFlowInfo};
            condition.accept(new JetVisitor() {
                @Override
                public void visitIsExpression(JetIsExpression expression) {
                    if (conditionValue) {
                        JetPattern pattern = expression.getPattern();
                        result[0] = patternsToDataFlowInfo.get(pattern);
                        if (scopeToExtend != null) {
                            for (VariableDescriptor variableDescriptor : patternsToBoundVariableLists.get(pattern)) {
                                scopeToExtend.addVariableDescriptor(variableDescriptor);
                            }
                        }
                    }
                }

                @Override
                public void visitBinaryExpression(JetBinaryExpression expression) {
                    IElementType operationToken = expression.getOperationToken();
                    if (operationToken == JetTokens.ANDAND || operationToken == JetTokens.OROR) {
                        WritableScope actualScopeToExtend;
                        if (operationToken == JetTokens.ANDAND) {
                            actualScopeToExtend = conditionValue ? scopeToExtend : null;
                        }
                        else {
                            actualScopeToExtend = conditionValue ? null : scopeToExtend;
                        }

                        DataFlowInfo dataFlowInfo = extractDataFlowInfoFromCondition(expression.getLeft(), conditionValue, actualScopeToExtend);
                        JetExpression expressionRight = expression.getRight();
                        if (expressionRight != null) {
                            DataFlowInfo rightInfo = extractDataFlowInfoFromCondition(expressionRight, conditionValue, actualScopeToExtend);
                            DataFlowInfo.CompositionOperator operator;
                            if (operationToken == JetTokens.ANDAND) {
                                operator = conditionValue ? DataFlowInfo.AND : DataFlowInfo.OR;
                            }
                            else {
                                operator = conditionValue ? DataFlowInfo.OR : DataFlowInfo.AND;
                            }
                            dataFlowInfo = operator.compose(dataFlowInfo, rightInfo);
                        }
                        result[0] = dataFlowInfo;
                    }
                    else if (operationToken == JetTokens.EQEQ
                             || operationToken == JetTokens.EXCLEQ
                             || operationToken == JetTokens.EQEQEQ
                             || operationToken == JetTokens.EXCLEQEQEQ) {
                        JetExpression left = expression.getLeft();
                        JetExpression right = expression.getRight();
                        if (right == null) return;

                        if (!(left instanceof JetSimpleNameExpression)) {
                            JetExpression tmp = left;
                            left = right;
                            right = tmp;

                            if (!(left instanceof JetSimpleNameExpression)) {
                                return;
                            }
                        }

                        VariableDescriptor variableDescriptor = getVariableDescriptorFromSimpleName(left);

                        // TODO : validate that DF makes sense for this variable: local, val, internal w/backing field, etc

                        // Comparison to a non-null expression
                        JetType rhsType = context.trace.getBindingContext().getExpressionType(right);
                        if (rhsType != null && !rhsType.isNullable()) {
                            extendDataFlowWithNullComparison(operationToken, variableDescriptor, !conditionValue);
                            return;
                        }

                        VariableDescriptor rightVariable = getVariableDescriptorFromSimpleName(right);
                        if (rightVariable != null) {
                            JetType lhsType = context.trace.getBindingContext().getExpressionType(left);
                            if (lhsType != null && !lhsType.isNullable()) {
                                extendDataFlowWithNullComparison(operationToken, rightVariable, !conditionValue);
                                return;
                            }
                        }

                        // Comparison to 'null'
                        if (!(right instanceof JetConstantExpression)) {
                            return;
                        }
                        JetConstantExpression constantExpression = (JetConstantExpression) right;
                        if (constantExpression.getNode().getElementType() != JetNodeTypes.NULL) {
                            return;
                        }

                        extendDataFlowWithNullComparison(operationToken, variableDescriptor, conditionValue);
                    }
                }

                private void extendDataFlowWithNullComparison(IElementType operationToken, VariableDescriptor variableDescriptor, boolean equalsToNull) {
                    if (operationToken == JetTokens.EQEQ || operationToken == JetTokens.EQEQEQ) {
                        result[0] = context.dataFlowInfo.equalsToNull(variableDescriptor, !equalsToNull);
                    }
                    else if (operationToken == JetTokens.EXCLEQ || operationToken == JetTokens.EXCLEQEQEQ) {
                        result[0] = context.dataFlowInfo.equalsToNull(variableDescriptor, equalsToNull);
                    }
                }

                @Override
                public void visitUnaryExpression(JetUnaryExpression expression) {
                    IElementType operationTokenType = expression.getOperationSign().getReferencedNameElementType();
                    if (operationTokenType == JetTokens.EXCL) {
                        JetExpression baseExpression = expression.getBaseExpression();
                        if (baseExpression != null) {
                            result[0] = extractDataFlowInfoFromCondition(baseExpression, !conditionValue, scopeToExtend);
                        }
                    }
                }

                @Override
                public void visitParenthesizedExpression(JetParenthesizedExpression expression) {
                    JetExpression body = expression.getExpression();
                    if (body != null) {
                        body.accept(this);
                    }
                }
            });
            return result[0];
        }

        private void checkCondition(@NotNull JetScope scope, @Nullable JetExpression condition) {
            if (condition != null) {
                JetType conditionType = getType(scope, condition, false);

                if (conditionType != null && !isBoolean(conditionType)) {
                    context.trace.getErrorHandler().genericError(condition.getNode(), "Condition must be of type Boolean, but was of type " + conditionType);
                }
            }
        }

        @Override
        public void visitWhileExpression(JetWhileExpression expression) {
            JetExpression condition = expression.getCondition();
            checkCondition(context.scope, condition);
            JetExpression body = expression.getBody();
            if (body != null) {
                WritableScopeImpl scopeToExtend = newWritableScopeImpl().setDebugName("Scope extended in while's condition");
                DataFlowInfo conditionInfo = condition == null ? context.dataFlowInfo : extractDataFlowInfoFromCondition(condition, true, scopeToExtend);
                getTypeWithNewDataFlowInfo(scopeToExtend, body, true, conditionInfo);
            }
            if (!flowInformationProvider.isBreakable(expression)) {
//                resultScope = newWritableScopeImpl();
                resultDataFlowInfo = extractDataFlowInfoFromCondition(condition, false, null);
            }
            result = JetStandardClasses.getUnitType();
        }

        @Override
        public void visitDoWhileExpression(JetDoWhileExpression expression) {
            JetExpression body = expression.getBody();
            JetScope conditionScope = context.scope;
            if (body instanceof JetFunctionLiteralExpression) {
                JetFunctionLiteralExpression function = (JetFunctionLiteralExpression) body;
                if (!function.hasParameterSpecification()) {
                    WritableScope writableScope = newWritableScopeImpl().setDebugName("do..while body scope");
                    conditionScope = writableScope;
                    services.getBlockReturnedTypeWithWritableScope(writableScope, function.getBody(), context.dataFlowInfo, context.expectedReturnType);
                    context.trace.recordBlock(function);
                } else {
                    getType(context.scope, body, true);
                }
            }
            else if (body != null) {
                WritableScope writableScope = newWritableScopeImpl().setDebugName("do..while body scope");
                conditionScope = writableScope;
                services.getBlockReturnedTypeWithWritableScope(writableScope, Collections.singletonList(body), context.dataFlowInfo, context.expectedReturnType);
            }
            JetExpression condition = expression.getCondition();
            checkCondition(conditionScope, condition);
            if (!flowInformationProvider.isBreakable(expression)) {
//                resultScope = newWritableScopeImpl();
                resultDataFlowInfo = extractDataFlowInfoFromCondition(condition, false, null);
            }
            result = JetStandardClasses.getUnitType();
        }

        protected WritableScopeImpl newWritableScopeImpl() {
            return newWritableScopeImpl(context.scope);
        }

        protected WritableScopeImpl newWritableScopeImpl(JetScope scopeToExtend) {
            return new WritableScopeImpl(scopeToExtend, scopeToExtend.getContainingDeclaration(), context.trace.getErrorHandler());
        }

        @Override
        public void visitForExpression(JetForExpression expression) {
            JetParameter loopParameter = expression.getLoopParameter();
            JetExpression loopRange = expression.getLoopRange();
            JetType loopRangeType = null;
            if (loopRange != null) {
                loopRangeType = getType(context.scope, loopRange, false);
            }
            JetType expectedParameterType = null;
            if (loopRangeType != null) {
                expectedParameterType = checkIterableConvention(loopRangeType, loopRange.getNode());
            }

            WritableScope loopScope = newWritableScopeImpl().setDebugName("Scope with for-loop index");

            if (loopParameter != null) {
                JetTypeReference typeReference = loopParameter.getTypeReference();
                VariableDescriptor variableDescriptor;
                if (typeReference != null) {
                    variableDescriptor = context.classDescriptorResolver.resolveLocalVariableDescriptor(context.scope.getContainingDeclaration(), context.scope, loopParameter);
                    JetType actualParameterType = variableDescriptor.getOutType();
                    if (expectedParameterType != null &&
                            actualParameterType != null &&
                            !semanticServices.getTypeChecker().isSubtypeOf(expectedParameterType, actualParameterType)) {
                        context.trace.getErrorHandler().genericError(typeReference.getNode(), "The loop iterates over values of type " + expectedParameterType + " but the parameter is declared to be " + actualParameterType);
                    }
                }
                else {
                    if (expectedParameterType == null) {
                        expectedParameterType = ErrorUtils.createErrorType("Error");
                    }
                    variableDescriptor = context.classDescriptorResolver.resolveLocalVariableDescriptor(context.scope.getContainingDeclaration(), loopParameter, expectedParameterType);
                }
                loopScope.addVariableDescriptor(variableDescriptor);
            }

            JetExpression body = expression.getBody();
            if (body != null) {
                getType(loopScope, body, true); // TODO
            }

            result = JetStandardClasses.getUnitType();
        }

        @Nullable
        private JetType checkIterableConvention(@NotNull JetType type, @NotNull ASTNode reportErrorsOn) {
            OverloadResolutionResult iteratorResolutionResult = services.resolveNoParametersFunction(type, context.scope, "iterator");
            if (iteratorResolutionResult.isSuccess()) {
                JetType iteratorType = iteratorResolutionResult.getFunctionDescriptor().getUnsubstitutedReturnType();
                boolean hasNextFunctionSupported = checkHasNextFunctionSupport(reportErrorsOn, iteratorType);
                boolean hasNextPropertySupported = checkHasNextPropertySupport(reportErrorsOn, iteratorType);
                if (hasNextFunctionSupported && hasNextPropertySupported && !ErrorUtils.isErrorType(iteratorType)) {
                    // TODO : overload resolution rules impose priorities here???
                    context.trace.getErrorHandler().genericError(reportErrorsOn, "An ambiguity between 'iterator().hasNext()' function and 'iterator().hasNext()' property");
                }
                else if (!hasNextFunctionSupported && !hasNextPropertySupported) {
                    context.trace.getErrorHandler().genericError(reportErrorsOn, "Loop range must have an 'iterator().hasNext()' function or an 'iterator().hasNext' property");
                }

                OverloadResolutionResult nextResolutionResult = services.resolveNoParametersFunction(iteratorType, context.scope, "next");
                if (nextResolutionResult.isAmbiguity()) {
                    context.trace.getErrorHandler().genericError(reportErrorsOn, "Method 'iterator().next()' is ambiguous for this expression");
                } else if (nextResolutionResult.isNothing()) {
                    context.trace.getErrorHandler().genericError(reportErrorsOn, "Loop range must have an 'iterator().next()' method");
                } else {
                    return nextResolutionResult.getFunctionDescriptor().getUnsubstitutedReturnType();
                }
            }
            else {
                String errorMessage = "For-loop range must have an iterator() method";
                if (iteratorResolutionResult.isAmbiguity()) {
                    errorMessage = "Method 'iterator()' is ambiguous for this expression";
                }
                context.trace.getErrorHandler().genericError(reportErrorsOn, errorMessage);
            }
            return null;
        }

        private boolean checkHasNextFunctionSupport(@NotNull ASTNode reportErrorsOn, @NotNull JetType iteratorType) {
            OverloadResolutionResult hasNextResolutionResult = services.resolveNoParametersFunction(iteratorType, context.scope, "hasNext");
            if (hasNextResolutionResult.isAmbiguity()) {
                context.trace.getErrorHandler().genericError(reportErrorsOn, "Method 'iterator().hasNext()' is ambiguous for this expression");
            } else if (hasNextResolutionResult.isNothing()) {
                return false;
            } else {
                JetType hasNextReturnType = hasNextResolutionResult.getFunctionDescriptor().getUnsubstitutedReturnType();
                if (!isBoolean(hasNextReturnType)) {
                    context.trace.getErrorHandler().genericError(reportErrorsOn, "The 'iterator().hasNext()' method of the loop range must return Boolean, but returns " + hasNextReturnType);
                }
            }
            return true;
        }

        private boolean checkHasNextPropertySupport(@NotNull ASTNode reportErrorsOn, @NotNull JetType iteratorType) {
            VariableDescriptor hasNextProperty = iteratorType.getMemberScope().getVariable("hasNext");
            // TODO :extension properties
            if (hasNextProperty == null) {
                return false;
            } else {
                JetType hasNextReturnType = hasNextProperty.getOutType();
                if (hasNextReturnType == null) {
                    // TODO : accessibility
                    context.trace.getErrorHandler().genericError(reportErrorsOn, "The 'iterator().hasNext' property of the loop range must be readable");
                }
                else if (!isBoolean(hasNextReturnType)) {
                    context.trace.getErrorHandler().genericError(reportErrorsOn, "The 'iterator().hasNext' property of the loop range must return Boolean, but returns " + hasNextReturnType);
                }
            }
            return true;
        }

//        @Override
//        public void visitNewExpression(JetNewExpression expression) {
//            // TODO : type argument inference
//            JetTypeReference typeReference = expression.getTypeReference();
//            if (typeReference != null) {
//                result = checkTypeInitializerCall(scope, typeReference, expression);
//            }
//        }

//        private void resolveCallWithExplicitName(@NotNull JetScope scope, JetExpression receiver, String functionName, List<JetType> resolvedTypeArguments, List<ValueArgumentPsi> valueArguments) {
//
//            boolean someNamed = false;
//            boolean allNamed = true;
//            for (ValueArgumentPsi valueArgument : valueArguments) {
//                if (valueArgument.isNamed()) {
//                    context.trace.getErrorHandler().genericError(valueArgument.asElement().getNode(), "Named arguments are not supported");
//                    someNamed = true;
//                }
//                else {
//                    allNamed = false;
//                }
//            }
//            if (someNamed) {
//                return; // TODO
//            }
//            if (someNamed && !allNamed) {
//                // TODO function literals outside parentheses
//            }
//
//            ErrorHandlerWithRegions errorHandler = context.trace.getErrorHandler();
//
//            // 1. resolve 'receiver' in 'scope' with expected type 'NO_EXPECTED_TYPE'
//            errorHandler.openRegion();
//            JetType receiverType = JetTypeInferrer.this.getType(scope, receiver, false, NO_EXPECTED_TYPE);
//            // for each applicable function in 'receiverType'
//            Set<FunctionDescriptor> allFunctions = receiverType.getMemberScope().getFunctionGroup(functionName).getFunctionDescriptors();
//            Map<FunctionDescriptor, List<JetType>> applicableFunctions = Maps.newHashMap();
//            int typeArgCount = resolvedTypeArguments.size();
//            int valueArgCount = valueArguments.size();
//            for (FunctionDescriptor functionDescriptor : allFunctions) {
//                if (typeArgCount == 0 || functionDescriptor.getTypeParameters().size() == typeArgCount) {
//                    if (FunctionDescriptorUtil.getMinimumArity(functionDescriptor) <= valueArgCount &&
//                            valueArgCount <= FunctionDescriptorUtil.getMaximumArity(functionDescriptor)) {
//                        //   get expected types for value parameters
//                        if (typeArgCount > 0) {
//                            FunctionDescriptor substitutedFunctionDescriptor = FunctionDescriptorUtil.substituteFunctionDescriptor(resolvedTypeArguments, functionDescriptor);
//                        }
//                        else {
//                            FunctionDescriptor substitutedFunctionDescriptor = FunctionDescriptorUtil.substituteFunctionDescriptor(TypeUtils.getDefaultTypes(functionDescriptor.getTypeParameters()), functionDescriptor);
//                            List<JetType> valueArgumentTypes = getArgumentTypes(substitutedFunctionDescriptor, valueArguments);
//                            if (valueArgumentTypes == null) {
//                                Map<TypeConstructor, TypeProjection> noExpectedTypes = Maps.newHashMap();
//                                for (TypeParameterDescriptor typeParameterDescriptor : functionDescriptor.getTypeParameters()) {
//                                    noExpectedTypes.put(typeParameterDescriptor.getTypeConstructor(), new TypeProjection(NO_EXPECTED_TYPE));
//                                }
//                                substitutedFunctionDescriptor = functionDescriptor.substitute(TypeSubstitutor.create(noExpectedTypes));
//                                valueArgumentTypes = getArgumentTypes(substitutedFunctionDescriptor, valueArguments);
//                            }
//                            if (valueArgumentTypes != null) {
//                                List<JetType> typeArguments = solveConstraintSystem(functionDescriptor, valueArgumentTypes, expectedReturnType);
//                                if (typeArguments != null) {
//                                    applicableFunctions.put(functionDescriptor, typeArguments);
//                                }
//                            }
//                        }
//                        //   type-check the parameters
//                        // if something was found (one or many options
//                        errorHandler.closeAndCommitCurrentRegion();
//                        // otherwise
//                        errorHandler.closeAndReturnCurrentRegion();
//
//                    }
//                }
//            }
//            //   get expected types for value parameters
//            //   type-check the parameters
//            // if something was found (one or many options
//            errorHandler.closeAndCommitCurrentRegion();
//            // otherwise
//            errorHandler.closeAndReturnCurrentRegion();
//
//        }

        @Override
        public void visitHashQualifiedExpression(JetHashQualifiedExpression expression) {
            context.trace.getErrorHandler().genericError(expression.getOperationTokenNode(), "Unsupported");
        }

        @Override
        public void visitQualifiedExpression(JetQualifiedExpression expression) {
            // TODO : functions as values
            JetExpression selectorExpression = expression.getSelectorExpression();
            JetExpression receiverExpression = expression.getReceiverExpression();
            JetType receiverType = new TypeInferrerVisitorWithNamespaces(new TypeInferenceContext(context.trace, context.scope, false, context.dataFlowInfo, NO_EXPECTED_TYPE, null)).getType(receiverExpression);
            if (receiverType != null) {
                ErrorHandlerWithRegions errorHandler = context.trace.getErrorHandler();
                errorHandler.openRegion();
                JetType selectorReturnType = getSelectorReturnType(receiverType, selectorExpression);

                if (selectorReturnType != null) {
                    errorHandler.closeAndCommitCurrentRegion();
                }
                else {
                    ErrorHandlerWithRegions.DiagnosticsRegion regionToCommit = errorHandler.closeAndReturnCurrentRegion();

                    VariableDescriptor variableDescriptor = getVariableDescriptorFromSimpleName(receiverExpression);
                    if (variableDescriptor != null) {
                        List<JetType> possibleTypes = Lists.newArrayList(context.dataFlowInfo.getPossibleTypes(variableDescriptor));
                        Collections.reverse(possibleTypes);
                        for (JetType possibleType : possibleTypes) {
                            errorHandler.openRegion();
                            selectorReturnType = getSelectorReturnType(possibleType, selectorExpression);
                            if (selectorReturnType != null) {
                                regionToCommit = errorHandler.closeAndReturnCurrentRegion();
                                context.trace.recordAutoCast(receiverExpression, possibleType);
                                break;
                            }
                            else {
                                errorHandler.closeAndReturnCurrentRegion();
                            }
                        }
                    }

                    regionToCommit.commit();
                }

                if (expression.getOperationSign() == JetTokens.QUEST) {
                    if (selectorReturnType != null && !isBoolean(selectorReturnType) && selectorExpression != null) {
                        // TODO : more comprehensible error message
                        errorHandler.typeMismatch(selectorExpression, semanticServices.getStandardLibrary().getBooleanType(), selectorReturnType);
                    }
                    result = TypeUtils.makeNullable(receiverType);
                }
                else {
                    result = selectorReturnType;
                }
                if (selectorExpression != null && result != null) {
                    context.trace.recordExpressionType(selectorExpression, result);
                }
                if (selectorReturnType != null) {
                    // TODO : extensions to 'Any?'
                    if (selectorExpression != null) {
                        receiverType = enrichOutType(receiverExpression, receiverType);

                        services.checkNullSafety(receiverType, expression.getOperationTokenNode(), getCalleeFunctionDescriptor(selectorExpression));
                    }
                }
            }
        }

        private JetType enrichOutType(JetExpression receiverExpression, JetType receiverType) {
            VariableDescriptor variableDescriptor = getVariableDescriptorFromSimpleName(receiverExpression);
            if (variableDescriptor != null) {
                return context.dataFlowInfo.getOutType(variableDescriptor);
            }
            return receiverType;
        }

        @Nullable
        private VariableDescriptor getVariableDescriptorFromSimpleName(@NotNull JetExpression receiverExpression) {
            if (receiverExpression instanceof JetBinaryExpressionWithTypeRHS) {
                JetBinaryExpressionWithTypeRHS expression = (JetBinaryExpressionWithTypeRHS) receiverExpression;
                if (expression.getOperationSign().getReferencedNameElementType() == JetTokens.COLON) {
                    return getVariableDescriptorFromSimpleName(expression.getLeft());
                }
            }
            VariableDescriptor variableDescriptor = null;
            if (receiverExpression instanceof JetSimpleNameExpression) {
                JetSimpleNameExpression nameExpression = (JetSimpleNameExpression) receiverExpression;
                DeclarationDescriptor declarationDescriptor = context.trace.getBindingContext().resolveReferenceExpression(nameExpression);
                if (declarationDescriptor instanceof VariableDescriptor) {
                    variableDescriptor = (VariableDescriptor) declarationDescriptor;
                }
            }
            return variableDescriptor;
        }

        @NotNull
        private FunctionDescriptor getCalleeFunctionDescriptor(@NotNull JetExpression selectorExpression) {
            final FunctionDescriptor[] result = new FunctionDescriptor[1];
            selectorExpression.accept(new JetVisitor() {
                @Override
                public void visitCallExpression(JetCallExpression callExpression) {
                    JetExpression calleeExpression = callExpression.getCalleeExpression();
                    if (calleeExpression != null) {
                        calleeExpression.accept(this);
                    }
                }

                @Override
                public void visitReferenceExpression(JetReferenceExpression referenceExpression) {
                    DeclarationDescriptor declarationDescriptor = context.trace.getBindingContext().resolveReferenceExpression(referenceExpression);
                    if (declarationDescriptor instanceof FunctionDescriptor) {
                        result[0] = (FunctionDescriptor) declarationDescriptor;
                    }
                }

                @Override
                public void visitArrayAccessExpression(JetArrayAccessExpression expression) {
                    expression.getArrayExpression().accept(this);
                }

                @Override
                public void visitBinaryExpression(JetBinaryExpression expression) {
                    expression.getLeft().accept(this);
                }

                @Override
                public void visitQualifiedExpression(JetQualifiedExpression expression) {
                    expression.getReceiverExpression().accept(this);
                }

                @Override
                public void visitJetElement(JetElement element) {
                    context.trace.getErrorHandler().genericError(element.getNode(), "Unsupported [getCalleeFunctionDescriptor]: " + element);
                }
            });
            if (result[0] == null) {
                result[0] = ErrorUtils.createErrorFunction(0, Collections.<JetType>emptyList());
            }
            return result[0];
        }

        private JetType getCallExpressionType(@Nullable JetType receiverType, @NotNull JetCallExpression callExpression) {
            JetExpression calleeExpression = callExpression.getCalleeExpression();
            if (calleeExpression == null) {
                return null;
            }
            OverloadDomain overloadDomain = services.getOverloadDomain(receiverType, context.scope, calleeExpression, callExpression.getValueArgumentList());
            return services.resolveCall(context.scope, overloadDomain, callExpression);
        }

        private JetType getSelectorReturnType(JetType receiverType, JetExpression selectorExpression) {
            if (selectorExpression instanceof JetCallExpression) {
                return getCallExpressionType(receiverType, (JetCallExpression) selectorExpression);
            }
            else if (selectorExpression instanceof JetSimpleNameExpression) {
                JetScope compositeScope = new ScopeWithReceiver(context.scope, receiverType, semanticServices.getTypeChecker());
                return getType(compositeScope, selectorExpression, false);
            }
            else if (selectorExpression != null) {
                // TODO : not a simple name -> resolve in scope, expect property type or a function type
                context.trace.getErrorHandler().genericError(selectorExpression.getNode(), "Unsupported selector element type: " + selectorExpression);
            }
            return null;
        }

        @Override
        public void visitCallExpression(JetCallExpression expression) {
            result = getCallExpressionType(null, expression);
        }

        @Override
        public void visitIsExpression(JetIsExpression expression) {
            JetType knownType = getType(context.scope, expression.getLeftHandSide(), false);
            JetPattern pattern = expression.getPattern();
            if (pattern != null && knownType != null) {
                WritableScopeImpl scopeToExtend = newWritableScopeImpl().setDebugName("Scope extended in 'is'");
                DataFlowInfo newDataFlowInfo = checkPatternType(pattern, knownType, scopeToExtend, getVariableDescriptorFromSimpleName(expression.getLeftHandSide()));
                patternsToDataFlowInfo.put(pattern, newDataFlowInfo);
                patternsToBoundVariableLists.put(pattern, scopeToExtend.getDeclaredVariables());
            }
            result = semanticServices.getStandardLibrary().getBooleanType();
        }

        @Override
        public void visitUnaryExpression(JetUnaryExpression expression) {
            JetExpression baseExpression = expression.getBaseExpression();
            if (baseExpression == null) {
                return;
            }
            JetSimpleNameExpression operationSign = expression.getOperationSign();
            if (JetTokens.LABELS.contains(operationSign.getReferencedNameElementType())) {
                // TODO : Some processing for the label?
                result = getType(baseExpression);
                return;
            }
            IElementType operationType = operationSign.getReferencedNameElementType();
            String name = unaryOperationNames.get(operationType);
            if (name == null) {
                context.trace.getErrorHandler().genericError(operationSign.getNode(), "Unknown unary operation");
            }
            else {
                JetType receiverType = getType(context.scope, baseExpression, false);
                if (receiverType != null) {
                    FunctionDescriptor functionDescriptor = services.lookupFunction(context.scope, expression.getOperationSign(), name, receiverType, Collections.<JetType>emptyList(), true);
                    if (functionDescriptor != null) {
                        JetType returnType = functionDescriptor.getUnsubstitutedReturnType();
                        if (operationType == JetTokens.PLUSPLUS || operationType == JetTokens.MINUSMINUS) {
                            if (semanticServices.getTypeChecker().isSubtypeOf(returnType, JetStandardClasses.getUnitType())) {
                                 result = JetStandardClasses.getUnitType();
                            }
                            else {
                                if (!semanticServices.getTypeChecker().isSubtypeOf(returnType, receiverType)) {
                                    context.trace.getErrorHandler().genericError(operationSign.getNode(), name + " must return " + receiverType + " but returns " + returnType);
                                }
                                // TODO : Maybe returnType?
                                result = receiverType;
                            }
                        } else {
                            result = returnType;
                        }
                    }
                }
            }
        }

        @Override
        public void visitBinaryExpression(JetBinaryExpression expression) {
            JetSimpleNameExpression operationSign = expression.getOperationReference();

            JetExpression left = expression.getLeft();
            JetExpression right = expression.getRight();

            IElementType operationType = operationSign.getReferencedNameElementType();
            if (operationType == JetTokens.IDENTIFIER) {
                String referencedName = operationSign.getReferencedName();
                if (referencedName != null) {
                    result = getTypeForBinaryCall(expression, referencedName, context.scope, true);
                }
            }
            else if (binaryOperationNames.containsKey(operationType)) {
                result = getTypeForBinaryCall(expression, binaryOperationNames.get(operationType), context.scope, true);
            }
            else if (operationType == JetTokens.EQ) {
                visitAssignment(expression);
            }
            else if (assignmentOperationNames.containsKey(operationType)) {
                visitAssignmentOperation(expression);
            }
            else if (comparisonOperations.contains(operationType)) {
                JetType compareToReturnType = getTypeForBinaryCall(expression, "compareTo", context.scope, true);
                if (compareToReturnType != null) {
                    TypeConstructor constructor = compareToReturnType.getConstructor();
                    JetStandardLibrary standardLibrary = semanticServices.getStandardLibrary();
                    TypeConstructor intTypeConstructor = standardLibrary.getInt().getTypeConstructor();
                    if (constructor.equals(intTypeConstructor)) {
                        result = standardLibrary.getBooleanType();
                    } else {
                        context.trace.getErrorHandler().genericError(operationSign.getNode(), "compareTo must return Int, but returns " + compareToReturnType);
                    }
                }
            }
            else if (equalsOperations.contains(operationType)) {
                String name = "equals";
                if (right != null) {
                    JetType leftType = getType(context.scope, left, false);
                    if (leftType != null) {
                        JetType rightType = getType(context.scope, right, false);
                        if (rightType != null) {
                            FunctionDescriptor equals = services.lookupFunction(
                                    context.scope, operationSign, "equals",
                                    leftType, Collections.singletonList(JetStandardClasses.getNullableAnyType()), false);
                            if (equals != null) {
                                if (ensureBooleanResult(operationSign, name, equals.getUnsubstitutedReturnType())) {
                                    ensureNonemptyIntersectionOfOperandTypes(expression);
                                }
                            }
                            else {
                                context.trace.getErrorHandler().genericError(operationSign.getNode(), "No method 'equals(Any?) : Boolean' available");
                            }
                        }
                    }
                }
                result = semanticServices.getStandardLibrary().getBooleanType();
            }
            else if (operationType == JetTokens.EQEQEQ || operationType == JetTokens.EXCLEQEQEQ) {
                ensureNonemptyIntersectionOfOperandTypes(expression);

                // TODO : Check comparison pointlessness
                result = semanticServices.getStandardLibrary().getBooleanType();
            }
            else if (inOperations.contains(operationType)) {
                if (right == null) {
                    result = ErrorUtils.createErrorType("No right argument"); // TODO
                    return;
                }
                checkInExpression(operationSign, left, right);
                result = semanticServices.getStandardLibrary().getBooleanType();
            }
            else if (operationType == JetTokens.ANDAND || operationType == JetTokens.OROR) {
                JetType leftType = getType(context.scope, left, false);
                WritableScopeImpl leftScope = newWritableScopeImpl().setDebugName("Left scope of && or ||");
                DataFlowInfo flowInfoLeft = extractDataFlowInfoFromCondition(left, operationType == JetTokens.ANDAND, leftScope);  // TODO: This gets computed twice: here and in extractDataFlowInfoFromCondition() for the whole condition
                WritableScopeImpl rightScope = operationType == JetTokens.ANDAND ? leftScope : newWritableScopeImpl().setDebugName("Right scope of && or ||");
                JetType rightType = right == null ? null : getTypeWithNewDataFlowInfo(rightScope, right, false, flowInfoLeft);
                if (leftType != null && !isBoolean(leftType)) {
                    context.trace.getErrorHandler().typeMismatch(left, semanticServices.getStandardLibrary().getBooleanType(), leftType);
                }
                if (rightType != null && !isBoolean(rightType)) {
                    context.trace.getErrorHandler().typeMismatch(right, semanticServices.getStandardLibrary().getBooleanType(), rightType);
                }
                result = semanticServices.getStandardLibrary().getBooleanType();
            }
            else if (operationType == JetTokens.ELVIS) {
                JetType leftType = getType(context.scope, left, false);
                JetType rightType = right == null ? null : getType(context.scope, right, false);
                if (leftType != null) {
                    if (!leftType.isNullable()) {
                        context.trace.getErrorHandler().genericWarning(left.getNode(), "Elvis operator (?:) is always returns the left operand of non-nullable type " + leftType);
                    }
                    if (rightType != null) {
                        result = TypeUtils.makeNullableAsSpecified(semanticServices.getTypeChecker().commonSupertype(leftType, rightType), rightType.isNullable());
                    }
                }
            }
            else {
                context.trace.getErrorHandler().genericError(operationSign.getNode(), "Unknown operation");
            }
        }

        private void checkInExpression(JetSimpleNameExpression operationSign, JetExpression left, JetExpression right) {
            String name = "contains";
            JetType containsType = getTypeForBinaryCall(context.scope, right, operationSign, left, name, true);
            ensureBooleanResult(operationSign, name, containsType);
        }

        private void ensureNonemptyIntersectionOfOperandTypes(JetBinaryExpression expression) {
            JetSimpleNameExpression operationSign = expression.getOperationReference();
            JetExpression left = expression.getLeft();
            JetExpression right = expression.getRight();

            // TODO : duplicated effort for == and !=
            JetType leftType = getType(context.scope, left, false);
            if (leftType != null && right != null) {
                JetType rightType = getType(context.scope, right, false);

                if (rightType != null) {
                    JetType intersect = TypeUtils.intersect(semanticServices.getTypeChecker(), new HashSet<JetType>(Arrays.asList(leftType, rightType)));
                    if (intersect == null) {
                        context.trace.getErrorHandler().genericError(expression.getNode(), "Operator " + operationSign.getReferencedName() + " cannot be applied to " + leftType + " and " + rightType);
                    }
                }
            }
        }

        protected void visitAssignmentOperation(JetBinaryExpression expression) {
            assignmentIsNotAnExpressionError(expression);
        }

        protected void visitAssignment(JetBinaryExpression expression) {
            assignmentIsNotAnExpressionError(expression);
        }

        private void assignmentIsNotAnExpressionError(JetBinaryExpression expression) {
            context.trace.getErrorHandler().genericError(expression.getNode(), "Assignments are not expressions, and only expressions are allowed in this context");
        }

        private boolean ensureBooleanResult(JetExpression operationSign, String name, JetType resultType) {
            return ensureBooleanResultWithCustomSubject(operationSign, resultType, "'" + name + "'");
        }

        private boolean ensureBooleanResultWithCustomSubject(JetExpression operationSign, JetType resultType, String subjectName) {
            if (resultType != null) {
                // TODO : Relax?
                if (!isBoolean(resultType)) {
                    context.trace.getErrorHandler().genericError(operationSign.getNode(), subjectName + " must return Boolean but returns " + resultType);
                    return false;
                }
            }
            return true;
        }

        private boolean isBoolean(@NotNull JetType type) {
            return semanticServices.getTypeChecker().isConvertibleTo(type,  semanticServices.getStandardLibrary().getBooleanType());
        }

        @Nullable
        protected List<JetType> getTypes(JetScope scope, List<JetExpression> indexExpressions) {
            List<JetType> argumentTypes = new ArrayList<JetType>();
            TypeInferrerVisitor typeInferrerVisitor = new TypeInferrerVisitor(new TypeInferenceContext(context.trace, scope, false, context.dataFlowInfo, NO_EXPECTED_TYPE, null));
            for (JetExpression indexExpression : indexExpressions) {
                JetType type = typeInferrerVisitor.getType(indexExpression);
                if (type == null) {
                    return null;
                }
                argumentTypes.add(type);
                typeInferrerVisitor.resetResult(); // TODO : recreate?
            }
            return argumentTypes;
        }

        @Override
        public void visitArrayAccessExpression(JetArrayAccessExpression expression) {
            JetExpression arrayExpression = expression.getArrayExpression();
            JetType receiverType = getType(context.scope, arrayExpression, false);
            List<JetExpression> indexExpressions = expression.getIndexExpressions();
            List<JetType> argumentTypes = getTypes(context.scope, indexExpressions);
            if (argumentTypes == null) return;

            if (receiverType != null) {
                FunctionDescriptor functionDescriptor = services.lookupFunction(context.scope, expression, "get", receiverType, argumentTypes, true);
                if (functionDescriptor != null) {
//                    checkNullSafety(receiverType, expression.getIndexExpressions().get(0).getNode(), functionDescriptor);
                    result = functionDescriptor.getUnsubstitutedReturnType();
                }
            }
        }

        @Nullable
        protected JetType getTypeForBinaryCall(
                @NotNull JetBinaryExpression expression,
                @NotNull String name,
                @NotNull JetScope scope,
                boolean reportUnresolved) {
            JetExpression left = expression.getLeft();
            JetExpression right = expression.getRight();
            if (right == null) {
                return null;
            }
            JetSimpleNameExpression operationSign = expression.getOperationReference();
            return getTypeForBinaryCall(scope, left, operationSign, right, name, reportUnresolved);
        }

        @Nullable
        private JetType getTypeForBinaryCall(
                @NotNull JetScope scope,
                @NotNull JetExpression left,
                @NotNull JetSimpleNameExpression operationSign,
                @NotNull JetExpression right,
                @NotNull String name,
                boolean reportUnresolved) {
            JetType leftType = getType(scope, left, false);
            JetType rightType = getType(scope, right, false);
            if (leftType == null || rightType == null) {
                return null;
            }
            FunctionDescriptor functionDescriptor = services.lookupFunction(scope, operationSign, name, leftType, Collections.singletonList(rightType), reportUnresolved);
            if (functionDescriptor != null) {
                if (leftType.isNullable()) {
                    // TODO : better error message for '1 + nullableVar' case
                    context.trace.getErrorHandler().genericError(operationSign.getNode(),
                            "Infix call corresponds to a dot-qualified call '" +
                            left.getText() + "." + name + "(" + right.getText() + ")'" +
                            " which is not allowed on a nullable receiver '" + right.getText() + "'." +
                            " Use '?.'-qualified call instead");
                }

                return functionDescriptor.getUnsubstitutedReturnType();
            }
            return null;
        }

        @Override
        public void visitDeclaration(JetDeclaration dcl) {
            context.trace.getErrorHandler().genericError(dcl.getNode(), "Declarations are not allowed in this position");
        }

        @Override
        public void visitRootNamespaceExpression(JetRootNamespaceExpression expression) {
            context.trace.getErrorHandler().genericError(expression.getNode(), "'namespace' is not an expression");
            result = null;
        }

        @Override
        public void visitJetElement(JetElement element) {
            context.trace.getErrorHandler().genericError(element.getNode(), "[JetTypeInferrer] Unsupported element: " + element + " " + element.getClass().getCanonicalName());
        }
    }

    private class TypeInferrerVisitorWithNamespaces extends TypeInferrerVisitor {
        private TypeInferrerVisitorWithNamespaces(@NotNull TypeInferenceContext context) {
            super(context);
        }

        @NotNull
        @Override
        public TypeInferrerVisitor createNew(@NotNull TypeInferenceContext context) {
            return new TypeInferrerVisitorWithNamespaces(context);
        }

        @Override
        public boolean isNamespacePosition() {
            return true;
        }

        @Override
        public void visitRootNamespaceExpression(JetRootNamespaceExpression expression) {
            result = JetModuleUtil.getRootNamespaceType(expression);
        }

        @Override
        protected boolean furtherNameLookup(@NotNull JetSimpleNameExpression expression, @NotNull String referencedName) {
            result = lookupNamespaceType(expression, referencedName);
            return result != null;
        }

    }

    private class TypeInferrerVisitorWithWritableScope extends TypeInferrerVisitor {
        private final WritableScope scope;

        public TypeInferrerVisitorWithWritableScope(@NotNull WritableScope scope, @NotNull TypeInferenceContext context) {
            super(context);
            this.scope = scope;
        }

        @NotNull
        @Override
        public TypeInferrerVisitor createNew(@NotNull TypeInferenceContext context) {
            WritableScopeImpl writableScope = newWritableScopeImpl(context.scope).setDebugName("Block scope");
            return new TypeInferrerVisitorWithWritableScope(writableScope, context);
        }

        @Override
        public void visitObjectDeclaration(JetObjectDeclaration declaration) {
            TopDownAnalyzer topDownAnalyzer = new TopDownAnalyzer(semanticServices, context.trace);
            topDownAnalyzer.processObject(scope, scope.getContainingDeclaration(), declaration);
            ClassDescriptor classDescriptor = context.trace.getBindingContext().getClassDescriptor(declaration);
            if (classDescriptor != null) {
                PropertyDescriptor propertyDescriptor = context.classDescriptorResolver.resolveObjectDeclarationAsPropertyDescriptor(scope.getContainingDeclaration(), declaration, classDescriptor);
                scope.addVariableDescriptor(propertyDescriptor);
            }
        }

        @Override
        public void visitProperty(JetProperty property) {

            JetTypeReference receiverTypeRef = property.getReceiverTypeRef();
            if (receiverTypeRef != null) {
                context.trace.getErrorHandler().genericError(receiverTypeRef.getNode(), "Local receiver-properties are not allowed");
            }

            JetPropertyAccessor getter = property.getGetter();
            if (getter != null) {
                context.trace.getErrorHandler().genericError(getter.getNode(), "Local variables are not allowed to have getters");
            }

            JetPropertyAccessor setter = property.getSetter();
            if (setter != null) {
                context.trace.getErrorHandler().genericError(setter.getNode(), "Local variables are not allowed to have setters");
            }

            VariableDescriptor propertyDescriptor = context.classDescriptorResolver.resolveLocalVariableDescriptor(scope.getContainingDeclaration(), scope, property);
            JetExpression initializer = property.getInitializer();
            if (property.getPropertyTypeRef() != null && initializer != null) {
                JetType initializerType = getType(scope, initializer, false);
                JetType outType = propertyDescriptor.getOutType();
                if (outType != null &&
                    initializerType != null &&
                    !semanticServices.getTypeChecker().isConvertibleTo(initializerType, outType)) {
                    context.trace.getErrorHandler().typeMismatch(initializer, outType, initializerType);
                }
            }

            scope.addVariableDescriptor(propertyDescriptor);
        }

        @Override
        public void visitFunction(JetFunction function) {
            scope.addFunctionDescriptor(context.classDescriptorResolver.resolveFunctionDescriptor(scope.getContainingDeclaration(), scope, function));
        }

        @Override
        public void visitClass(JetClass klass) {
            super.visitClass(klass); // TODO
        }

        @Override
        public void visitExtension(JetExtension extension) {
            super.visitExtension(extension); // TODO
        }

        @Override
        public void visitTypedef(JetTypedef typedef) {
            super.visitTypedef(typedef); // TODO
        }

        @Override
        public void visitDeclaration(JetDeclaration dcl) {
            visitJetElement(dcl);
        }

        @Override
        protected void visitAssignmentOperation(JetBinaryExpression expression) {
            IElementType operationType = expression.getOperationReference().getReferencedNameElementType();
            String name = assignmentOperationNames.get(operationType);
            JetType assignmentOperationType = getTypeForBinaryCall(expression, name, scope, false);

            if (assignmentOperationType == null) {
                String counterpartName = binaryOperationNames.get(assignmentOperationCounterparts.get(operationType));
                getTypeForBinaryCall(expression, counterpartName, scope, true);
            }
            result = null; // not an expression
        }

        @Override
        protected void visitAssignment(JetBinaryExpression expression) {
            JetExpression left = expression.getLeft();
            JetExpression deparenthesized = JetPsiUtil.deparenthesize(left);
            JetExpression right = expression.getRight();
            if (deparenthesized instanceof JetArrayAccessExpression) {
                JetArrayAccessExpression arrayAccessExpression = (JetArrayAccessExpression) deparenthesized;
                resolveArrayAccessToLValue(arrayAccessExpression, right, expression.getOperationReference());
            }
            else {
                JetType leftType = getType(scope, left, false);
                if (right != null) {
                    JetType rightType = getType(scope, right, false);
                    if (rightType != null &&
                        leftType != null &&
                            !semanticServices.getTypeChecker().isConvertibleTo(rightType, leftType)) {
                        context.trace.getErrorHandler().typeMismatch(right, leftType, rightType);
                    }
                }
            }
            result = null; // This is not an element
        }

        private void resolveArrayAccessToLValue(JetArrayAccessExpression arrayAccessExpression, JetExpression rightHandSide, JetSimpleNameExpression operationSign) {
            List<JetType> argumentTypes = getTypes(scope, arrayAccessExpression.getIndexExpressions());
            if (argumentTypes == null) return;
            JetType rhsType = getType(scope, rightHandSide, false);
            if (rhsType == null) return;
            argumentTypes.add(rhsType);

            JetType receiverType = getType(scope, arrayAccessExpression.getArrayExpression(), false);
            if (receiverType == null) return;

            // TODO : nasty hack: effort is duplicated
            services.lookupFunction(scope, arrayAccessExpression, "set", receiverType, argumentTypes, true);
            FunctionDescriptor functionDescriptor = services.lookupFunction(scope, operationSign, "set", receiverType, argumentTypes, true);
            if (functionDescriptor != null) {
                result = functionDescriptor.getUnsubstitutedReturnType();
            }
        }

        @Override
        public void visitJetElement(JetElement element) {
            context.trace.getErrorHandler().genericError(element.getNode(), "Unsupported element in a block: " + element + " " + element.getClass().getCanonicalName());
        }
    }
}
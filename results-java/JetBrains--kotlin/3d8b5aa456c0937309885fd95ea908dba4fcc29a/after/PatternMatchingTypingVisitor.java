package org.jetbrains.jet.lang.types.expressions;

import com.google.common.collect.Sets;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.VariableDescriptor;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.calls.AutoCastUtils;
import org.jetbrains.jet.lang.resolve.calls.DataFlowInfo;
import org.jetbrains.jet.lang.resolve.scopes.WritableScope;
import org.jetbrains.jet.lang.resolve.scopes.WritableScopeImpl;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ExpressionReceiver;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverDescriptor;
import org.jetbrains.jet.lang.resolve.scopes.receivers.TransientReceiver;
import org.jetbrains.jet.lang.types.*;

import java.util.List;
import java.util.Set;

import static org.jetbrains.jet.lang.diagnostics.Errors.*;
import static org.jetbrains.jet.lang.diagnostics.Errors.INCOMPATIBLE_TYPES;
import static org.jetbrains.jet.lang.diagnostics.Errors.UNSUPPORTED;
import static org.jetbrains.jet.lang.types.expressions.ExpressionTypingUtils.ensureBooleanResultWithCustomSubject;
import static org.jetbrains.jet.lang.types.expressions.ExpressionTypingUtils.newWritableScopeImpl;

/**
 * @author abreslav
 */
public class PatternMatchingTypingVisitor extends ExpressionTypingVisitor {
    protected PatternMatchingTypingVisitor(@NotNull ExpressionTypingInternals facade) {
        super(facade);
    }

    @Override
    public JetType visitIsExpression(JetIsExpression expression, ExpressionTypingContext contextWithExpectedType) {
        ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(TypeUtils.NO_EXPECTED_TYPE);
        JetType knownType = facade.safeGetType(expression.getLeftHandSide(), context.replaceScope(context.scope));
        JetPattern pattern = expression.getPattern();
        if (pattern != null) {
            WritableScopeImpl scopeToExtend = newWritableScopeImpl(context).setDebugName("Scope extended in 'is'");
            DataFlowInfo newDataFlowInfo = checkPatternType(pattern, knownType, scopeToExtend, context, AutoCastUtils.getVariableDescriptorFromSimpleName(context.trace.getBindingContext(), expression.getLeftHandSide()));
            context.patternsToDataFlowInfo.put(pattern, newDataFlowInfo);
            context.patternsToBoundVariableLists.put(pattern, scopeToExtend.getDeclaredVariables());
        }
        return ExpressionTypingUtils.checkType(context.semanticServices.getStandardLibrary().getBooleanType(), expression, contextWithExpectedType);
    }

    @Override
    public JetType visitWhenExpression(final JetWhenExpression expression, ExpressionTypingContext contextWithExpectedType) {
        ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(TypeUtils.NO_EXPECTED_TYPE);
        // TODO :change scope according to the bound value in the when header
        final JetExpression subjectExpression = expression.getSubjectExpression();

        final JetType subjectType = subjectExpression != null ? context.getServices().safeGetType(context.scope, subjectExpression, TypeUtils.NO_EXPECTED_TYPE) : ErrorUtils.createErrorType("Unknown type");
        final VariableDescriptor variableDescriptor = subjectExpression != null ? AutoCastUtils.getVariableDescriptorFromSimpleName(context.trace.getBindingContext(), subjectExpression) : null;

        // TODO : exhaustive patterns

        Set<JetType> expressionTypes = Sets.newHashSet();
        for (JetWhenEntry whenEntry : expression.getEntries()) {
            JetWhenCondition[] conditions = whenEntry.getConditions();
            DataFlowInfo newDataFlowInfo;
            WritableScope scopeToExtend;
            if (conditions.length == 1) {
                scopeToExtend = newWritableScopeImpl(context).setDebugName("Scope extended in when entry");
                newDataFlowInfo = context.dataFlowInfo;
                JetWhenCondition condition = conditions[0];
                if (condition != null) {
                    newDataFlowInfo = checkWhenCondition(subjectExpression, subjectType, condition, scopeToExtend, context, variableDescriptor);
                }
            }
            else {
                scopeToExtend = newWritableScopeImpl(context); // We don't write to this scope
                newDataFlowInfo = null;
                for (JetWhenCondition condition : conditions) {
                    DataFlowInfo dataFlowInfo = checkWhenCondition(subjectExpression, subjectType, condition, newWritableScopeImpl(context), context, variableDescriptor);
                    if (newDataFlowInfo == null) {
                        newDataFlowInfo = dataFlowInfo;
                    }
                    else {
                        newDataFlowInfo = newDataFlowInfo.or(dataFlowInfo);
                    }
                }
                if (newDataFlowInfo == null) {
                    newDataFlowInfo = context.dataFlowInfo;
                }
                else {
                    newDataFlowInfo = newDataFlowInfo.and(context.dataFlowInfo);
                }
            }
            JetExpression bodyExpression = whenEntry.getExpression();
            if (bodyExpression != null) {
                JetType type = facade.getType(bodyExpression, contextWithExpectedType.replaceScope(scopeToExtend).replaceDataFlowInfo(newDataFlowInfo));
                if (type != null) {
                    expressionTypes.add(type);
                }
            }
        }

        if (!expressionTypes.isEmpty()) {
            return context.semanticServices.getTypeChecker().commonSupertype(expressionTypes);
        }
        else if (expression.getEntries().isEmpty()) {
//                context.trace.getErrorHandler().genericError(expression.getNode(), "Entries required for when-expression");
            context.trace.report(NO_WHEN_ENTRIES.on(expression));
        }
        return null;
    }

    private DataFlowInfo checkWhenCondition(@Nullable final JetExpression subjectExpression, final JetType subjectType, JetWhenCondition condition, final WritableScope scopeToExtend, final ExpressionTypingContext context, final VariableDescriptor... subjectVariables) {
        final DataFlowInfo[] newDataFlowInfo = new DataFlowInfo[]{context.dataFlowInfo};
        condition.accept(new JetVisitorVoid() {

            @Override
            public void visitWhenConditionCall(JetWhenConditionCall condition) {
                JetExpression callSuffixExpression = condition.getCallSuffixExpression();
//                    JetScope compositeScope = new ScopeWithReceiver(context.scope, subjectType, semanticServices.getTypeChecker());
                if (callSuffixExpression != null) {
//                        JetType selectorReturnType = getType(compositeScope, callSuffixExpression, false, context);
                    assert subjectExpression != null;
                    JetType selectorReturnType = facade.getSelectorReturnType(new ExpressionReceiver(subjectExpression, subjectType), condition.getOperationTokenNode(), callSuffixExpression, context);//getType(compositeScope, callSuffixExpression, false, context);
                    ensureBooleanResultWithCustomSubject(callSuffixExpression, selectorReturnType, "This expression", context);
//                        context.getServices().checkNullSafety(subjectType, condition.getOperationTokenNode(), getCalleeFunctionDescriptor(callSuffixExpression, context), condition);
                }
            }

            @Override
            public void visitWhenConditionInRange(JetWhenConditionInRange condition) {
                JetExpression rangeExpression = condition.getRangeExpression();
                if (rangeExpression != null) {
                    assert subjectExpression != null;
                    facade.checkInExpression(condition, condition.getOperationReference(), subjectExpression, rangeExpression, context);
                }
            }

            @Override
            public void visitWhenConditionIsPattern(JetWhenConditionIsPattern condition) {
                JetPattern pattern = condition.getPattern();
                if (pattern != null) {
                    newDataFlowInfo[0] = checkPatternType(pattern, subjectType, scopeToExtend, context, subjectVariables);
                }
            }

            @Override
            public void visitJetElement(JetElement element) {
//                    context.trace.getErrorHandler().genericError(element.getNode(), "Unsupported [OperatorConventions] : " + element);
                context.trace.report(UNSUPPORTED.on(element, getClass().getCanonicalName()));
            }
        });
        return newDataFlowInfo[0];
    }

    private DataFlowInfo checkPatternType(@NotNull JetPattern pattern, @NotNull final JetType subjectType, @NotNull final WritableScope scopeToExtend, final ExpressionTypingContext context, @NotNull final VariableDescriptor... subjectVariables) {
        final DataFlowInfo[] result = new DataFlowInfo[] {context.dataFlowInfo};
        pattern.accept(new JetVisitorVoid() {
            @Override
            public void visitTypePattern(JetTypePattern typePattern) {
                JetTypeReference typeReference = typePattern.getTypeReference();
                if (typeReference != null) {
                    JetType type = context.getTypeResolver().resolveType(context.scope, typeReference);
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
//                        context.trace.getErrorHandler().genericError(pattern.getNode(), "Type mismatch: subject is of type " + subjectType + " but the pattern is of type Tuple" + entries.size());
                    context.trace.report(TYPE_MISMATCH_IN_TUPLE_PATTERN.on(pattern, subjectType, entries.size()));
                }
                else {
                    for (int i = 0, entriesSize = entries.size(); i < entriesSize; i++) {
                        JetTuplePatternEntry entry = entries.get(i);
                        JetType type = subjectType.getArguments().get(i).getType();

                        // TODO : is a name always allowed, ie for tuple patterns, not decomposer arg lists?
                        ASTNode nameLabelNode = entry.getNameLabelNode();
                        if (nameLabelNode != null) {
//                                context.trace.getErrorHandler().genericError(nameLabelNode, "Unsupported [OperatorConventions]");
                            context.trace.report(UNSUPPORTED.on(nameLabelNode, getClass().getCanonicalName()));
                        }

                        JetPattern entryPattern = entry.getPattern();
                        if (entryPattern != null) {
                            result[0] = result[0].and(checkPatternType(entryPattern, type, scopeToExtend, context));
                        }
                    }
                }
            }

            @Override
            public void visitDecomposerPattern(JetDecomposerPattern pattern) {
                JetExpression decomposerExpression = pattern.getDecomposerExpression();
                if (decomposerExpression != null) {
                    ReceiverDescriptor receiver = new TransientReceiver(subjectType);
                    JetType selectorReturnType = facade.getSelectorReturnType(receiver, null, decomposerExpression, context);

                    result[0] = checkPatternType(pattern.getArgumentList(), selectorReturnType == null ? ErrorUtils.createErrorType("No type") : selectorReturnType, scopeToExtend, context);
                }
            }

            @Override
            public void visitWildcardPattern(JetWildcardPattern pattern) {
                // Nothing
            }

            @Override
            public void visitExpressionPattern(JetExpressionPattern pattern) {
                JetExpression expression = pattern.getExpression();
                if (expression != null) {
                    JetType type = facade.getType(expression, context.replaceScope(scopeToExtend));
                    checkTypeCompatibility(type, subjectType, pattern);
                }
            }

            @Override
            public void visitBindingPattern(JetBindingPattern pattern) {
                JetProperty variableDeclaration = pattern.getVariableDeclaration();
                JetTypeReference propertyTypeRef = variableDeclaration.getPropertyTypeRef();
                JetType type = propertyTypeRef == null ? subjectType : context.getTypeResolver().resolveType(context.scope, propertyTypeRef);
                VariableDescriptor variableDescriptor = context.getClassDescriptorResolver().resolveLocalVariableDescriptorWithType(context.scope.getContainingDeclaration(), variableDeclaration, type);
                scopeToExtend.addVariableDescriptor(variableDescriptor);
                if (propertyTypeRef != null) {
                    if (!context.semanticServices.getTypeChecker().isSubtypeOf(subjectType, type)) {
//                            context.trace.getErrorHandler().genericError(propertyTypeRef.getNode(), type + " must be a supertype of " + subjectType + ". Use 'is' to match against " + type);
                        context.trace.report(TYPE_MISMATCH_IN_BINDING_PATTERN.on(propertyTypeRef, type, subjectType));
                    }
                }

                JetWhenCondition condition = pattern.getCondition();
                if (condition != null) {
                    int oldLength = subjectVariables.length;
                    VariableDescriptor[] newSubjectVariables = new VariableDescriptor[oldLength + 1];
                    System.arraycopy(subjectVariables, 0, newSubjectVariables, 0, oldLength);
                    newSubjectVariables[oldLength] = variableDescriptor;
                    result[0] = checkWhenCondition(null, subjectType, condition, scopeToExtend, context, newSubjectVariables);
                }
            }

            private void checkTypeCompatibility(@Nullable JetType type, @NotNull JetType subjectType, @NotNull JetElement reportErrorOn) {
                // TODO : Take auto casts into account?
                if (type == null) {
                    return;
                }
                if (TypeUtils.intersect(context.semanticServices.getTypeChecker(), Sets.newHashSet(type, subjectType)) == null) {
//                        context.trace.getErrorHandler().genericError(reportErrorOn.getNode(), "Incompatible types: " + type + " and " + subjectType);
                    context.trace.report(INCOMPATIBLE_TYPES.on(reportErrorOn, type, subjectType));
                }
            }

            @Override
            public void visitJetElement(JetElement element) {
//                    context.trace.getErrorHandler().genericError(element.getNode(), "Unsupported [OperatorConventions]");
                context.trace.report(UNSUPPORTED.on(element, getClass().getCanonicalName()));
            }
        });
        return result[0];
    }

}
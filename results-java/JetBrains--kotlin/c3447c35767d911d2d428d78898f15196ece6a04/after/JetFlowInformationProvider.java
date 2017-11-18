package org.jetbrains.jet.lang.cfg;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.cfg.pseudocode.*;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.descriptors.VariableDescriptor;
import org.jetbrains.jet.lang.diagnostics.Errors;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.BindingContextUtils;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.types.JetStandardClasses;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lexer.JetTokens;

import java.util.*;

import static org.jetbrains.jet.lang.diagnostics.Errors.*;
import static org.jetbrains.jet.lang.types.TypeUtils.NO_EXPECTED_TYPE;

/**
 * @author svtk
 */
public class JetFlowInformationProvider {

    private final Map<JetElement, Pseudocode> pseudocodeMap;
    private BindingTrace trace;

    public JetFlowInformationProvider(@NotNull JetDeclaration declaration, @NotNull final JetExpression bodyExpression, @NotNull JetControlFlowDataTraceFactory flowDataTraceFactory, @NotNull BindingTrace trace) {
        this.trace = trace;
        final JetPseudocodeTrace pseudocodeTrace = flowDataTraceFactory.createTrace(declaration);
        pseudocodeMap = new HashMap<JetElement, Pseudocode>();
        final Map<JetElement, Instruction> representativeInstructions = new HashMap<JetElement, Instruction>();
        final Map<JetExpression, LoopInfo> loopInfo = Maps.newHashMap();
        JetPseudocodeTrace wrappedTrace = new JetPseudocodeTrace() {
            @Override
            public void recordControlFlowData(@NotNull JetElement element, @NotNull Pseudocode pseudocode) {
                pseudocodeTrace.recordControlFlowData(element, pseudocode);
                pseudocodeMap.put(element, pseudocode);
            }

            @Override
            public void recordRepresentativeInstruction(@NotNull JetElement element, @NotNull Instruction instruction) {
                Instruction oldValue = representativeInstructions.put(element, instruction);
//                assert oldValue == null : element.getText();
                pseudocodeTrace.recordRepresentativeInstruction(element, instruction);
            }

            @Override
            public void recordLoopInfo(JetExpression expression, LoopInfo blockInfo) {
                loopInfo.put(expression, blockInfo);
                pseudocodeTrace.recordLoopInfo(expression, blockInfo);
            }

            @Override
            public void close() {
                pseudocodeTrace.close();
                for (Pseudocode pseudocode : pseudocodeMap.values()) {
                    pseudocode.postProcess();
                }
            }
        };
        JetControlFlowInstructionsGenerator instructionsGenerator = new JetControlFlowInstructionsGenerator(wrappedTrace);
        new JetControlFlowProcessor(trace, instructionsGenerator).generate(declaration, bodyExpression);
        wrappedTrace.close();
    }

    private void collectReturnExpressions(@NotNull JetElement subroutine, @NotNull final Collection<JetExpression> returnedExpressions) {
        Pseudocode pseudocode = pseudocodeMap.get(subroutine);
        assert pseudocode != null;

        final Set<Instruction> instructions = Sets.newHashSet(pseudocode.getInstructions());
        SubroutineExitInstruction exitInstruction = pseudocode.getExitInstruction();
        for (Instruction previousInstruction : exitInstruction.getPreviousInstructions()) {
            previousInstruction.accept(new InstructionVisitor() {
                @Override
                public void visitReturnValue(ReturnValueInstruction instruction) {
                    if (instructions.contains(instruction)) { //exclude non-local return expressions
                        returnedExpressions.add((JetExpression) instruction.getElement());
                    }
                }

                @Override
                public void visitReturnNoValue(ReturnNoValueInstruction instruction) {
                    if (instructions.contains(instruction)) {
                        returnedExpressions.add((JetExpression) instruction.getElement());
                    }
                }


                @Override
                public void visitJump(AbstractJumpInstruction instruction) {
                    // Nothing
                }

                @Override
                public void visitUnconditionalJump(UnconditionalJumpInstruction instruction) {
                    redirectToPrevInstructions(instruction);
                }

                private void redirectToPrevInstructions(Instruction instruction) {
                    for (Instruction previousInstruction : instruction.getPreviousInstructions()) {
                        previousInstruction.accept(this);
                    }
                }

                @Override
                public void visitNondeterministicJump(NondeterministicJumpInstruction instruction) {
                    redirectToPrevInstructions(instruction);
                }

                @Override
                public void visitInstruction(Instruction instruction) {
                    if (instruction instanceof JetElementInstruction) {
                        JetElementInstruction elementInstruction = (JetElementInstruction) instruction;
                        returnedExpressions.add((JetExpression) elementInstruction.getElement());
                    }
                    else {
                        throw new IllegalStateException(instruction + " precedes the exit point");
                    }
                }
            });
        }
    }

    public void checkDefiniteReturn(@NotNull JetDeclarationWithBody function, final @NotNull JetType expectedReturnType) {
        assert function instanceof JetDeclaration;

        JetExpression bodyExpression = function.getBodyExpression();
        if (bodyExpression == null) return;

        List<JetExpression> returnedExpressions = Lists.newArrayList();
        collectReturnExpressions(function.asElement(), returnedExpressions);

        boolean nothingReturned = returnedExpressions.isEmpty();

        returnedExpressions.remove(function); // This will be the only "expression" if the body is empty

        if (expectedReturnType != NO_EXPECTED_TYPE && !JetStandardClasses.isUnit(expectedReturnType) && returnedExpressions.isEmpty() && !nothingReturned) {
            trace.report(RETURN_TYPE_MISMATCH.on(bodyExpression, expectedReturnType));
        }
        final boolean blockBody = function.hasBlockBody();

        final Set<JetElement> rootUnreachableElements = collectUnreachableCode(function.asElement());
        for (JetElement element : rootUnreachableElements) {
            trace.report(UNREACHABLE_CODE.on(element));
        }

        for (JetExpression returnedExpression : returnedExpressions) {
            returnedExpression.accept(new JetVisitorVoid() {
                @Override
                public void visitReturnExpression(JetReturnExpression expression) {
                    if (!blockBody) {
                        trace.report(RETURN_IN_FUNCTION_WITH_EXPRESSION_BODY.on(expression));
                    }
                }

                @Override
                public void visitExpression(JetExpression expression) {
                    if (blockBody && expectedReturnType != NO_EXPECTED_TYPE && !JetStandardClasses.isUnit(expectedReturnType) && !rootUnreachableElements.contains(expression)) {
                        trace.report(NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY.on(expression));
                    }
                }
            });
        }
    }

    private Set<JetElement> collectUnreachableCode(@NotNull JetElement subroutine) {
        Pseudocode pseudocode = pseudocodeMap.get(subroutine);
        assert pseudocode != null;

        Collection<JetElement> unreachableElements = Lists.newArrayList();
        for (Instruction deadInstruction : pseudocode.getDeadInstructions()) {
            if (deadInstruction instanceof JetElementInstruction &&
                !(deadInstruction instanceof ReadUnitValueInstruction)) {
                unreachableElements.add(((JetElementInstruction) deadInstruction).getElement());
            }
        }
        // This is needed in order to highlight only '1 < 2' and not '1', '<' and '2' as well
        return JetPsiUtil.findRootExpressions(unreachableElements);
    }

////////////////////////////////////////////////////////////////////////////////
//  Uninitialized variables analysis

    public void markUninitializedVariables(@NotNull JetElement subroutine, final boolean processLocalDeclaration) {
        final Pseudocode pseudocode = pseudocodeMap.get(subroutine);
        assert pseudocode != null;

        JetControlFlowGraphTraverser<Map<VariableDescriptor, VariableInitializers>> traverser = JetControlFlowGraphTraverser.create(pseudocode, false);

        Collection<VariableDescriptor> usedVariables = collectUsedVariables(pseudocode);
        final Collection<VariableDescriptor> declaredVariables = collectDeclaredVariables(subroutine);
        Map<VariableDescriptor, VariableInitializers> initialMapForStartInstruction = prepareInitialMapForStartInstruction(usedVariables, declaredVariables);

        JetControlFlowGraphTraverser.InstructionDataMergeStrategy<Map<VariableDescriptor, VariableInitializers>> variableInitializersMergeStrategy =
                new JetControlFlowGraphTraverser.InstructionDataMergeStrategy<Map<VariableDescriptor, VariableInitializers>>() {
            @Override
            public Pair<Map<VariableDescriptor, VariableInitializers>, Map<VariableDescriptor, VariableInitializers>> execute(
                    Instruction instruction,
                    @NotNull Collection<Map<VariableDescriptor, VariableInitializers>> incomingEdgesData) {

                Map<VariableDescriptor, VariableInitializers> enterInstructionData = mergeIncomingEdgesData(incomingEdgesData);
                Map<VariableDescriptor, VariableInitializers> exitInstructionData = addVariableInitializerFromCurrentInstructionIfAny(instruction, enterInstructionData);
                return Pair.create(enterInstructionData, exitInstructionData);
            }
        };

        traverser.collectInformationFromInstructionGraph(variableInitializersMergeStrategy, Collections.<VariableDescriptor, VariableInitializers>emptyMap(), initialMapForStartInstruction, true);

        final Collection<VariableDescriptor> varWithUninitializedErrorGenerated = Sets.newHashSet();
        final Collection<VariableDescriptor> varWithValReassignErrorGenerated = Sets.newHashSet();
        final boolean processClassOrObject = subroutine instanceof JetClassOrObject;
        traverser.traverseAndAnalyzeInstructionGraph(new JetControlFlowGraphTraverser.InstructionDataAnalyzeStrategy<Map<VariableDescriptor, VariableInitializers>>() {
            @Override
            public void execute(Instruction instruction, @Nullable Map<VariableDescriptor, VariableInitializers> enterData, @Nullable Map<VariableDescriptor, VariableInitializers> exitData) {
                assert enterData != null && exitData != null;
                if (instruction instanceof ReadValueInstruction) {
                    VariableDescriptor variableDescriptor = extractVariableDescriptorIfAny(instruction, false);
                    if (variableDescriptor != null && declaredVariables.contains(variableDescriptor)) {
                        checkIsInitialized(variableDescriptor, ((ReadValueInstruction) instruction).getElement(), exitData.get(variableDescriptor), varWithUninitializedErrorGenerated);
                    }
                }
                else if (instruction instanceof WriteValueInstruction) {
                    VariableDescriptor variableDescriptor = extractVariableDescriptorIfAny(instruction, true);
                    JetElement element = ((WriteValueInstruction) instruction).getlValue();
                    if (variableDescriptor != null && element instanceof JetExpression) {
                        if (!processLocalDeclaration) { // error has been generated before while processing outer function of this local declaration
                            checkValReassignment(variableDescriptor, (JetExpression) element, enterData.get(variableDescriptor), varWithValReassignErrorGenerated);
                        }
                        if (processClassOrObject) {
                            checkInitializationUsingBackingField(variableDescriptor, (JetExpression) element, enterData.get(variableDescriptor), exitData.get(variableDescriptor));
                        }
                    }
                }
            }
        });

        recordInitializedVariables(declaredVariables, traverser.getResultInfo());
        analyzeLocalDeclarations(processLocalDeclaration, pseudocode);
    }

    private void checkIsInitialized(@NotNull VariableDescriptor variableDescriptor, @NotNull JetElement element, @NotNull VariableInitializers variableInitializers, @NotNull Collection<VariableDescriptor> varWithUninitializedErrorGenerated) {
        if (!(element instanceof JetSimpleNameExpression)) return;

        boolean isInitialized = variableInitializers.isInitialized();
        if (variableDescriptor instanceof PropertyDescriptor) {
            if (!trace.get(BindingContext.BACKING_FIELD_REQUIRED, (PropertyDescriptor) variableDescriptor)) {
                isInitialized = true;
            }
        }
        if (!isInitialized && !varWithUninitializedErrorGenerated.contains(variableDescriptor)) {
            varWithUninitializedErrorGenerated.add(variableDescriptor);
            trace.report(Errors.UNINITIALIZED_VARIABLE.on((JetSimpleNameExpression) element, variableDescriptor));
        }
    }

    private void checkValReassignment(@NotNull VariableDescriptor variableDescriptor, @NotNull JetExpression expression, @NotNull VariableInitializers enterInitializers, @NotNull Collection<VariableDescriptor> varWithValReassignErrorGenerated) {
        boolean isInitializedNotHere = enterInitializers.isInitialized();
        Set<JetElement> possibleLocalInitializers = enterInitializers.getPossibleLocalInitializers();
        if (possibleLocalInitializers.size() == 1) {
            JetElement initializer = possibleLocalInitializers.iterator().next();
            if (initializer instanceof JetProperty && initializer == expression.getParent()) {
                isInitializedNotHere = false;
            }
        }
        boolean hasBackingField = true;
        if (variableDescriptor instanceof PropertyDescriptor) {
            hasBackingField = trace.get(BindingContext.BACKING_FIELD_REQUIRED, (PropertyDescriptor) variableDescriptor);
        }
        if ((isInitializedNotHere || !hasBackingField) && !variableDescriptor.isVar() && !varWithValReassignErrorGenerated.contains(variableDescriptor)) {
            varWithValReassignErrorGenerated.add(variableDescriptor);

            boolean hasReassignMethodReturningUnit = false;
            JetSimpleNameExpression operationReference = null;
            PsiElement parent = expression.getParent();
            if (parent instanceof JetBinaryExpression) {
                operationReference = ((JetBinaryExpression) parent).getOperationReference();
            }
            else if (parent instanceof JetUnaryExpression) {
                operationReference = ((JetUnaryExpression) parent).getOperationSign();
            }
            if (operationReference != null) {
                DeclarationDescriptor descriptor = trace.get(BindingContext.REFERENCE_TARGET, operationReference);
                if (descriptor instanceof FunctionDescriptor) {
                    if (JetStandardClasses.isUnit(((FunctionDescriptor) descriptor).getReturnType())) {
                        hasReassignMethodReturningUnit = true;
                    }
                }
            }
            if (!hasReassignMethodReturningUnit) {
                trace.report(Errors.VAL_REASSIGNMENT.on(expression, variableDescriptor));
            }
        }
    }

    private void checkInitializationUsingBackingField(@NotNull VariableDescriptor variableDescriptor, @NotNull JetExpression expression, @NotNull VariableInitializers enterInitializers, @NotNull VariableInitializers exitInitializers) {
        if (variableDescriptor instanceof PropertyDescriptor && !enterInitializers.isInitialized() && exitInitializers.isInitialized()) {
            JetExpression variable = expression;
            if (expression instanceof JetDotQualifiedExpression) {
                if (((JetDotQualifiedExpression) expression).getReceiverExpression() instanceof JetThisExpression) {
                    variable = ((JetDotQualifiedExpression) expression).getSelectorExpression();
                }
            }
            if (variable instanceof JetSimpleNameExpression) {
                JetSimpleNameExpression simpleNameExpression = (JetSimpleNameExpression) variable;
                if (simpleNameExpression.getReferencedNameElementType() != JetTokens.FIELD_IDENTIFIER) {
                    trace.report(Errors.INITIALIZATION_USING_BACKING_FIELD.on(simpleNameExpression, expression, variableDescriptor));
                }
            }
        }
    }

    private void recordInitializedVariables(Collection<VariableDescriptor> declaredVariables, Map<VariableDescriptor, VariableInitializers> resultInfo) {
        for (Map.Entry<VariableDescriptor, VariableInitializers> entry : resultInfo.entrySet()) {
            VariableDescriptor variable = entry.getKey();
            if (variable instanceof PropertyDescriptor && declaredVariables.contains(variable)) {
                VariableInitializers initializers = entry.getValue();
                trace.record(BindingContext.IS_INITIALIZED, (PropertyDescriptor) variable, initializers.isInitialized());
            }
        }
    }

    private void analyzeLocalDeclarations(boolean processLocalDeclaration, Pseudocode pseudocode) {
        for (Instruction instruction : pseudocode.getInstructions()) {
            if (instruction instanceof LocalDeclarationInstruction) {
                JetElement element = ((LocalDeclarationInstruction) instruction).getElement();
                markUninitializedVariables(element, processLocalDeclaration);
            }
        }
    }

    private Map<VariableDescriptor, VariableInitializers> addVariableInitializerFromCurrentInstructionIfAny(Instruction instruction, Map<VariableDescriptor, VariableInitializers> enterInstructionData) {
        Map<VariableDescriptor, VariableInitializers> exitInstructionData = Maps.newHashMap(enterInstructionData);
        if (instruction instanceof WriteValueInstruction) {
            VariableDescriptor variable = extractVariableDescriptorIfAny(instruction, false);
            VariableInitializers initializationAtThisElement = new VariableInitializers(((WriteValueInstruction) instruction).getElement());
            exitInstructionData.put(variable, initializationAtThisElement);
        }
        return exitInstructionData;
    }

    private Map<VariableDescriptor, VariableInitializers> mergeIncomingEdgesData(Collection<Map<VariableDescriptor, VariableInitializers>> incomingEdgesData) {
        Set<VariableDescriptor> variablesInScope = Sets.newHashSet();
        for (Map<VariableDescriptor, VariableInitializers> edgeData : incomingEdgesData) {
            variablesInScope.addAll(edgeData.keySet());
        }

        Map<VariableDescriptor, VariableInitializers> enterInstructionData = Maps.newHashMap();
        for (VariableDescriptor variable : variablesInScope) {
            Set<VariableInitializers> edgesDataForVariable = Sets.newHashSet();
            for (Map<VariableDescriptor, VariableInitializers> edgeData : incomingEdgesData) {
                VariableInitializers initializers = edgeData.get(variable);
                if (initializers != null) {
                    edgesDataForVariable.add(initializers);
                }
            }
            enterInstructionData.put(variable, new VariableInitializers(edgesDataForVariable));
        }
        return enterInstructionData;
    }

    private Map<VariableDescriptor, VariableInitializers> prepareInitialMapForStartInstruction(Collection<VariableDescriptor> usedVariables, Collection<VariableDescriptor> declaredVariables) {
        Map<VariableDescriptor, VariableInitializers> initialMapForStartInstruction = Maps.newHashMap();
        VariableInitializers    isInitializedForExternalVariable = new VariableInitializers(true);
        VariableInitializers isNotInitializedForDeclaredVariable = new VariableInitializers(false);

        for (VariableDescriptor variable : usedVariables) {
            if (declaredVariables.contains(variable)) {
                initialMapForStartInstruction.put(variable, isNotInitializedForDeclaredVariable);
            }
            else {
                initialMapForStartInstruction.put(variable, isInitializedForExternalVariable);
            }
        }
        return initialMapForStartInstruction;
    }

////////////////////////////////////////////////////////////////////////////////

    public void markNotOnlyInvokedFunctionVariables(@NotNull JetElement subroutine, List<? extends VariableDescriptor> variables) {
        final List<VariableDescriptor> functionVariables = Lists.newArrayList();
        for (VariableDescriptor variable : variables) {
            if (JetStandardClasses.isFunctionType(variable.getReturnType())) {
                functionVariables.add(variable);
            }
        }

        Pseudocode pseudocode = pseudocodeMap.get(subroutine);
        assert pseudocode != null;

        JetControlFlowGraphTraverser.<Void>create(pseudocode, true).traverseAndAnalyzeInstructionGraph(new JetControlFlowGraphTraverser.InstructionDataAnalyzeStrategy<Void>() {
            @Override
            public void execute(Instruction instruction, Void enterData, Void exitData) {
                if (instruction instanceof ReadValueInstruction) {
                    VariableDescriptor variableDescriptor = extractVariableDescriptorIfAny(instruction, false);
                    if (variableDescriptor != null && functionVariables.contains(variableDescriptor)) {
                        //check that we only invoke this variable
                        JetElement element = ((ReadValueInstruction) instruction).getElement();
                        if (element instanceof JetSimpleNameExpression && !(element.getParent() instanceof JetCallExpression)) {
                            trace.report(Errors.FUNCTION_PARAMETERS_OF_INLINE_FUNCTION.on((JetSimpleNameExpression) element, variableDescriptor));
                        }
                    }
                }
            }
        });
    }

////////////////////////////////////////////////////////////////////////////////
//  "Unused variable" & "unused value" analyses

    public void markUnusedVariables(@NotNull JetElement subroutine) {
        Pseudocode pseudocode = pseudocodeMap.get(subroutine);
        assert pseudocode != null;

        final Set<VariableDescriptor> usedVariables = Sets.newHashSet();
        JetControlFlowGraphTraverser.<Void>create(pseudocode, true).traverseAndAnalyzeInstructionGraph(new JetControlFlowGraphTraverser.InstructionDataAnalyzeStrategy<Void>() {
            @Override
            public void execute(Instruction instruction, @Nullable Void enterData, @Nullable Void exitData) {
                if (instruction instanceof ReadValueInstruction) {
                    VariableDescriptor variableDescriptor = extractVariableDescriptorIfAny(instruction, false);
                    usedVariables.add(variableDescriptor);
                }
            }
        });
        Collection<VariableDescriptor> declaredVariables = collectDeclaredVariables(subroutine);
        for (VariableDescriptor declaredVariable : declaredVariables) {
            if (!usedVariables.contains(declaredVariable)) {
                PsiElement element = trace.get(BindingContext.DESCRIPTOR_TO_DECLARATION, declaredVariable);
                //todo
                if (element instanceof JetProperty && JetPsiUtil.isLocal((JetNamedDeclaration) element)) {
                    PsiElement nameIdentifier = ((JetNamedDeclaration) element).getNameIdentifier();
                    PsiElement elementToMark = nameIdentifier != null ? nameIdentifier : element;
                    trace.report(Errors.UNUSED_VARIABLE.on((JetNamedDeclaration)element, elementToMark, declaredVariable));
                }
            }
        }

        markUnusedValues(subroutine, pseudocode, declaredVariables);
    }

    private void markUnusedValues(@NotNull JetElement subroutine, Pseudocode pseudocode, final Collection<VariableDescriptor> declaredVariables) {
        JetControlFlowGraphTraverser<Set<VariableDescriptor>> traverser = JetControlFlowGraphTraverser.create(pseudocode, true);
        traverser.collectInformationFromInstructionGraph(new JetControlFlowGraphTraverser.InstructionDataMergeStrategy<Set<VariableDescriptor>>() {
            @Override
            public Pair<Set<VariableDescriptor>, Set<VariableDescriptor>> execute(Instruction instruction, @NotNull Collection<Set<VariableDescriptor>> incomingEdgesData) {
                Set<VariableDescriptor> enterResult = Sets.newHashSet();
                for (Set<VariableDescriptor> edgeData : incomingEdgesData) {
                    enterResult.addAll(edgeData);
                }
                Set<VariableDescriptor> exitResult = Sets.newHashSet(enterResult);
                if (instruction instanceof ReadValueInstruction) {
                    VariableDescriptor variableDescriptor = extractVariableDescriptorIfAny(instruction, true);
                    if (variableDescriptor != null) {
                        exitResult.add(variableDescriptor);
                    }
                }
                else if (instruction instanceof WriteValueInstruction) {
                    VariableDescriptor variableDescriptor = extractVariableDescriptorIfAny(instruction, true);
                    if (variableDescriptor != null) {
                        exitResult.remove(variableDescriptor);
                    }
                }
                return new Pair<Set<VariableDescriptor>, Set<VariableDescriptor>>(enterResult, exitResult);
            }
        }, Collections.<VariableDescriptor>emptySet(), Collections.<VariableDescriptor>emptySet(), false);
        traverser.traverseAndAnalyzeInstructionGraph(new JetControlFlowGraphTraverser.InstructionDataAnalyzeStrategy<Set<VariableDescriptor>>() {
            @Override
            public void execute(Instruction instruction, @Nullable Set<VariableDescriptor> enterData, @Nullable Set<VariableDescriptor> exitData) {
                assert enterData != null && exitData != null;
                if (instruction instanceof WriteValueInstruction) {
                    VariableDescriptor variableDescriptor = extractVariableDescriptorIfAny(instruction, false);
                    if (variableDescriptor != null && declaredVariables.contains(variableDescriptor)) {
                        if (!enterData.contains(variableDescriptor)) {
                            PsiElement variableDeclarationElement = trace.get(BindingContext.DESCRIPTOR_TO_DECLARATION, variableDescriptor);
//                            assert variableDeclarationElement instanceof JetProperty || variableDeclarationElement instanceof JetParameter;
//                            boolean isLocal = !(variableDeclarationElement instanceof JetProperty) || JetPsiUtil.isLocal((JetProperty) variableDeclarationElement);
                            assert variableDeclarationElement instanceof JetDeclaration;
                            boolean isLocal = JetPsiUtil.isLocal((JetDeclaration) variableDeclarationElement);
                            if (isLocal) {
                                JetElement element = ((WriteValueInstruction) instruction).getElement();
                                if (element instanceof JetBinaryExpression && ((JetBinaryExpression) element).getOperationToken() == JetTokens.EQ) {
                                    JetExpression right = ((JetBinaryExpression) element).getRight();
                                    if (right != null) {
                                        trace.report(Errors.UNUSED_VALUE.on(right, right, variableDescriptor));
                                    }
                                }
                                else if (element instanceof JetPostfixExpression) {
                                    IElementType operationToken = ((JetPostfixExpression) element).getOperationSign().getReferencedNameElementType();
                                    if (operationToken == JetTokens.PLUSPLUS || operationToken == JetTokens.MINUSMINUS) {
                                        trace.report(Errors.UNUSED_CHANGED_VALUE.on(element, element));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

////////////////////////////////////////////////////////////////////////////////
//  Util methods

    @Nullable
    private VariableDescriptor extractVariableDescriptorIfAny(Instruction instruction, boolean onlyReference) {
        VariableDescriptor variableDescriptor = null;
        if (instruction instanceof ReadValueInstruction) {
            JetElement element = ((ReadValueInstruction) instruction).getElement();
            variableDescriptor = BindingContextUtils.extractVariableDescriptorIfAny(trace.getBindingContext(), element, onlyReference);
        }
        else if (instruction instanceof WriteValueInstruction) {
            JetElement lValue = ((WriteValueInstruction) instruction).getlValue();
            variableDescriptor = BindingContextUtils.extractVariableDescriptorIfAny(trace.getBindingContext(), lValue, onlyReference);
        }
        return variableDescriptor;
    }

    private Collection<VariableDescriptor> collectUsedVariables(Pseudocode pseudocode) {
        final Set<VariableDescriptor> usedVariables = Sets.newHashSet();
        JetControlFlowGraphTraverser.<Void>create(pseudocode, true).traverseAndAnalyzeInstructionGraph(new JetControlFlowGraphTraverser.InstructionDataAnalyzeStrategy<Void>() {
            @Override
            public void execute(Instruction instruction, Void enterData, Void exitData) {
                VariableDescriptor variableDescriptor = extractVariableDescriptorIfAny(instruction, false);
                if (variableDescriptor != null) {
                    usedVariables.add(variableDescriptor);
                }
            }
        });
        return usedVariables;
    }

    private Collection<VariableDescriptor> collectDeclaredVariables(JetElement element) {
        final Pseudocode pseudocode = pseudocodeMap.get(element);
        assert pseudocode != null;

        final Set<VariableDescriptor> declaredVariables = Sets.newHashSet();
        JetControlFlowGraphTraverser.<Void>create(pseudocode, false).traverseAndAnalyzeInstructionGraph(new JetControlFlowGraphTraverser.InstructionDataAnalyzeStrategy<Void>() {
            @Override
            public void execute(Instruction instruction, @Nullable Void enterData, @Nullable Void exitData) {
                if (instruction instanceof VariableDeclarationInstruction) {
                    JetDeclaration variableDeclarationElement = ((VariableDeclarationInstruction) instruction).getVariableDeclarationElement();
                    DeclarationDescriptor descriptor = trace.get(BindingContext.DECLARATION_TO_DESCRIPTOR, variableDeclarationElement);
                    if (descriptor != null) {
                        assert descriptor instanceof VariableDescriptor;
                        declaredVariables.add((VariableDescriptor) descriptor);
                    }
                }
            }
        });
        return declaredVariables;
    }

////////////////////////////////////////////////////////////////////////////////
//  Local class for uninitialized variables analysis

    private static class VariableInitializers {
        private Set<JetElement> possibleLocalInitializers = Sets.newHashSet();
        private boolean isInitialized;

        public VariableInitializers(boolean isInitialized) {
            this.isInitialized = isInitialized;
        }

        public VariableInitializers(JetElement element) {
            isInitialized = true;
            possibleLocalInitializers.add(element);
        }

        public VariableInitializers(Set<VariableInitializers> edgesData) {
            isInitialized = true;
            for (VariableInitializers edgeData : edgesData) {
                if (!edgeData.isInitialized) {
                    isInitialized = false;
                }
                possibleLocalInitializers.addAll(edgeData.possibleLocalInitializers);
            }
        }

        public Set<JetElement> getPossibleLocalInitializers() {
            return possibleLocalInitializers;
        }

        public boolean isInitialized() {
            return isInitialized;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VariableInitializers)) return false;

            VariableInitializers that = (VariableInitializers) o;

            if (isInitialized != that.isInitialized) return false;
            if (possibleLocalInitializers != null ? !possibleLocalInitializers.equals(that.possibleLocalInitializers) : that.possibleLocalInitializers != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = possibleLocalInitializers != null ? possibleLocalInitializers.hashCode() : 0;
            result = 31 * result + (isInitialized ? 1 : 0);
            return result;
        }
    }
}
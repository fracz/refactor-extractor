package org.jetbrains.jet.lang.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.cfg.JetFlowInformationProvider;
import org.jetbrains.jet.lang.cfg.pseudocode.JetControlFlowDataTraceFactory;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.types.JetStandardClasses;
import org.jetbrains.jet.lang.types.JetType;

import java.util.List;
import java.util.Map;

import static org.jetbrains.jet.lang.types.TypeUtils.NO_EXPECTED_TYPE;

/**
 * @author svtk
 */
public class ControlFlowAnalyzer {
    private TopDownAnalysisContext context;
    private final JetControlFlowDataTraceFactory flowDataTraceFactory;
    private final boolean processLocalDeclaration;

    public ControlFlowAnalyzer(TopDownAnalysisContext context, JetControlFlowDataTraceFactory flowDataTraceFactory, boolean processLocalDeclaration) {
        this.context = context;
        this.flowDataTraceFactory = flowDataTraceFactory;
        this.processLocalDeclaration = processLocalDeclaration;
    }

    public void process() {
        for (JetClass aClass : context.getClasses().keySet()) {
            if (!context.completeAnalysisNeeded(aClass)) continue;
            checkClassOrObject(aClass);
        }
        for (JetObjectDeclaration objectDeclaration : context.getObjects().keySet()) {
            if (!context.completeAnalysisNeeded(objectDeclaration)) continue;
            checkClassOrObject(objectDeclaration);
        }
        for (Map.Entry<JetNamedFunction, FunctionDescriptorImpl> entry : context.getFunctions().entrySet()) {
            JetNamedFunction function = entry.getKey();
            FunctionDescriptorImpl functionDescriptor = entry.getValue();
            if (!context.completeAnalysisNeeded(function)) continue;
            final JetType expectedReturnType = !function.hasBlockBody() && !function.hasDeclaredReturnType()
                                               ? NO_EXPECTED_TYPE
                                               : functionDescriptor.getReturnType();
            checkFunction(function, expectedReturnType);
        }
        for (JetSecondaryConstructor constructor : this.context.getConstructors().keySet()) {
            if (!context.completeAnalysisNeeded(constructor)) continue;
            checkFunction(constructor, JetStandardClasses.getUnitType());
        }
    }

    private void checkClassOrObject(JetClassOrObject klass) {
        JetFlowInformationProvider flowInformationProvider = new JetFlowInformationProvider((JetDeclaration) klass, (JetExpression) klass, flowDataTraceFactory, context.getTrace());
        flowInformationProvider.markUninitializedVariables((JetElement) klass, processLocalDeclaration);

        List<JetDeclaration> declarations = klass.getDeclarations();
        for (JetDeclaration declaration : declarations) {
            if (declaration instanceof JetProperty) {
                JetProperty property = (JetProperty) declaration;
                DeclarationDescriptor descriptor = context.getTrace().get(BindingContext.DECLARATION_TO_DESCRIPTOR, property);
                assert descriptor instanceof PropertyDescriptor;
                PropertyDescriptor propertyDescriptor = (PropertyDescriptor) descriptor;
                for (JetPropertyAccessor accessor : property.getAccessors()) {
                    PropertyAccessorDescriptor accessorDescriptor = accessor.isGetter()
                                                                    ? propertyDescriptor.getGetter()
                                                                    : propertyDescriptor.getSetter();
                    assert accessorDescriptor != null;
                    checkFunction(accessor, accessorDescriptor.getReturnType());
                }
            }
        }
    }

    private void checkFunction(JetDeclarationWithBody function, final @NotNull JetType expectedReturnType) {
        assert function instanceof JetDeclaration;

        JetExpression bodyExpression = function.getBodyExpression();
        if (bodyExpression == null) return;
        JetFlowInformationProvider flowInformationProvider = new JetFlowInformationProvider((JetDeclaration) function, bodyExpression, flowDataTraceFactory, context.getTrace());

        flowInformationProvider.checkDefiniteReturn(function, expectedReturnType);

        flowInformationProvider.markUninitializedVariables(function.asElement(), processLocalDeclaration);

        flowInformationProvider.markUnusedVariables(function.asElement());
    }
}
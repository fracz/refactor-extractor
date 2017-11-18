package org.jetbrains.jet.lang.resolve.java;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverDescriptor;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.types.TypeSubstitutor;

import java.util.*;

/**
 * @author abreslav
 */
public class JavaClassMembersScope implements JetScope {
    private final PsiClass psiClass;
    private final JavaSemanticServices semanticServices;
    private final boolean staticMembers;
    private final DeclarationDescriptor containingDeclaration;
    private final Map<String, Set<FunctionDescriptor>> functionGroups = Maps.newHashMap();
    private final Map<String, Set<VariableDescriptor>> variables = Maps.newHashMap();
    private final Map<String, ClassifierDescriptor> classifiers = Maps.newHashMap();
    private Collection<DeclarationDescriptor> allDescriptors;

    public JavaClassMembersScope(@NotNull DeclarationDescriptor classOrNamespaceDescriptor, PsiClass psiClass, JavaSemanticServices semanticServices, boolean staticMembers) {
        this.containingDeclaration = classOrNamespaceDescriptor;
        this.psiClass = psiClass;
        this.semanticServices = semanticServices;
        this.staticMembers = staticMembers;
    }

    @NotNull
    @Override
    public DeclarationDescriptor getContainingDeclaration() {
        return containingDeclaration;
    }

    @NotNull
    @Override
    public Collection<DeclarationDescriptor> getDeclarationsByLabel(String labelName) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public PropertyDescriptor getPropertyByFieldReference(@NotNull String fieldName) {
        return null;
    }

    @Override
    public ClassifierDescriptor getClassifier(@NotNull String name) {
        ClassifierDescriptor classifierDescriptor = classifiers.get(name);
        if (classifierDescriptor == null) {
            classifierDescriptor = doGetClassifierDescriptor(name);
            classifiers.put(name, classifierDescriptor);
        }
        return classifierDescriptor;
    }

    @Override
    public ClassDescriptor getObjectDescriptor(@NotNull String name) {
        return null;
    }

    @NotNull
    @Override
    public Collection<DeclarationDescriptor> getAllDescriptors() {
        if (allDescriptors == null) {
            allDescriptors = Sets.newHashSet();
            TypeSubstitutor substitutorForGenericSupertypes = getTypeSubstitutorForSupertypes();

            for (HierarchicalMethodSignature signature : psiClass.getVisibleSignatures()) {
                PsiMethod method = signature.getMethod();
                if (method.hasModifierProperty(PsiModifier.STATIC) != staticMembers) {
                    continue;
                }
                FunctionDescriptor functionDescriptor = semanticServices.getDescriptorResolver().resolveMethodToFunctionDescriptor(containingDeclaration, psiClass, substitutorForGenericSupertypes, method);
                if (functionDescriptor != null) {
                    allDescriptors.add(functionDescriptor);
                }
            }

            allDescriptors.addAll(semanticServices.getDescriptorResolver().resolveFieldGroup(containingDeclaration, psiClass, staticMembers));

            allDescriptors.addAll(semanticServices.getDescriptorResolver().resolveInnerClasses(containingDeclaration, psiClass, staticMembers));
        }
        return allDescriptors;
    }

    private TypeSubstitutor getTypeSubstitutorForSupertypes() {
        TypeSubstitutor substitutorForGenericSupertypes;
        if (containingDeclaration instanceof ClassDescriptor) {
            substitutorForGenericSupertypes = semanticServices.getDescriptorResolver().createSubstitutorForGenericSupertypes((ClassDescriptor) containingDeclaration);
        }
        else {
            substitutorForGenericSupertypes = TypeSubstitutor.EMPTY;
        }
        return substitutorForGenericSupertypes;
    }

    private ClassifierDescriptor doGetClassifierDescriptor(String name) {
        // TODO : suboptimal, walk the list only once
        for (PsiClass innerClass : psiClass.getAllInnerClasses()) {
            if (name.equals(innerClass.getName())) {
                if (innerClass.hasModifierProperty(PsiModifier.STATIC) != staticMembers) return null;
                ClassDescriptor classDescriptor = semanticServices.getDescriptorResolver().resolveClass(innerClass);
                if (classDescriptor != null) {
                    return classDescriptor;
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Set<VariableDescriptor> getProperties(@NotNull String name) {
        Set<VariableDescriptor> variableDescriptor = variables.get(name);
        if (variableDescriptor == null) {
            variableDescriptor = doGetVariable(name);
            if (variableDescriptor != null) {
                variables.put(name, variableDescriptor);
            }
        }
        return variableDescriptor != null ? variableDescriptor : Collections.<VariableDescriptor>emptySet();
    }

    @Override
    public VariableDescriptor getLocalVariable(@NotNull String name) {
        return null;
    }

    @Nullable
    private Set<VariableDescriptor> doGetVariable(String name) {
        PsiField field = psiClass.findFieldByName(name, true);
        if (field == null) return null;
        if (field.hasModifierProperty(PsiModifier.STATIC) != staticMembers) {
            return null;
        }

        //return semanticServices.getDescriptorResolver().resolveFieldToVariableDescriptor(containingDeclaration, field);
        return semanticServices.getDescriptorResolver().resolveFieldGroupByName(containingDeclaration, psiClass, name, staticMembers);
    }

    @NotNull
    @Override
    public Set<FunctionDescriptor> getFunctions(@NotNull String name) {
        Set<FunctionDescriptor> functionGroup = functionGroups.get(name);
        if (functionGroup == null) {
            functionGroup = semanticServices.getDescriptorResolver().resolveFunctionGroup(
                    containingDeclaration,
                    psiClass,
                    staticMembers ? null : (ClassDescriptor) containingDeclaration,
                    name,
                    staticMembers);
            functionGroups.put(name, functionGroup);
        }
        return functionGroup;
    }

    @Override
    public NamespaceDescriptor getNamespace(@NotNull String name) {
        return null;
    }

    @NotNull
    @Override
    public ReceiverDescriptor getImplicitReceiver() {
        return ReceiverDescriptor.NO_RECEIVER; // Should never occur, we don't sit in a Java class...
    }

    @Override
    public void getImplicitReceiversHierarchy(@NotNull List<ReceiverDescriptor> result) {
        // we cannot really be scoped inside here
    }
}
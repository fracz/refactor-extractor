package org.jetbrains.jet.lang.resolve.java;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.descriptors.annotations.AnnotationDescriptor;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.types.*;

import java.util.*;

/**
 * @author abreslav
 */
public class JavaDescriptorResolver {

    /*package*/ static final DeclarationDescriptor JAVA_ROOT = new DeclarationDescriptorImpl(null, Collections.<AnnotationDescriptor>emptyList(), "<java_root>") {
        @NotNull
        @Override
        public DeclarationDescriptor substitute(TypeSubstitutor substitutor) {
            throw new UnsupportedOperationException(); // TODO
        }

        @Override
        public <R, D> R accept(DeclarationDescriptorVisitor<R, D> visitor, D data) {
            return visitor.visitDeclarationDescriptor(this, data);
        }
    };

    /*package*/ static final DeclarationDescriptor JAVA_CLASS_OBJECT = new DeclarationDescriptorImpl(null, Collections.<AnnotationDescriptor>emptyList(), "<java_class_object_emulation>") {
        @NotNull
        @Override
        public DeclarationDescriptor substitute(TypeSubstitutor substitutor) {
            throw new UnsupportedOperationException(); // TODO
        }

        @Override
        public <R, D> R accept(DeclarationDescriptorVisitor<R, D> visitor, D data) {
            return visitor.visitDeclarationDescriptor(this, data);
        }
    };

    protected final Map<String, ClassDescriptor> classDescriptorCache = new HashMap<String, ClassDescriptor>();
    protected final Map<PsiTypeParameter, TypeParameterDescriptor> typeParameterDescriptorCache = Maps.newHashMap();
    protected final Map<PsiMethod, FunctionDescriptor> methodDescriptorCache = Maps.newHashMap();
    protected final Map<PsiField, VariableDescriptor> fieldDescriptorCache = Maps.newHashMap();
    protected final Map<String, NamespaceDescriptor> namespaceDescriptorCache = new HashMap<String, NamespaceDescriptor>();
    protected final JavaPsiFacade javaFacade;
    protected final GlobalSearchScope javaSearchScope;
    protected final JavaSemanticServices semanticServices;

    public JavaDescriptorResolver(Project project, JavaSemanticServices semanticServices) {
        this.javaFacade = JavaPsiFacade.getInstance(project);
        this.javaSearchScope = GlobalSearchScope.allScope(project);
        this.semanticServices = semanticServices;
    }

    @NotNull
    public ClassDescriptor resolveClass(@NotNull PsiClass psiClass) {
        String qualifiedName = psiClass.getQualifiedName();
        ClassDescriptor classDescriptor = classDescriptorCache.get(qualifiedName);
        if (classDescriptor == null) {
            classDescriptor = createJavaClassDescriptor(psiClass);
            classDescriptorCache.put(qualifiedName, classDescriptor);
        }
        return classDescriptor;
    }

    @Nullable
    public ClassDescriptor resolveClass(@NotNull String qualifiedName) {
        ClassDescriptor classDescriptor = classDescriptorCache.get(qualifiedName);
        if (classDescriptor == null) {
            PsiClass psiClass = javaFacade.findClass(qualifiedName, javaSearchScope);
            if (psiClass == null) {
                return null;
            }
            classDescriptor = createJavaClassDescriptor(psiClass);
        }
        return classDescriptor;
    }

    private ClassDescriptor createJavaClassDescriptor(@NotNull final PsiClass psiClass) {
        assert !classDescriptorCache.containsKey(psiClass.getQualifiedName()) : psiClass.getQualifiedName();
        classDescriptorCache.put(psiClass.getQualifiedName(), null); // TODO

        String name = psiClass.getName();
        JavaClassDescriptor classDescriptor = new JavaClassDescriptor(
                JAVA_ROOT
        );
        classDescriptor.setName(name);

        List<JetType> supertypes = new ArrayList<JetType>();
        List<TypeParameterDescriptor> typeParameters = resolveTypeParameters(classDescriptor, psiClass.getTypeParameters());
        classDescriptor.setTypeConstructor(new TypeConstructorImpl(
                classDescriptor,
                Collections.<AnnotationDescriptor>emptyList(), // TODO
                // TODO
                psiClass.hasModifierProperty(PsiModifier.FINAL),
                name,
                typeParameters,
                supertypes

        ));
        classDescriptorCache.put(psiClass.getQualifiedName(), classDescriptor);
        classDescriptor.setUnsubstitutedMemberScope(new JavaClassMembersScope(classDescriptor, psiClass, semanticServices, false));
        classDescriptor.setClassObjectMemberScope(new JavaClassMembersScope(classDescriptor, psiClass, semanticServices, true));
        // UGLY HACK
        supertypes.addAll(getSupertypes(psiClass));

        PsiMethod[] psiConstructors = psiClass.getConstructors();

        if (psiConstructors.length == 0) {
            if (!psiClass.hasModifierProperty(PsiModifier.ABSTRACT) && !psiClass.isInterface()) {
                ConstructorDescriptorImpl constructorDescriptor = new ConstructorDescriptorImpl(
                        classDescriptor,
                        Collections.<AnnotationDescriptor>emptyList(),
                        false);
                constructorDescriptor.initialize(typeParameters, Collections.<ValueParameterDescriptor>emptyList());
                constructorDescriptor.setReturnType(classDescriptor.getDefaultType());
                classDescriptor.addConstructor(constructorDescriptor);
                semanticServices.getTrace().record(BindingContext.CONSTRUCTOR, psiClass, constructorDescriptor);
            }
        }
        else {
            for (PsiMethod constructor : psiConstructors) {
                ConstructorDescriptorImpl constructorDescriptor = new ConstructorDescriptorImpl(
                        classDescriptor,
                        Collections.<AnnotationDescriptor>emptyList(), // TODO
                        false);
                constructorDescriptor.initialize(typeParameters, resolveParameterDescriptors(constructorDescriptor, constructor.getParameterList().getParameters()));
                constructorDescriptor.setReturnType(classDescriptor.getDefaultType());
                classDescriptor.addConstructor(constructorDescriptor);
                semanticServices.getTrace().record(BindingContext.CONSTRUCTOR, constructor, constructorDescriptor);
            }
        }

        semanticServices.getTrace().record(BindingContext.CLASS, psiClass, classDescriptor);

        return classDescriptor;
    }

    private List<TypeParameterDescriptor> resolveTypeParameters(@NotNull DeclarationDescriptor containingDeclaration, @NotNull PsiTypeParameter[] typeParameters) {
        List<TypeParameterDescriptor> result = Lists.newArrayList();
        for (PsiTypeParameter typeParameter : typeParameters) {
            TypeParameterDescriptor typeParameterDescriptor = resolveTypeParameter(containingDeclaration, typeParameter);
            result.add(typeParameterDescriptor);
        }
        return result;
    }

    private TypeParameterDescriptor createJavaTypeParameterDescriptor(@NotNull DeclarationDescriptor owner, @NotNull PsiTypeParameter typeParameter) {
        TypeParameterDescriptor typeParameterDescriptor = TypeParameterDescriptor.createForFurtherModification(
                owner,
                Collections.<AnnotationDescriptor>emptyList(), // TODO
                Variance.INVARIANT,
                typeParameter.getName(),
                typeParameter.getIndex()
        );
        typeParameterDescriptorCache.put(typeParameter, typeParameterDescriptor);
        PsiClassType[] referencedTypes = typeParameter.getExtendsList().getReferencedTypes();
        if (referencedTypes.length == 0){
            typeParameterDescriptor.addUpperBound(JetStandardClasses.getNullableAnyType());
        }
        else if (referencedTypes.length == 1) {
            typeParameterDescriptor.addUpperBound(semanticServices.getTypeTransformer().transformToType(referencedTypes[0]));
        }
        else {
            for (PsiClassType referencedType : referencedTypes) {
                typeParameterDescriptor.addUpperBound(semanticServices.getTypeTransformer().transformToType(referencedType));
            }
        }
        return typeParameterDescriptor;
    }

    @NotNull
    public TypeParameterDescriptor resolveTypeParameter(@NotNull DeclarationDescriptor containingDeclaration, @NotNull PsiTypeParameter psiTypeParameter) {
        TypeParameterDescriptor typeParameterDescriptor = typeParameterDescriptorCache.get(psiTypeParameter);
        if (typeParameterDescriptor == null) {
            typeParameterDescriptor = createJavaTypeParameterDescriptor(containingDeclaration, psiTypeParameter);
//            This is done inside the method: typeParameterDescriptorCache.put(psiTypeParameter, typeParameterDescriptor);
        }
        return typeParameterDescriptor;
    }

    private Collection<? extends JetType> getSupertypes(PsiClass psiClass) {
        List<JetType> result = new ArrayList<JetType>();
        result.add(JetStandardClasses.getAnyType());
        transformSupertypeList(result, psiClass.getExtendsListTypes());
        transformSupertypeList(result, psiClass.getImplementsListTypes());
        return result;
    }

    private void transformSupertypeList(List<JetType> result, PsiClassType[] extendsListTypes) {
        for (PsiClassType type : extendsListTypes) {
            JetType transform = semanticServices.getTypeTransformer().transformToType(type);

            result.add(TypeUtils.makeNotNullable(transform));
        }
    }

    public NamespaceDescriptor resolveNamespace(String qualifiedName) {
        NamespaceDescriptor namespaceDescriptor = namespaceDescriptorCache.get(qualifiedName);
        if (namespaceDescriptor == null) {
            // TODO : packages

//            PsiClass psiClass = javaFacade.findClass(qualifiedName, javaSearchScope);
//            if (psiClass != null) {
//                namespaceDescriptor = createJavaNamespaceDescriptor(psiClass);
//            }
//            else {
                PsiPackage psiPackage = javaFacade.findPackage(qualifiedName);
                if (psiPackage == null) {
                    return null;
                }
                namespaceDescriptor = createJavaNamespaceDescriptor(psiPackage);
//            }
            namespaceDescriptorCache.put(qualifiedName, namespaceDescriptor);
        }
        return namespaceDescriptor;
    }

    private NamespaceDescriptor createJavaNamespaceDescriptor(PsiPackage psiPackage) {
        JavaNamespaceDescriptor namespaceDescriptor = new JavaNamespaceDescriptor(
                JAVA_ROOT,
                Collections.<AnnotationDescriptor>emptyList(), // TODO
                psiPackage.getName()
        );
        namespaceDescriptor.setMemberScope(new JavaPackageScope(psiPackage.getQualifiedName(), namespaceDescriptor, semanticServices));
        semanticServices.getTrace().record(BindingContext.NAMESPACE, psiPackage, namespaceDescriptor);
        return namespaceDescriptor;
    }

    private NamespaceDescriptor createJavaNamespaceDescriptor(@NotNull final PsiClass psiClass) {
        JavaNamespaceDescriptor namespaceDescriptor = new JavaNamespaceDescriptor(
                JAVA_ROOT,
                Collections.<AnnotationDescriptor>emptyList(), // TODO
                psiClass.getName()
        );
        namespaceDescriptor.setMemberScope(new JavaClassMembersScope(namespaceDescriptor, psiClass, semanticServices, true));
        semanticServices.getTrace().record(BindingContext.NAMESPACE, psiClass, namespaceDescriptor);
        return namespaceDescriptor;
    }

    public List<ValueParameterDescriptor> resolveParameterDescriptors(DeclarationDescriptor containingDeclaration, PsiParameter[] parameters) {
        List<ValueParameterDescriptor> result = new ArrayList<ValueParameterDescriptor>();
        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            PsiParameter parameter = parameters[i];
            String name = parameter.getName();
            result.add(new ValueParameterDescriptorImpl(
                    containingDeclaration,
                    i,
                    Collections.<AnnotationDescriptor>emptyList(), // TODO
                    name == null ? "p" + i : name,
                    null, // TODO : review
                    semanticServices.getTypeTransformer().transformToType(parameter.getType()),
                    false,
                    parameter.isVarArgs()
            ));
        }
        return result;
    }

    public VariableDescriptor resolveFieldToVariableDescriptor(DeclarationDescriptor containingDeclaration, PsiField field) {
        VariableDescriptor variableDescriptor = fieldDescriptorCache.get(field);
        if (variableDescriptor != null) {
            return variableDescriptor;
        }
        JetType type = semanticServices.getTypeTransformer().transformToType(field.getType());
        boolean isFinal = field.hasModifierProperty(PsiModifier.FINAL);
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(
                containingDeclaration,
                Collections.<AnnotationDescriptor>emptyList(),
                new MemberModifiers(false, false, false),
                !isFinal,
                null,
                field.getName(),
                isFinal ? null : type,
                type);
        semanticServices.getTrace().record(BindingContext.VARIABLE, field, propertyDescriptor);
        fieldDescriptorCache.put(field, propertyDescriptor);
        return propertyDescriptor;
    }

    @NotNull
    public FunctionGroup resolveFunctionGroup(@NotNull DeclarationDescriptor owner, @NotNull PsiClass psiClass, @Nullable ClassDescriptor classDescriptor, @NotNull String methodName, boolean staticMembers) {
        WritableFunctionGroup writableFunctionGroup = new WritableFunctionGroup(methodName);
        final Collection<HierarchicalMethodSignature> signatures = psiClass.getVisibleSignatures();
        TypeSubstitutor typeSubstitutor = createSubstitutorForGenericSupertypes(classDescriptor);
        for (HierarchicalMethodSignature signature: signatures) {
            PsiMethod method = signature.getMethod();
            if (method.hasModifierProperty(PsiModifier.STATIC) != staticMembers) {
                continue;
            }
            if (!methodName.equals(method.getName())) {
                 continue;
            }

            FunctionDescriptor substitutedFunctionDescriptor = resolveMethodToFunctionDescriptor(owner, psiClass, typeSubstitutor, method);
            if (substitutedFunctionDescriptor != null) {
                writableFunctionGroup.addFunction(substitutedFunctionDescriptor);
            }
        }
        return writableFunctionGroup;
    }

    public TypeSubstitutor createSubstitutorForGenericSupertypes(ClassDescriptor classDescriptor) {
        TypeSubstitutor typeSubstitutor;
        if (classDescriptor != null) {
            typeSubstitutor = TypeUtils.buildDeepSubstitutor(classDescriptor.getDefaultType());
        }
        else {
            typeSubstitutor = TypeSubstitutor.EMPTY;
        }
        return typeSubstitutor;
    }

    @Nullable
    public FunctionDescriptor resolveMethodToFunctionDescriptor(DeclarationDescriptor owner, PsiClass psiClass, TypeSubstitutor typeSubstitutorForGenericSuperclasses, PsiMethod method) {
        PsiType returnType = method.getReturnType();
        if (returnType == null) {
            return null;
        }
        FunctionDescriptor functionDescriptor = methodDescriptorCache.get(method);
        if (functionDescriptor != null) {
            if (method.getContainingClass() != psiClass) {
                functionDescriptor = functionDescriptor.substitute(typeSubstitutorForGenericSuperclasses);
            }
            return functionDescriptor;
        }
        PsiParameter[] parameters = method.getParameterList().getParameters();
        FunctionDescriptorImpl functionDescriptorImpl = new FunctionDescriptorImpl(
                owner,
                Collections.<AnnotationDescriptor>emptyList(), // TODO
                method.getName()
        );
        methodDescriptorCache.put(method, functionDescriptorImpl);
        functionDescriptorImpl.initialize(
                null,
                resolveTypeParameters(functionDescriptorImpl, method.getTypeParameters()),
                semanticServices.getDescriptorResolver().resolveParameterDescriptors(functionDescriptorImpl, parameters),
                semanticServices.getTypeTransformer().transformToType(returnType)
        );
        semanticServices.getTrace().record(BindingContext.FUNCTION, method, functionDescriptorImpl);
        FunctionDescriptor substitutedFunctionDescriptor = functionDescriptorImpl;
        if (method.getContainingClass() != psiClass) {
            substitutedFunctionDescriptor = functionDescriptorImpl.substitute(typeSubstitutorForGenericSuperclasses);
        }
        return substitutedFunctionDescriptor;
    }
}
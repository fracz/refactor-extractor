package org.jetbrains.jet.codegen;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import jet.JetObject;
import jet.typeinfo.TypeInfo;
import jet.typeinfo.TypeInfoProjection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverDescriptor;
import org.jetbrains.jet.lang.types.*;
import org.jetbrains.jet.lexer.JetTokens;
import org.jetbrains.jet.resolve.DescriptorRenderer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.commons.Method;

import java.util.*;

/**
 * @author yole
 * @author alex.tkachman
 */
public class JetTypeMapper {
    public static final Type TYPE_OBJECT = Type.getObjectType("java/lang/Object");
    public static final Type TYPE_TYPEINFO = Type.getType(TypeInfo.class);
    public static final Type TYPE_TYPEINFOPROJECTION = Type.getType(TypeInfoProjection.class);
    public static final Type TYPE_JET_OBJECT = Type.getType(JetObject.class);
    public static final Type TYPE_NOTHING = Type.getObjectType("jet/Nothing");
    public static final Type JL_INTEGER_TYPE = Type.getObjectType("java/lang/Integer");
    public static final Type JL_LONG_TYPE = Type.getObjectType("java/lang/Long");
    public static final Type JL_SHORT_TYPE = Type.getObjectType("java/lang/Short");
    public static final Type JL_BYTE_TYPE = Type.getObjectType("java/lang/Byte");
    public static final Type JL_CHAR_TYPE = Type.getObjectType("java/lang/Character");
    public static final Type JL_FLOAT_TYPE = Type.getObjectType("java/lang/Float");
    public static final Type JL_DOUBLE_TYPE = Type.getObjectType("java/lang/Double");
    public static final Type JL_BOOLEAN_TYPE = Type.getObjectType("java/lang/Boolean");
    public static final Type JL_NUMBER_TYPE = Type.getObjectType("java/lang/Number");
    public static final Type JL_STRING_BUILDER = Type.getObjectType("java/lang/StringBuilder");
    public static final Type JL_STRING_TYPE = Type.getObjectType("java/lang/String");

    public static final Type ARRAY_INT_TYPE = Type.getType(int[].class);
    public static final Type ARRAY_LONG_TYPE = Type.getType(long[].class);
    public static final Type ARRAY_SHORT_TYPE = Type.getType(short[].class);
    public static final Type ARRAY_BYTE_TYPE = Type.getType(byte[].class);
    public static final Type ARRAY_CHAR_TYPE = Type.getType(char[].class);
    public static final Type ARRAY_FLOAT_TYPE = Type.getType(float[].class);
    public static final Type ARRAY_DOUBLE_TYPE = Type.getType(double[].class);
    public static final Type ARRAY_BOOL_TYPE = Type.getType(boolean[].class);
    public static final Type ARRAY_GENERIC_TYPE = Type.getType(Object[].class);

    private final JetStandardLibrary standardLibrary;
    private final BindingContext bindingContext;
    private final Map<JetExpression, String> classNamesForAnonymousClasses = new HashMap<JetExpression, String>();
    private final Map<String, Integer> anonymousSubclassesCount = new HashMap<String, Integer>();

    private final HashMap<JetType,String> knowTypes = new HashMap<JetType, String>();
    public static final Type TYPE_FUNCTION1 = Type.getObjectType("jet/Function1");
    public static final Type TYPE_ITERATOR = Type.getObjectType("jet/Iterator");
    public static final Type TYPE_INT_RANGE = Type.getObjectType("jet/IntRange");
    public static final Type TYPE_SHARED_VAR = Type.getObjectType("jet/runtime/SharedVar$Object");
    public static final Type TYPE_SHARED_INT = Type.getObjectType("jet/runtime/SharedVar$Int");
    public static final Type TYPE_SHARED_DOUBLE = Type.getObjectType("jet/runtime/SharedVar$Double");
    public static final Type TYPE_SHARED_FLOAT = Type.getObjectType("jet/runtime/SharedVar$Float");
    public static final Type TYPE_SHARED_BYTE = Type.getObjectType("jet/runtime/SharedVar$Byte");
    public static final Type TYPE_SHARED_SHORT = Type.getObjectType("jet/runtime/SharedVar$Short");
    public static final Type TYPE_SHARED_CHAR = Type.getObjectType("jet/runtime/SharedVar$Char");
    public static final Type TYPE_SHARED_LONG = Type.getObjectType("jet/runtime/SharedVar$Long");
    public static final Type TYPE_SHARED_BOOLEAN = Type.getObjectType("jet/runtime/SharedVar$Boolean");

    public JetTypeMapper(JetStandardLibrary standardLibrary, BindingContext bindingContext) {
        this.standardLibrary = standardLibrary;
        this.bindingContext = bindingContext;
        initKnownTypes();
    }

    static String jvmName(PsiClass psiClass) {
        final String qName = psiClass.getQualifiedName();
        if (qName == null) {
            throw new UnsupportedOperationException("can't evaluate JVM name for anonymous class " + psiClass);
        }
        return qName.replace(".", "/");
    }

    public static boolean isIntPrimitive(Type type) {
        return type == Type.INT_TYPE || type == Type.SHORT_TYPE || type == Type.BYTE_TYPE || type == Type.CHAR_TYPE;
    }

    public static boolean isPrimitive(Type type) {
        return type == Type.INT_TYPE
                || type == Type.SHORT_TYPE
                || type == Type.BYTE_TYPE
                || type == Type.CHAR_TYPE
                || type == Type.SHORT_TYPE
                || type == Type.FLOAT_TYPE
                || type == Type.DOUBLE_TYPE
                || type == Type.LONG_TYPE
                || type == Type.BOOLEAN_TYPE
                || type == Type.VOID_TYPE;
    }

    static Type psiTypeToAsm(PsiType type) {
        if(type instanceof PsiArrayType) {
            PsiArrayType psiArrayType = (PsiArrayType) type;
            return Type.getType("[" + psiTypeToAsm(psiArrayType.getComponentType()).getDescriptor());
        }

        if (type instanceof PsiPrimitiveType) {
            if (type == PsiType.VOID) {
                return Type.VOID_TYPE;
            }
            if (type == PsiType.INT) {
                return Type.INT_TYPE;
            }
            if (type == PsiType.LONG) {
                return Type.LONG_TYPE;
            }
            if (type == PsiType.BOOLEAN) {
                return Type.BOOLEAN_TYPE;
            }
            if (type == PsiType.BYTE) {
                return Type.BYTE_TYPE;
            }
            if (type == PsiType.SHORT) {
                return Type.SHORT_TYPE;
            }
            if (type == PsiType.CHAR) {
                return Type.CHAR_TYPE;
            }
            if (type == PsiType.FLOAT) {
                return Type.FLOAT_TYPE;
            }
            if (type == PsiType.DOUBLE) {
                return Type.DOUBLE_TYPE;
            }
        }
        if (type instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) type).resolve();
            if (psiClass instanceof PsiTypeParameter) {
                final PsiClassType[] extendsListTypes = psiClass.getExtendsListTypes();
                if (extendsListTypes.length > 0) {
                    throw new UnsupportedOperationException("should return common supertype");
                }
                return TYPE_OBJECT;
            }
            if (psiClass == null) {
                throw new UnsupportedOperationException("unresolved PsiClassType: " + type);
            }
            return psiClassType(psiClass);
        }
        throw new UnsupportedOperationException("don't know how to map type " + type + " to ASM");
    }

    static Method getMethodDescriptor(PsiMethod method) {
        Type returnType = method.isConstructor() ? Type.VOID_TYPE : psiTypeToAsm(method.getReturnType());
        PsiParameter[] parameters = method.getParameterList().getParameters();
        Type[] parameterTypes = new Type[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = psiTypeToAsm(parameters[i].getType());
        }
        return new Method(method.isConstructor() ? "<init>" : method.getName(), Type.getMethodDescriptor(returnType, parameterTypes));
    }

    public static Type getBoxedType(final Type type) {
        switch (type.getSort()) {
            case Type.BYTE:
                return JL_BYTE_TYPE;
            case Type.BOOLEAN:
                return JL_BOOLEAN_TYPE;
            case Type.SHORT:
                return JL_SHORT_TYPE;
            case Type.CHAR:
                return JL_CHAR_TYPE;
            case Type.INT:
                return JL_INTEGER_TYPE;
            case Type.FLOAT:
                return JL_FLOAT_TYPE;
            case Type.LONG:
                return JL_LONG_TYPE;
            case Type.DOUBLE:
                return JL_DOUBLE_TYPE;
        }
        return type;
    }

    public boolean hasTypeInfoField(JetType type) {
        if(type.getConstructor().getParameters().size() > 0) {
            if(!(bindingContext.get(BindingContext.DESCRIPTOR_TO_DECLARATION, type.getConstructor().getDeclarationDescriptor()) instanceof PsiClass))
                return true;
        }

        for (JetType jetType : type.getConstructor().getSupertypes()) {
            if(hasTypeInfoField(jetType))
                return true;
        }

        ClassDescriptor outerClassDescriptor = CodegenUtil.getOuterClassDescriptor(type.getConstructor().getDeclarationDescriptor());
        if(outerClassDescriptor == null)
            return false;

        return hasTypeInfoField(outerClassDescriptor.getDefaultType());
    }

    public boolean hasDerivedTypeInfoField(JetType type, boolean exceptOwn) {
        if(!exceptOwn) {
            if(!CodegenUtil.isInterface(type))
                if(hasTypeInfoField(type))
                    return true;
        }

        for (JetType jetType : type.getConstructor().getSupertypes()) {
            if(hasDerivedTypeInfoField(jetType, false))
                return true;
        }

        return false;
    }

    public String jvmName(ClassDescriptor jetClass, OwnerKind kind) {
        PsiElement declaration = bindingContext.get(BindingContext.DESCRIPTOR_TO_DECLARATION, jetClass);
        if (declaration instanceof PsiClass) {
            return jvmName((PsiClass) declaration);
        }
        if (declaration instanceof JetObjectDeclaration && ((JetObjectDeclaration) declaration).isObjectLiteral()) {
            final PsiElement parent = declaration.getParent();
            if (parent instanceof JetClassObject) {
                JetClass containingClass = PsiTreeUtil.getParentOfType(parent, JetClass.class);
                final ClassDescriptor containingClassDescriptor = bindingContext.get(BindingContext.CLASS, containingClass);
                return jvmName(containingClassDescriptor, OwnerKind.IMPLEMENTATION) + "$$ClassObj";
            }
            @SuppressWarnings("SuspiciousMethodCalls") String className = classNamesForAnonymousClasses.get(declaration);
            if (className == null) {
                throw new UnsupportedOperationException("Unexpected forward reference to anonymous class " + declaration);
            }
            return className;
        }
        return jetJvmName(jetClass, kind);
    }

    public String jvmName(JetClassObject classObject) {
        final ClassDescriptor descriptor = bindingContext.get(BindingContext.CLASS, classObject.getObjectDeclaration());
        return jvmName(descriptor, OwnerKind.IMPLEMENTATION);
    }

    private static String jetJvmName(ClassDescriptor jetClass, OwnerKind kind) {
        if (jetClass.getKind() == ClassKind.OBJECT) {
            return jvmNameForImplementation(jetClass);
        }
        else if (kind == OwnerKind.IMPLEMENTATION) {
            return jvmNameForImplementation(jetClass);
        }
        else if (kind == OwnerKind.TRAIT_IMPL) {
            return jvmNameForTraitImpl(jetClass);
        }
        else {
            assert false : "Unsuitable kind";
            return "java/lang/Object";
        }
    }

    public Type jvmType(ClassDescriptor jetClass, OwnerKind kind) {
        if (jetClass == standardLibrary.getString()) {
            return Type.getType(String.class);
        }
        return Type.getType("L" + jvmName(jetClass, kind) + ";");
    }

    static Type psiClassType(PsiClass psiClass) {
        return Type.getType("L" + jvmName(psiClass) + ";");
    }

    static Type jetInterfaceType(ClassDescriptor classDescriptor) {
        return Type.getType("L" + jvmNameForInterface(classDescriptor) + ";");
    }

    static Type jetImplementationType(ClassDescriptor classDescriptor) {
        return Type.getType("L" + jvmNameForImplementation(classDescriptor) + ";");
    }

    public static String jvmName(JetNamespace namespace) {
        return NamespaceCodegen.getJVMClassName(namespace.getFQName());
    }

    static String jvmName(NamespaceDescriptor namespace) {
        return NamespaceCodegen.getJVMClassName(DescriptorRenderer.getFQName(namespace));
    }

    public static String jvmNameForInterface(ClassDescriptor descriptor) {
        return DescriptorRenderer.getFQName(descriptor).replace('.', '/');
    }

    public static String jvmNameForImplementation(ClassDescriptor descriptor) {
        return jvmNameForInterface(descriptor);
    }

    private static String jvmNameForTraitImpl(ClassDescriptor descriptor) {
        return jvmNameForInterface(descriptor) + "$$TImpl";
    }

    public String getOwner(DeclarationDescriptor descriptor, OwnerKind kind) {
        String owner;
        if (descriptor.getContainingDeclaration() instanceof NamespaceDescriptorImpl) {
            owner = jvmName((NamespaceDescriptor) descriptor.getContainingDeclaration());
        }
        else if (descriptor.getContainingDeclaration() instanceof ClassDescriptor) {
            ClassDescriptor classDescriptor = (ClassDescriptor) descriptor.getContainingDeclaration();
            if (kind instanceof OwnerKind.DelegateKind) {
                kind = OwnerKind.IMPLEMENTATION;
            }
            else {
                assert classDescriptor != null;
                if (classDescriptor.getKind() == ClassKind.OBJECT) {
                    kind = OwnerKind.IMPLEMENTATION;
                }
            }
            owner = jvmName(classDescriptor, kind);
        }
        else {
            throw new UnsupportedOperationException("don't know how to generate owner for parent " + descriptor.getContainingDeclaration());
        }
        return owner;
    }

    @NotNull public Type mapReturnType(@NotNull final JetType jetType, OwnerKind kind) {
        if (jetType.equals(JetStandardClasses.getUnitType()) || jetType.equals(JetStandardClasses.getNothingType())) {
            return Type.VOID_TYPE;
        }
        if (jetType.equals(JetStandardClasses.getNullableNothingType())) {
            return TYPE_OBJECT;
        }
        return mapType(jetType, kind);
    }

    @NotNull public Type mapReturnType(final JetType jetType) {
        return mapReturnType(jetType, OwnerKind.IMPLEMENTATION);
    }

    @NotNull public Type mapType(final JetType jetType) {
        return mapType(jetType, OwnerKind.IMPLEMENTATION);
    }

    @NotNull public Type mapType(@NotNull final JetType jetType, OwnerKind kind) {
        if (jetType.equals(JetStandardClasses.getNothingType()) || jetType.equals(JetStandardClasses.getNullableNothingType())) {
            return TYPE_NOTHING;
        }
        if (jetType.equals(standardLibrary.getIntType())) {
            return Type.INT_TYPE;
        }
        if (jetType.equals(standardLibrary.getNullableIntType())) {
            return JL_INTEGER_TYPE;
        }
        if (jetType.equals(standardLibrary.getLongType())) {
            return Type.LONG_TYPE;
        }
        if (jetType.equals(standardLibrary.getNullableLongType())) {
            return JL_LONG_TYPE;
        }
        if (jetType.equals(standardLibrary.getShortType())) {
            return Type.SHORT_TYPE;
        }
        if (jetType.equals(standardLibrary.getNullableShortType())) {
            return JL_SHORT_TYPE;
        }
        if (jetType.equals(standardLibrary.getByteType())) {
            return Type.BYTE_TYPE;
        }
        if (jetType.equals(standardLibrary.getNullableByteType())) {
            return JL_BYTE_TYPE;
        }
        if (jetType.equals(standardLibrary.getCharType())) {
            return Type.CHAR_TYPE;
        }
        if (jetType.equals(standardLibrary.getNullableCharType())) {
            return JL_CHAR_TYPE;
        }
        if (jetType.equals(standardLibrary.getFloatType())) {
            return Type.FLOAT_TYPE;
        }
        if (jetType.equals(standardLibrary.getNullableFloatType())) {
            return JL_FLOAT_TYPE;
        }
        if (jetType.equals(standardLibrary.getDoubleType())) {
            return Type.DOUBLE_TYPE;
        }
        if (jetType.equals(standardLibrary.getNullableDoubleType())) {
            return JL_DOUBLE_TYPE;
        }
        if (jetType.equals(standardLibrary.getBooleanType())) {
            return Type.BOOLEAN_TYPE;
        }
        if (jetType.equals(standardLibrary.getNullableBooleanType())) {
            return JL_BOOLEAN_TYPE;
        }
        if(jetType.equals(standardLibrary.getByteArrayType())){
            return ARRAY_BYTE_TYPE;
        }
        if(jetType.equals(standardLibrary.getShortArrayType())){
            return ARRAY_SHORT_TYPE;
        }
        if(jetType.equals(standardLibrary.getIntArrayType())){
            return ARRAY_INT_TYPE;
        }
        if(jetType.equals(standardLibrary.getLongArrayType())){
            return ARRAY_LONG_TYPE;
        }
        if(jetType.equals(standardLibrary.getFloatArrayType())){
            return ARRAY_FLOAT_TYPE;
        }
        if(jetType.equals(standardLibrary.getDoubleArrayType())){
            return ARRAY_DOUBLE_TYPE;
        }
        if(jetType.equals(standardLibrary.getCharArrayType())){
            return ARRAY_CHAR_TYPE;
        }
        if(jetType.equals(standardLibrary.getBooleanArrayType())){
            return ARRAY_BOOL_TYPE;
        }

        if (jetType.equals(standardLibrary.getStringType()) || jetType.equals(standardLibrary.getNullableStringType())) {
            return Type.getType(String.class);
        }

        DeclarationDescriptor descriptor = jetType.getConstructor().getDeclarationDescriptor();
        if (standardLibrary.getArray().equals(descriptor)) {
            if (jetType.getArguments().size() != 1) {
                throw new UnsupportedOperationException("arrays must have one type argument");
            }
            JetType memberType = jetType.getArguments().get(0).getType();
            if(!isGenericsArray(jetType))
                return Type.getType("[" + boxType(mapType(memberType, kind)).getDescriptor());
            else
                return ARRAY_GENERIC_TYPE;
        }
        if (JetStandardClasses.getAny().equals(descriptor)) {
            return Type.getType(Object.class);
        }

        if (descriptor instanceof ClassDescriptor) {
            return Type.getObjectType(jvmName((ClassDescriptor) descriptor, kind));
        }

        if (descriptor instanceof TypeParameterDescriptor) {
            return mapType(((TypeParameterDescriptor) descriptor).getBoundsAsType(), kind);
        }

        throw new UnsupportedOperationException("Unknown type " + jetType);
    }

    public static Type unboxType(final Type type) {
        if (type == JL_INTEGER_TYPE) {
            return Type.INT_TYPE;
        }
        else if (type == JL_BOOLEAN_TYPE) {
            return Type.BOOLEAN_TYPE;
        }
        else if (type == JL_CHAR_TYPE) {
            return Type.CHAR_TYPE;
        }
        else if (type == JL_SHORT_TYPE) {
            return Type.SHORT_TYPE;
        }
        else if (type == JL_LONG_TYPE) {
            return Type.LONG_TYPE;
        }
        else if (type == JL_BYTE_TYPE) {
            return Type.BYTE_TYPE;
        }
        else if (type == JL_FLOAT_TYPE) {
            return Type.FLOAT_TYPE;
        }
        else if (type == JL_DOUBLE_TYPE) {
            return Type.DOUBLE_TYPE;
        }
        throw new UnsupportedOperationException("Unboxing: " + type);
    }

    public static Type boxType(Type asmType) {
        switch (asmType.getSort()) {
            case Type.VOID:
                return Type.VOID_TYPE;
            case Type.BYTE:
                return JL_BYTE_TYPE;
            case Type.BOOLEAN:
                return JL_BOOLEAN_TYPE;
            case Type.SHORT:
                return JL_SHORT_TYPE;
            case Type.CHAR:
                return JL_CHAR_TYPE;
            case Type.INT:
                return JL_INTEGER_TYPE;
            case Type.FLOAT:
                return JL_FLOAT_TYPE;
            case Type.LONG:
                return JL_LONG_TYPE;
            case Type.DOUBLE:
                return JL_DOUBLE_TYPE;
        }

        return asmType;
    }


    private Method mapSignature(FunctionDescriptor f, List<Type> valueParameterTypes, OwnerKind kind) {
        final ReceiverDescriptor receiverTypeRef = f.getReceiverParameter();
        final JetType receiverType = !receiverTypeRef.exists() ? null : receiverTypeRef.getType();
        final List<ValueParameterDescriptor> parameters = f.getValueParameters();
        List<Type> parameterTypes = new ArrayList<Type>();
        if (receiverType != null) {
            parameterTypes.add(mapType(receiverType));
        }
        if(kind == OwnerKind.TRAIT_IMPL) {
            ClassDescriptor containingDeclaration = (ClassDescriptor) f.getContainingDeclaration();
            JetType jetType = TraitImplBodyCodegen.getSuperClass(containingDeclaration, bindingContext);
            Type type = mapType(jetType);
            if(type.getInternalName().equals("java/lang/Object")) {
                jetType = containingDeclaration.getDefaultType();
                type = mapType(jetType);
            }
            valueParameterTypes.add(type);
            parameterTypes.add(type);
        }
        for (ValueParameterDescriptor parameter : parameters) {
            Type type = mapType(parameter.getOutType());
            valueParameterTypes.add(type);
            parameterTypes.add(type);
        }
        for (int n = f.getTypeParameters().size(); n > 0; n--) {
            parameterTypes.add(TYPE_TYPEINFO);
        }
        Type returnType = mapReturnType(f.getReturnType());
        return new Method(f.getName(), returnType, parameterTypes.toArray(new Type[parameterTypes.size()]));
    }

    public CallableMethod mapToCallableMethod(PsiNamedElement declaration, OwnerKind kind) {
        if (declaration instanceof PsiMethod) {
            return mapToCallableMethod((PsiMethod) declaration);
        }
        if (!(declaration instanceof JetNamedFunction)) {
            throw new UnsupportedOperationException("unknown declaration type " + declaration);
        }
        JetNamedFunction f = (JetNamedFunction) declaration;
        final FunctionDescriptor functionDescriptor = bindingContext.get(BindingContext.FUNCTION, f);
        assert functionDescriptor != null;
        return mapToCallableMethod(functionDescriptor, kind);
    }

    static CallableMethod mapToCallableMethod(PsiMethod method) {
        final PsiClass containingClass = method.getContainingClass();
        String owner = jvmName(containingClass);
        Method signature = getMethodDescriptor(method);
        List<Type> valueParameterTypes = new ArrayList<Type>();
        Collections.addAll(valueParameterTypes, signature.getArgumentTypes());
        int opcode;
        boolean ownerFromCall = false;
        DeclarationDescriptor thisClass = null;
        if (method.isConstructor()) {
            opcode = Opcodes.INVOKESPECIAL;
        }
        else if (method.hasModifierProperty(PsiModifier.STATIC)) {
            opcode = Opcodes.INVOKESTATIC;
        }
        else {
            assert containingClass != null;
            if (containingClass.isInterface()) {
                opcode = Opcodes.INVOKEINTERFACE;
            }
            else {
                opcode = Opcodes.INVOKEVIRTUAL;
            }
            ownerFromCall = true;
            thisClass = JetStandardClasses.getAny(); // todo - this is hack, correct descriptor needed
        }
        final CallableMethod result = new CallableMethod(owner, signature, opcode, valueParameterTypes);
        result.setNeedsThis(thisClass);
        result.setOwnerFromCall(ownerFromCall);
        return result;
    }

    public CallableMethod mapToCallableMethod(FunctionDescriptor functionDescriptor, OwnerKind kind) {
        if(functionDescriptor == null)
            return null;

        final DeclarationDescriptor functionParent = functionDescriptor.getContainingDeclaration();
        final List<Type> valueParameterTypes = new ArrayList<Type>();
        Method descriptor = mapSignature(functionDescriptor.getOriginal(), valueParameterTypes, kind);
        String owner;
        int invokeOpcode;
        ClassDescriptor thisClass;
        if (functionParent instanceof NamespaceDescriptor) {
            owner = NamespaceCodegen.getJVMClassName(DescriptorRenderer.getFQName(functionParent));
            invokeOpcode = Opcodes.INVOKESTATIC;
            thisClass = null;
        }
        else if (functionParent instanceof ClassDescriptor) {
            ClassDescriptor containingClass = (ClassDescriptor) functionParent;
            owner = jvmName(containingClass, OwnerKind.IMPLEMENTATION);
            invokeOpcode = CodegenUtil.isInterface(containingClass)
                    ? Opcodes.INVOKEINTERFACE
                    : Opcodes.INVOKEVIRTUAL;
            thisClass = containingClass;
        }
        else {
            throw new UnsupportedOperationException("unknown function parent");
        }

        final CallableMethod result = new CallableMethod(owner, descriptor, invokeOpcode, valueParameterTypes);
        result.setAcceptsTypeArguments(true);
        result.setNeedsThis(thisClass);
        if(functionDescriptor.getReceiverParameter().exists()) {
            result.setNeedsReceiver(functionDescriptor.getReceiverParameter().getType().getConstructor().getDeclarationDescriptor());
        }

        return result;
    }

    public Method mapSignature(String name, FunctionDescriptor f) {
        final ReceiverDescriptor receiver = f.getReceiverParameter();
        final List<ValueParameterDescriptor> parameters = f.getValueParameters();
        List<Type> parameterTypes = new ArrayList<Type>();
        if (receiver.exists()) {
            parameterTypes.add(mapType(receiver.getType()));
        }
        for (ValueParameterDescriptor parameter : parameters) {
            parameterTypes.add(mapType(parameter.getOutType()));
        }
        Type returnType = mapReturnType(f.getReturnType());
        return new Method(name, returnType, parameterTypes.toArray(new Type[parameterTypes.size()]));
    }

    public String genericSignature(FunctionDescriptor f) {
        StringBuffer answer = new StringBuffer();
        final List<TypeParameterDescriptor> typeParameters = f.getTypeParameters();
        if (!typeParameters.isEmpty()) {
            answer.append('<');
            for (TypeParameterDescriptor p : typeParameters) {
                appendTypeParameterSignature(answer, p);
            }
            answer.append('>');
        }

        answer.append('(');
        for (ValueParameterDescriptor p : f.getValueParameters()) {
            appendType(answer, p.getOutType());
        }
        answer.append(')');

        appendType(answer, f.getReturnType());

        return answer.toString();
    }

    private void appendType(StringBuffer answer, JetType type) {
        answer.append(mapType(type).getDescriptor()); // TODO: type parameter references!
    }

    private static void appendTypeParameterSignature(StringBuffer answer, TypeParameterDescriptor p) {
        answer.append(p.getName()); // TODO: BOUND!
    }

    public Method mapGetterSignature(PropertyDescriptor descriptor, OwnerKind kind) {
        Type returnType = mapType(descriptor.getOutType());
        if(kind != OwnerKind.TRAIT_IMPL)
            return new Method(PropertyCodegen.getterName(descriptor.getName()), returnType, new Type[0]);
        else {
            ClassDescriptor containingDeclaration = (ClassDescriptor) descriptor.getContainingDeclaration();
            return new Method(PropertyCodegen.getterName(descriptor.getName()), returnType, new Type[] { mapType(containingDeclaration.getDefaultType()) });
        }
    }

    public Method mapSetterSignature(PropertyDescriptor descriptor, OwnerKind kind) {
        final JetType inType = descriptor.getInType();
        if (inType == null) {
            return null;
        }
        Type paramType = mapType(inType);
        if(kind != OwnerKind.TRAIT_IMPL) {
            return new Method(PropertyCodegen.setterName(descriptor.getName()), Type.VOID_TYPE, new Type[] { paramType });
        }
        else {
            ClassDescriptor containingDeclaration = (ClassDescriptor) descriptor.getContainingDeclaration();
            return new Method(PropertyCodegen.setterName(descriptor.getName()), Type.VOID_TYPE, new Type[] { mapType(containingDeclaration.getDefaultType()), paramType });
        }
    }

    private Method mapConstructorSignature(ConstructorDescriptor descriptor, List<Type> valueParameterTypes) {
        List<ValueParameterDescriptor> parameters = descriptor.getOriginal().getValueParameters();
        List<Type> parameterTypes = new ArrayList<Type>();
        ClassDescriptor classDescriptor = descriptor.getContainingDeclaration();
        if (CodegenUtil.hasThis0(classDescriptor)) {
            parameterTypes.add(jvmType(CodegenUtil.getOuterClassDescriptor(classDescriptor), OwnerKind.IMPLEMENTATION));
        }
        for (ValueParameterDescriptor parameter : parameters) {
            final Type type = mapType(parameter.getOutType());
            parameterTypes.add(type);
            valueParameterTypes.add(type);
        }

        PsiElement psiElement = bindingContext.get(BindingContext.DESCRIPTOR_TO_DECLARATION, classDescriptor);
        if(!(psiElement instanceof PsiClass)) {
            List<TypeParameterDescriptor> typeParameters = classDescriptor.getTypeConstructor().getParameters();
            for (int n = typeParameters.size(); n > 0; n--) {
                parameterTypes.add(TYPE_TYPEINFO);
            }
        }

        return new Method("<init>", Type.VOID_TYPE, parameterTypes.toArray(new Type[parameterTypes.size()]));
    }

    public CallableMethod mapToCallableMethod(ConstructorDescriptor descriptor, OwnerKind kind) {
        List<Type> valueParameterTypes = new ArrayList<Type>();
        final Method method = mapConstructorSignature(descriptor, valueParameterTypes);
        String owner = jvmName(descriptor.getContainingDeclaration(), kind);
        final CallableMethod result = new CallableMethod(owner, method, Opcodes.INVOKESPECIAL, valueParameterTypes);
        result.setAcceptsTypeArguments(!(bindingContext.get(BindingContext.DESCRIPTOR_TO_DECLARATION, descriptor.getContainingDeclaration()) instanceof PsiClass));
        return result;
    }

    static int getAccessModifiers(JetDeclaration p, int defaultFlags) {
        int flags = 0;
        if (p.hasModifier(JetTokens.PUBLIC_KEYWORD)) {
            flags |= Opcodes.ACC_PUBLIC;
        }
        else if (p.hasModifier(JetTokens.PRIVATE_KEYWORD)) {
            flags |= Opcodes.ACC_PRIVATE;
        }
        else {
            flags |= defaultFlags;
        }
        return flags;
    }

    String classNameForAnonymousClass(JetExpression expression) {
        String name = classNamesForAnonymousClasses.get(expression);
        if (name != null) {
            return name;
        }

        @SuppressWarnings("unchecked") JetNamedDeclaration container = PsiTreeUtil.getParentOfType(expression, JetNamespace.class, JetClass.class, JetObjectDeclaration.class);

        String baseName;
        if (container instanceof JetNamespace) {
            baseName = NamespaceCodegen.getJVMClassName(((JetNamespace) container).getFQName());
        }
        else {
            ClassDescriptor aClass = bindingContext.get(BindingContext.CLASS, container);
            baseName = jvmNameForInterface(aClass);
        }

        Integer count = anonymousSubclassesCount.get(baseName);
        if (count == null) count = 0;

        anonymousSubclassesCount.put(baseName, count + 1);

        final String className = baseName + "$" + (count + 1);
        classNamesForAnonymousClasses.put(expression, className);
        return className;
    }

    public Collection<String> allJvmNames(JetClassOrObject jetClass) {
        Set<String> result = new HashSet<String>();
        final ClassDescriptor classDescriptor = bindingContext.get(BindingContext.CLASS, jetClass);
        if (classDescriptor != null) {
            result.add(jvmName(classDescriptor, OwnerKind.IMPLEMENTATION));
        }
        return result;
    }

    private void initKnownTypes() {
        knowTypes.put(standardLibrary.getIntType(), "INT_TYPE_INFO");
        knowTypes.put(standardLibrary.getNullableIntType(), "NULLABLE_INT_TYPE_INFO");
        knowTypes.put(standardLibrary.getLongType(), "LONG_TYPE_INFO");
        knowTypes.put(standardLibrary.getNullableLongType(), "NULLABLE_LONG_TYPE_INFO");
        knowTypes.put(standardLibrary.getShortType(),"SHORT_TYPE_INFO");
        knowTypes.put(standardLibrary.getNullableShortType(),"NULLABLE_SHORT_TYPE_INFO");
        knowTypes.put(standardLibrary.getByteType(),"BYTE_TYPE_INFO");
        knowTypes.put(standardLibrary.getNullableByteType(),"NULLABLE_BYTE_TYPE_INFO");
        knowTypes.put(standardLibrary.getCharType(),"CHAR_TYPE_INFO");
        knowTypes.put(standardLibrary.getNullableCharType(),"NULLABLE_CHAR_TYPE_INFO");
        knowTypes.put(standardLibrary.getFloatType(),"FLOAT_TYPE_INFO");
        knowTypes.put(standardLibrary.getNullableFloatType(),"NULLABLE_FLOAT_TYPE_INFO");
        knowTypes.put(standardLibrary.getDoubleType(),"DOUBLE_TYPE_INFO");
        knowTypes.put(standardLibrary.getNullableDoubleType(),"NULLABLE_DOUBLE_TYPE_INFO");
        knowTypes.put(standardLibrary.getBooleanType(),"BOOLEAN_TYPE_INFO");
        knowTypes.put(standardLibrary.getNullableBooleanType(),"NULLABLE_BOOLEAN_TYPE_INFO");
        knowTypes.put(standardLibrary.getStringType(),"STRING_TYPE_INFO");
        knowTypes.put(standardLibrary.getNullableStringType(),"NULLABLE_STRING_TYPE_INFO");
        knowTypes.put(standardLibrary.getTuple0Type(),"TUPLE0_TYPE_INFO");
        knowTypes.put(standardLibrary.getNullableTuple0Type(),"NULLABLE_TUPLE0_TYPE_INFO");

        knowTypes.put(standardLibrary.getIntArrayType(), "INT_ARRAY_TYPE_INFO");
        knowTypes.put(standardLibrary.getLongArrayType(), "LONG_ARRAY_TYPE_INFO");
        knowTypes.put(standardLibrary.getShortArrayType(),"SHORT_ARRAY_TYPE_INFO");
        knowTypes.put(standardLibrary.getByteArrayType(),"BYTE_ARRAY_TYPE_INFO");
        knowTypes.put(standardLibrary.getCharArrayType(),"CHAR_ARRAY_TYPE_INFO");
        knowTypes.put(standardLibrary.getFloatArrayType(),"FLOAT_ARRAY_TYPE_INFO");
        knowTypes.put(standardLibrary.getDoubleArrayType(),"DOUBLE_ARRAY_TYPE_INFO");
        knowTypes.put(standardLibrary.getBooleanArrayType(),"BOOLEAN_ARRAY_TYPE_INFO");
    }

    public String isKnownTypeInfo(JetType jetType) {
        return knowTypes.get(jetType);
    }

    public boolean isGenericsArray(JetType type) {
        DeclarationDescriptor declarationDescriptor = type.getConstructor().getDeclarationDescriptor();
        if(declarationDescriptor instanceof TypeParameterDescriptor)
            return true;

        if(standardLibrary.getArray().equals(declarationDescriptor))
            return isGenericsArray(type.getArguments().get(0).getType());

        return false;
    }

    public JetType getGenericsElementType(JetType arrayType) {
        JetType type = arrayType.getArguments().get(0).getType();
        return isGenericsArray(type) ? type : null;
    }
}
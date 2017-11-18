package org.jetbrains.jet.codegen;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor;
import org.jetbrains.jet.lang.descriptors.PropertyDescriptor;
import org.jetbrains.jet.lang.descriptors.PropertySetterDescriptor;
import org.jetbrains.jet.lang.descriptors.VariableDescriptor;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.constants.CompileTimeConstant;
import org.jetbrains.jet.lang.resolve.java.JvmAbi;
import org.jetbrains.jet.lang.resolve.java.JvmStdlibNames;
import org.jetbrains.jet.lexer.JetTokens;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

/**
 * @author max
 */
public class PropertyCodegen {
    private final GenerationState state;
    private final FunctionCodegen functionCodegen;
    private final ClassBuilder v;
    private final OwnerKind kind;

    public PropertyCodegen(CodegenContext context, ClassBuilder v, FunctionCodegen functionCodegen, GenerationState state) {
        this.v = v;
        this.functionCodegen = functionCodegen;
        this.state = state;
        this.kind = context.getContextKind();
    }

    public void gen(JetProperty p) {
        final VariableDescriptor descriptor = state.getBindingContext().get(BindingContext.VARIABLE, p);
        if (!(descriptor instanceof PropertyDescriptor)) {
            throw new UnsupportedOperationException("expect a property to have a property descriptor");
        }
        final PropertyDescriptor propertyDescriptor = (PropertyDescriptor) descriptor;
        if (kind == OwnerKind.NAMESPACE || kind == OwnerKind.IMPLEMENTATION || kind ==OwnerKind.TRAIT_IMPL ) {
            if(kind != OwnerKind.TRAIT_IMPL)
                generateBackingField(p, propertyDescriptor);
            generateGetter(p, propertyDescriptor);
            generateSetter(p, propertyDescriptor);
        }
        else if (kind instanceof OwnerKind.DelegateKind) {
            generateDefaultGetter(propertyDescriptor, Opcodes.ACC_PUBLIC, p);
            if (propertyDescriptor.isVar()) {
                generateDefaultSetter(propertyDescriptor, Opcodes.ACC_PUBLIC, p);
            }
        }
    }

    private void generateBackingField(JetProperty p, PropertyDescriptor propertyDescriptor) {
        if (state.getBindingContext().get(BindingContext.BACKING_FIELD_REQUIRED, propertyDescriptor)) {
            DeclarationDescriptor containingDeclaration = propertyDescriptor.getContainingDeclaration();
            if(CodegenUtil.isInterface(containingDeclaration))
                return;

            Object value = null;
            final JetExpression initializer = p.getInitializer();
            if (initializer != null) {
                if (initializer instanceof JetConstantExpression) {
                    CompileTimeConstant<?> compileTimeValue = state.getBindingContext().get(BindingContext.COMPILE_TIME_VALUE, initializer);
                    value = compileTimeValue != null ? compileTimeValue.getValue() : null;
                }
            }
            int modifiers;
            if (kind == OwnerKind.NAMESPACE) {
                int access = isExternallyAccessible(p) ? Opcodes.ACC_PUBLIC : Opcodes.ACC_PRIVATE;
                modifiers = access | Opcodes.ACC_STATIC;
            }
            else {
                modifiers = Opcodes.ACC_PRIVATE;
            }
            if (!propertyDescriptor.isVar()) {
                modifiers |= Opcodes.ACC_FINAL;
            }
            v.newField(p, modifiers, p.getName(), state.getTypeMapper().mapType(propertyDescriptor.getOutType()).getDescriptor(), null, value);
        }
    }

    private void generateGetter(JetProperty p, PropertyDescriptor propertyDescriptor) {
        final JetPropertyAccessor getter = p.getGetter();
        if (getter != null) {
            if (getter.getBodyExpression() != null) {
                JvmPropertyAccessorSignature signature = state.getTypeMapper().mapGetterSignature(propertyDescriptor, kind);
                functionCodegen.generateMethod(getter, signature.getJvmMethodSignature(), signature.getPropertyTypeKotlinSignature(), propertyDescriptor.getGetter());
            }
            else if (!getter.hasModifier(JetTokens.PRIVATE_KEYWORD)) {
                generateDefaultGetter(p, getter);
            }
        }
        else if (isExternallyAccessible(p)) {
            generateDefaultGetter(p, p);
        }
    }

    private static boolean isExternallyAccessible(JetProperty p) {
        return !p.hasModifier(JetTokens.PRIVATE_KEYWORD);
    }

    private void generateSetter(JetProperty p, PropertyDescriptor propertyDescriptor) {
        final JetPropertyAccessor setter = p.getSetter();
        if (setter != null) {
            if (setter.getBodyExpression() != null) {
                final PropertySetterDescriptor setterDescriptor = propertyDescriptor.getSetter();
                assert setterDescriptor != null;
                JvmPropertyAccessorSignature signature = state.getTypeMapper().mapSetterSignature(propertyDescriptor, kind);
                functionCodegen.generateMethod(setter, signature.getJvmMethodSignature(), signature.getPropertyTypeKotlinSignature(), setterDescriptor);
            }
            else if (!p.hasModifier(JetTokens.PRIVATE_KEYWORD)) {
                generateDefaultSetter(p, setter);
            }
        }
        else if (isExternallyAccessible(p) && p.isVar()) {
            generateDefaultSetter(p, p);
        }
    }

    private void generateDefaultGetter(JetProperty p, JetDeclaration declaration) {
        final PropertyDescriptor propertyDescriptor = (PropertyDescriptor) state.getBindingContext().get(BindingContext.VARIABLE, p);
        int flags = JetTypeMapper.getAccessModifiers(declaration, Opcodes.ACC_PUBLIC);
        generateDefaultGetter(propertyDescriptor, flags, p);
    }

    public void generateDefaultGetter(PropertyDescriptor propertyDescriptor, int flags, PsiElement origin) {
        if (kind == OwnerKind.TRAIT_IMPL) {
            return;
        }

        if (kind == OwnerKind.NAMESPACE) {
            flags |= Opcodes.ACC_STATIC;
        }

        PsiElement psiElement = state.getBindingContext().get(BindingContext.DESCRIPTOR_TO_DECLARATION, propertyDescriptor.getContainingDeclaration());
        boolean isTrait = psiElement instanceof JetClass && ((JetClass) psiElement).isTrait();
        if(isTrait && !(kind instanceof OwnerKind.DelegateKind))
            flags |= Opcodes.ACC_ABSTRACT;

        JvmPropertyAccessorSignature signature = state.getTypeMapper().mapGetterSignature(propertyDescriptor, kind);
        final String descriptor = signature.getJvmMethodSignature().getAsmMethod().getDescriptor();
        String getterName = getterName(propertyDescriptor.getName());
        MethodVisitor mv = v.newMethod(origin, flags, getterName, descriptor, null, null);
        generateJetPropertyAnnotation(mv, signature.getPropertyTypeKotlinSignature(), signature.getJvmMethodSignature().getKotlinTypeParameter());
        if (v.generateCode() && (!isTrait || kind instanceof OwnerKind.DelegateKind)) {
            mv.visitCode();
            InstructionAdapter iv = new InstructionAdapter(mv);
            if (kind != OwnerKind.NAMESPACE) {
                iv.load(0, JetTypeMapper.TYPE_OBJECT);
            }
            final Type type = state.getTypeMapper().mapType(propertyDescriptor.getOutType());
            if (kind instanceof OwnerKind.DelegateKind) {
                OwnerKind.DelegateKind dk = (OwnerKind.DelegateKind) kind;
                dk.getDelegate().put(JetTypeMapper.TYPE_OBJECT, iv);
                iv.invokeinterface(dk.getOwnerClass(), getterName, descriptor);
            }
            else {
                iv.visitFieldInsn(kind == OwnerKind.NAMESPACE ? Opcodes.GETSTATIC : Opcodes.GETFIELD,
                        state.getTypeMapper().getOwner(propertyDescriptor, kind), propertyDescriptor.getName(),
                        type.getDescriptor());
            }
            iv.areturn(type);
            FunctionCodegen.endVisit(mv, "getter", origin);
        }
    }

    public static void generateJetPropertyAnnotation(MethodVisitor mv, @NotNull String kotlinType, @NotNull String typeParameters) {
        AnnotationVisitor annotationVisitor = mv.visitAnnotation(JvmStdlibNames.JET_PROPERTY.getDescriptor(), true);
        if (kotlinType.length() > 0) {
            annotationVisitor.visit(JvmStdlibNames.JET_PROPERTY_TYPE_FIELD, kotlinType);
        }
        if (typeParameters.length() > 0) {
            annotationVisitor.visit(JvmStdlibNames.JET_PROPERTY_TYPE_PARAMETERS_FIELD, typeParameters);
        }
        annotationVisitor.visitEnd();
    }

    private void generateDefaultSetter(JetProperty p, JetDeclaration declaration) {
        final PropertyDescriptor propertyDescriptor = (PropertyDescriptor) state.getBindingContext().get(BindingContext.VARIABLE, p);
        int flags = JetTypeMapper.getAccessModifiers(declaration, Opcodes.ACC_PUBLIC);
        generateDefaultSetter(propertyDescriptor, flags, p);
    }

    public void generateDefaultSetter(PropertyDescriptor propertyDescriptor, int flags, PsiElement origin) {
        if (kind == OwnerKind.TRAIT_IMPL) {
            return;
        }

        if (kind == OwnerKind.NAMESPACE) {
            flags |= Opcodes.ACC_STATIC;
        }

        PsiElement psiElement = state.getBindingContext().get(BindingContext.DESCRIPTOR_TO_DECLARATION, propertyDescriptor.getContainingDeclaration());
        boolean isTrait = psiElement instanceof JetClass && ((JetClass) psiElement).isTrait();
        if(isTrait && !(kind instanceof OwnerKind.DelegateKind))
            flags |= Opcodes.ACC_ABSTRACT;

        JvmPropertyAccessorSignature signature = state.getTypeMapper().mapSetterSignature(propertyDescriptor, kind);
        final String descriptor = signature.getJvmMethodSignature().getAsmMethod().getDescriptor();
        MethodVisitor mv = v.newMethod(origin, flags, setterName(propertyDescriptor.getName()), descriptor, null, null);
        generateJetPropertyAnnotation(mv, signature.getPropertyTypeKotlinSignature(), signature.getJvmMethodSignature().getKotlinTypeParameter());
        if (v.generateCode() && (!isTrait || kind instanceof OwnerKind.DelegateKind)) {
            mv.visitCode();
            InstructionAdapter iv = new InstructionAdapter(mv);
            final Type type = state.getTypeMapper().mapType(propertyDescriptor.getOutType());
            int paramCode = 0;
            if (kind != OwnerKind.NAMESPACE) {
                iv.load(0, JetTypeMapper.TYPE_OBJECT);
                paramCode = 1;
            }

            if (kind instanceof OwnerKind.DelegateKind) {
                OwnerKind.DelegateKind dk = (OwnerKind.DelegateKind) kind;
                iv.load(0, JetTypeMapper.TYPE_OBJECT);
                dk.getDelegate().put(JetTypeMapper.TYPE_OBJECT, iv);

                iv.load(paramCode, type);
                iv.invokeinterface(dk.getOwnerClass(), setterName(propertyDescriptor.getName()), descriptor);
            }
            else {
                iv.load(paramCode, type);
                iv.visitFieldInsn(kind == OwnerKind.NAMESPACE ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD,
                        state.getTypeMapper().getOwner(propertyDescriptor, kind), propertyDescriptor.getName(),
                        type.getDescriptor());
            }

            iv.visitInsn(Opcodes.RETURN);
            FunctionCodegen.endVisit(mv, "setter", origin);
            mv.visitEnd();
        }
    }

    public static String getterName(String propertyName) {
        return JvmAbi.GETTER_PREFIX + StringUtil.capitalizeWithJavaBeanConvention(propertyName);
    }

    public static String setterName(String propertyName) {
        return JvmAbi.SETTER_PREFIX + StringUtil.capitalizeWithJavaBeanConvention(propertyName);
    }
}
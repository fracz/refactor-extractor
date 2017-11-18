package org.jetbrains.jet.codegen;

import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.types.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import java.util.List;

/**
 * @author max
 */
public class FunctionCodegen {
    private final ClassVisitor v;
    private final BindingContext bindingContext;
    private final JetStandardLibrary standardLibrary;
    private final JetTypeMapper typeMapper;

    public FunctionCodegen(ClassVisitor v, JetStandardLibrary standardLibrary, BindingContext bindingContext) {
        this.v = v;
        this.bindingContext = bindingContext;
        this.standardLibrary = standardLibrary;
        typeMapper = new JetTypeMapper(standardLibrary);
    }

    public void gen(JetFunction f, JetNamespace owner) {
        final List<JetParameter> parameters = f.getValueParameters();
        Type[] parameterTypes = new Type[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            parameterTypes[i] = typeMapper.mapType(bindingContext.resolveTypeReference(parameters.get(i).getTypeReference()));
        }
        final JetTypeReference returnTypeRef = f.getReturnTypeRef();
        Type returnType;
        if (returnTypeRef == null) {
            final FunctionDescriptor functionDescriptor = bindingContext.getFunctionDescriptor(f);
            final JetType type = functionDescriptor.getUnsubstitutedReturnType();
            returnType = typeMapper.mapType(type);
        }
        else {
            returnType = typeMapper.mapType(bindingContext.resolveTypeReference(returnTypeRef));
        }
        Method method = new Method(f.getName(), returnType, parameterTypes);
        final MethodVisitor mv = v.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                method.getName(), method.getDescriptor(), null, null);
        mv.visitCode();
        final JetExpression bodyExpression = f.getBodyExpression();
        FrameMap frameMap = new FrameMap();

        List<ValueParameterDescriptor> parameDescrs = bindingContext.getFunctionDescriptor(f).getUnsubstitutedValueParameters();

        for (ValueParameterDescriptor parameter : parameDescrs) {
            frameMap.enter(parameter);
        }

        bodyExpression.accept(new ExpressionCodegen(mv, bindingContext, frameMap));
        generateReturn(mv, bodyExpression);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateReturn(MethodVisitor mv, JetExpression bodyExpression) {
        if (!endsWithReturn(bodyExpression)) {
            final JetType expressionType = bindingContext.getExpressionType(bodyExpression);
            if (expressionType.equals(JetStandardClasses.getUnitType())) {
                mv.visitInsn(Opcodes.RETURN);
            }
            else {
                mv.visitInsn(Opcodes.ARETURN);
            }
        }
    }

    private static boolean endsWithReturn(JetExpression bodyExpression) {
        if (bodyExpression instanceof JetBlockExpression) {
            final List<JetElement> statements = ((JetBlockExpression) bodyExpression).getStatements();
            return statements.size() > 0 && statements.get(statements.size()-1) instanceof JetReturnExpression;
        }
        return false;
    }

    public void gen(JetFunction f, JetClass owner) {

    }
}
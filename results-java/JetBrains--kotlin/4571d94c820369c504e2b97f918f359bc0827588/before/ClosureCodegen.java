/*
 * @author max
 */
package org.jetbrains.jet.codegen;

import com.intellij.openapi.util.Pair;
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor;
import org.jetbrains.jet.lang.descriptors.FunctionDescriptor;
import org.jetbrains.jet.lang.descriptors.ValueParameterDescriptor;
import org.jetbrains.jet.lang.descriptors.VariableDescriptor;
import org.jetbrains.jet.lang.psi.JetFunctionLiteral;
import org.jetbrains.jet.lang.psi.JetFunctionLiteralExpression;
import org.jetbrains.jet.lang.types.JetType;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.signature.SignatureWriter;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClosureCodegen {
    public final GenerationState state;
    private final ExpressionCodegen exprContext;
    private final ClassContext context;
    private ClassVisitor cv = null;
    public String name = null;

    private Map<DeclarationDescriptor, EnclosedValueDescriptor> closure = new LinkedHashMap<DeclarationDescriptor, EnclosedValueDescriptor>();

    public ClosureCodegen(GenerationState state, ExpressionCodegen exprContext, ClassContext context) {
        this.state = state;
        this.exprContext = exprContext;
        this.context = context;
    }

    public static Method erasedInvokeSignature(FunctionDescriptor fd) {
        boolean isExtensionFunction = fd.getReceiverType() != null;
        int paramCount = fd.getValueParameters().size();
        if (isExtensionFunction) {
            paramCount++;
        }

        Type[] args = new Type[paramCount];
        Arrays.fill(args, JetTypeMapper.TYPE_OBJECT);
        return new Method("invoke", JetTypeMapper.TYPE_OBJECT, args);
    }

    public Method invokeSignature(FunctionDescriptor fd) {
        return state.getTypeMapper().mapSignature("invoke", fd);
    }

    public StackValue lookupInContext(DeclarationDescriptor d) {
        if (d instanceof VariableDescriptor) {
            VariableDescriptor vd = (VariableDescriptor) d;

            EnclosedValueDescriptor answer = closure.get(vd);
            if (answer != null) return answer.getInnerValue();

            final int idx = exprContext.lookupLocal(vd);
            if (idx < 0) return null;

            final Type type = state.getTypeMapper().mapType(vd.getOutType());

            StackValue outerValue = StackValue.local(idx, type);
            final String fieldName = "$" + (closure.size() + 1);
            StackValue innerValue = StackValue.field(type, name, fieldName, false);
            cv.visitField(Opcodes.ACC_PUBLIC, fieldName, type.getDescriptor(), null, null);
            answer = new EnclosedValueDescriptor(d, innerValue, outerValue);
            closure.put(d, answer);

            return innerValue;
        }

        return null;
    }

    public GeneratedAnonymousClassDescriptor gen(JetFunctionLiteralExpression fun) {
        final Pair<String, ClassVisitor> nameAndVisitor = state.forAnonymousSubclass(fun);

        final FunctionDescriptor funDescriptor = (FunctionDescriptor) state.getBindingContext().getDeclarationDescriptor(fun);

        cv = nameAndVisitor.getSecond();
        name = nameAndVisitor.getFirst();

        SignatureWriter signatureWriter = new SignatureWriter();

        final List<ValueParameterDescriptor> parameters = funDescriptor.getValueParameters();
        final String funClass = getInternalClassName(funDescriptor);
        signatureWriter.visitClassType(funClass);
        for (ValueParameterDescriptor parameter : parameters) {
            appendType(signatureWriter, parameter.getOutType(), '=');
        }

        appendType(signatureWriter, funDescriptor.getReturnType(), '=');
        signatureWriter.visitEnd();

        cv.visit(Opcodes.V1_6,
                Opcodes.ACC_PUBLIC,
                name,
                null,
                funClass,
                new String[0]
        );
        cv.visitSource(fun.getContainingFile().getName(), null);


        generateBridge(name, funDescriptor, cv);
        boolean captureThis = generateBody(funDescriptor, cv, fun.getFunctionLiteral());
        final Type enclosingType = context.enclosingClassType(state.getTypeMapper());
        if (enclosingType == null) captureThis = false;

        final Method constructor = generateConstructor(funClass, captureThis);

        if (captureThis) {
            cv.visitField(Opcodes.ACC_PRIVATE, "this$0", enclosingType.getDescriptor(), null, null);
        }

        cv.visitEnd();

        final GeneratedAnonymousClassDescriptor answer = new GeneratedAnonymousClassDescriptor(name, constructor, captureThis);
        for (DeclarationDescriptor descriptor : closure.keySet()) {
            final EnclosedValueDescriptor valueDescriptor = closure.get(descriptor);
            answer.addArg(valueDescriptor.getOuterValue());
        }
        return answer;
    }

    private boolean generateBody(FunctionDescriptor funDescriptor, ClassVisitor cv, JetFunctionLiteral body) {
        final ClassContext closureContext = context.intoClosure(name, this);
        FunctionCodegen fc = new FunctionCodegen(closureContext, cv, state);
        fc.generateMethod(body, invokeSignature(funDescriptor), funDescriptor);
        return closureContext.isThisWasUsed();
    }

    private void generateBridge(String className, FunctionDescriptor funDescriptor, ClassVisitor cv) {
        final Method bridge = erasedInvokeSignature(funDescriptor);
        final Method delegate = invokeSignature(funDescriptor);

        final MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "invoke", bridge.getDescriptor(), state.getTypeMapper().genericSignature(funDescriptor), new String[0]);
        mv.visitCode();

        InstructionAdapter iv = new InstructionAdapter(mv);

        iv.load(0, Type.getObjectType(className));

        final JetType receiverType = funDescriptor.getReceiverType();
        int count = 1;
        if (receiverType != null) {
            StackValue.local(count, JetTypeMapper.TYPE_OBJECT).put(JetTypeMapper.TYPE_OBJECT, iv);
            StackValue.onStack(JetTypeMapper.TYPE_OBJECT).upcast(state.getTypeMapper().mapType(receiverType), iv);
            count++;
        }

        final List<ValueParameterDescriptor> params = funDescriptor.getValueParameters();
        for (ValueParameterDescriptor param : params) {
            StackValue.local(count, JetTypeMapper.TYPE_OBJECT).put(JetTypeMapper.TYPE_OBJECT, iv);
            StackValue.onStack(JetTypeMapper.TYPE_OBJECT).upcast(state.getTypeMapper().mapType(param.getOutType()), iv);
            count++;
        }

        iv.invokespecial(className, "invoke", delegate.getDescriptor());
        StackValue.onStack(delegate.getReturnType()).put(JetTypeMapper.TYPE_OBJECT, iv);

        iv.areturn(JetTypeMapper.TYPE_OBJECT);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private Method generateConstructor(String funClass, boolean captureThis) {
        int argCount = closure.size();

        if (captureThis) {
            argCount++;
        }

        Type[] argTypes = new Type[argCount];


        int i = 0;
        if (captureThis) {
            i = 1;
            argTypes[0] = context.enclosingClassType(state.getTypeMapper());
        }

        for (DeclarationDescriptor descriptor : closure.keySet()) {
            argTypes[i++] = state.getTypeMapper().mapType(((VariableDescriptor) descriptor).getOutType());
        }

        final Method constructor = new Method("<init>", Type.VOID_TYPE, argTypes);
        final MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", constructor.getDescriptor(), null, new String[0]);
        mv.visitCode();
        InstructionAdapter iv = new InstructionAdapter(mv);

        iv.load(0, Type.getObjectType(funClass));
        iv.invokespecial(funClass, "<init>", "()V");

        i = 1;
        for (Type type : argTypes) {
            StackValue.local(0, JetTypeMapper.TYPE_OBJECT).put(JetTypeMapper.TYPE_OBJECT, iv);
            StackValue.local(i, type).put(type, iv);
            final String fieldName;
            if (captureThis && i == 1) {
                fieldName = "this$0";
                captureThis = false;
            }
            else {
                fieldName = "$" + (i);
                i++;
            }

            StackValue.field(type, name, fieldName, false).store(iv);
        }

        iv.visitInsn(Opcodes.RETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
        return constructor;
    }

    public static String getInternalClassName(FunctionDescriptor descriptor) {
        final int paramCount = descriptor.getValueParameters().size();
        if (descriptor.getReceiverType() != null) {
            return "jet/ExtensionFunction" + paramCount;
        }
        else {
            return "jet/Function" + paramCount;
        }
    }

    private void appendType(SignatureWriter signatureWriter, JetType type, char variance) {
        signatureWriter.visitTypeArgument(variance);

        final JetTypeMapper typeMapper = state.getTypeMapper();
        final Type rawRetType = typeMapper.boxType(typeMapper.mapType(type));
        signatureWriter.visitClassType(rawRetType.getInternalName());
        signatureWriter.visitEnd();
    }

    public static CallableMethod asCallableMethod(FunctionDescriptor fd) {
        Method descriptor = erasedInvokeSignature(fd);
        String owner = getInternalClassName(fd);
        final CallableMethod result = new CallableMethod(owner, descriptor, Opcodes.INVOKEVIRTUAL, Arrays.asList(descriptor.getArgumentTypes()));
        if (fd.getReceiverType() != null) {
            result.setNeedsReceiver(null);
        }
        result.requestGenerateCallee(Type.getObjectType(getInternalClassName(fd)));
        return result;
    }
}
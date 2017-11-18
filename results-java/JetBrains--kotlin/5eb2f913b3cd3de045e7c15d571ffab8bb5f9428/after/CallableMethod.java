package org.jetbrains.jet.codegen;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.commons.Method;

import java.util.List;

/**
 * @author yole
 */
public class CallableMethod {
    private String owner;
    private final Method signature;
    private final int invokeOpcode;
    private final List<Type> valueParameterTypes;

    public CallableMethod(String owner, Method signature, int invokeOpcode, List<Type> valueParameterTypes) {
        this.owner = owner;
        this.signature = signature;
        this.invokeOpcode = invokeOpcode;
        this.valueParameterTypes = valueParameterTypes;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Method getSignature() {
        return signature;
    }

    public int getInvokeOpcode() {
        return invokeOpcode;
    }

    public List<Type> getValueParameterTypes() {
        return valueParameterTypes;
    }

    void invoke(InstructionAdapter v) {
        v.visitMethodInsn(getInvokeOpcode(), getOwner(), getSignature().getName(), getSignature().getDescriptor());
    }
}
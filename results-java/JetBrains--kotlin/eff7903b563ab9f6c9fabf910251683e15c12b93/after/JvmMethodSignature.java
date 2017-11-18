package org.jetbrains.jet.codegen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.commons.Method;

import java.util.List;

/**
 * @author Stepan Koltsov
 */
public class JvmMethodSignature {

    private final Method asmMethod;
    /** Null when we don't care about type parameters */
    private final String genericsSignature;
    private final String kotlinTypeParameter;
    private final List<String> kotlinParameterTypes;
    private final String kotlinReturnType;

    public JvmMethodSignature(@NotNull Method asmMethod, @Nullable String genericsSignature,
            @Nullable String kotlinTypeParameters, @Nullable List<String> kotlinParameterTypes, @Nullable String kotlinReturnType) {
        this.asmMethod = asmMethod;
        this.genericsSignature = genericsSignature;
        this.kotlinTypeParameter = kotlinTypeParameters;
        this.kotlinParameterTypes = kotlinParameterTypes;
        this.kotlinReturnType = kotlinReturnType;
    }

    public Method getAsmMethod() {
        return asmMethod;
    }

    public String getGenericsSignature() {
        return genericsSignature;
    }

    public String getKotlinTypeParameter() {
        return kotlinTypeParameter;
    }

    public List<String> getKotlinParameterTypes() {
        return kotlinParameterTypes;
    }

    public String getKotlinReturnType() {
        return kotlinReturnType;
    }
}
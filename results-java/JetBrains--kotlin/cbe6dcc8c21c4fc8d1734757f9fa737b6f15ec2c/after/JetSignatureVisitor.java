package org.jetbrains.jet.rt.signature;

import jet.typeinfo.TypeInfoVariance;

/**
 * @author Stepan Koltsov
 *
 * @see SignatureVisitor
 * @url http://confluence.jetbrains.net/display/JET/Jet+Signatures
 */
public interface JetSignatureVisitor {

    /**
     * Wildcard for an "extends" type argument.
     */
    char EXTENDS = '+';

    /**
     * Wildcard for a "super" type argument.
     */
    char SUPER = '-';

    /**
     * Wildcard for a normal type argument.
     */
    char INSTANCEOF = '=';

    /**
     * Visits a formal type parameter.
     *
     * @param name the name of the formal parameter.
     */
    JetSignatureVisitor visitFormalTypeParameter(String name, TypeInfoVariance variance);

    void visitFormalTypeParameterEnd();

    /**
     * Visits the class bound of the last visited formal type parameter.
     *
     * @return a non null visitor to visit the signature of the class bound.
     */
    JetSignatureVisitor visitClassBound();

    /**
     * Visits an interface bound of the last visited formal type parameter.
     *
     * @return a non null visitor to visit the signature of the interface bound.
     */
    JetSignatureVisitor visitInterfaceBound();

    /**
     * Visits the type of the super class.
     *
     * @return a non null visitor to visit the signature of the super class
     *         type.
     */
    JetSignatureVisitor visitSuperclass();

    /**
     * Visits the type of an interface implemented by the class.
     *
     * @return a non null visitor to visit the signature of the interface type.
     */
    JetSignatureVisitor visitInterface();

    /**
     * Visits the type of a method parameter.
     *
     * @return a non null visitor to visit the signature of the parameter type.
     */
    JetSignatureVisitor visitParameterType();

    /**
     * Visits the return type of the method.
     *
     * @return a non null visitor to visit the signature of the return type.
     */
    JetSignatureVisitor visitReturnType();

    /**
     * Visits the type of a method exception.
     *
     * @return a non null visitor to visit the signature of the exception type.
     */
    JetSignatureVisitor visitExceptionType();

    /**
     * Visits a signature corresponding to a primitive type.
     *
     * @param descriptor the descriptor of the primitive type, or 'V' for
     *        <tt>void</tt>.
     */
    void visitBaseType(char descriptor, boolean nullable);

    /**
     * Visits a signature corresponding to a type variable.
     *
     * @param name the name of the type variable.
     */
    void visitTypeVariable(String name, boolean nullable);

    /**
     * Visits a signature corresponding to an array type.
     *
     * @return a non null visitor to visit the signature of the array element
     *         type.
     */
    JetSignatureVisitor visitArrayType(boolean nullable);

    /**
     * Starts the visit of a signature corresponding to a class or interface
     * type.
     *
     * @param name the internal name of the class or interface.
     */
    void visitClassType(String name, boolean nullable);

    /**
     * Visits an inner class.
     *
     * @param name the local name of the inner class in its enclosing class.
     */
    void visitInnerClassType(String name, boolean nullable);

    /**
     * Visits an unbounded type argument of the last visited class or inner
     * class type.
     */
    void visitTypeArgument();

    /**
     * Visits a type argument of the last visited class or inner class type.
     *
     * @param wildcard '+', '-' or '='.
     * @return a non null visitor to visit the signature of the type argument.
     */
    JetSignatureVisitor visitTypeArgument(char wildcard);

    /**
     * Ends the visit of a signature corresponding to a class or interface type.
     */
    void visitEnd();
}
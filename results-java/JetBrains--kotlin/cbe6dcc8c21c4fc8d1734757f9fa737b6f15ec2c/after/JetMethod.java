package jet.typeinfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for method
 *
 * The fact of receiver presence must be deducted from presence of 'this$receiver' parameter
 *
 * @author alex.tkachman
 *
 * @url http://confluence.jetbrains.net/display/JET/Jet+Signatures
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JetMethod {
    /**
     * @return type projections or empty
     */
    JetTypeProjection[] returnTypeProjections() default {};

    /**
     * Serialized method type parameters.
     * @return
     */
    String typeParameters() default "";

    /**
     * @return is this type returnTypeNullable
     */
    boolean nullableReturnType() default false;

    /**
     * Return type type unless java type is correct Kotlin type.
     */
    String returnType () default "";
}
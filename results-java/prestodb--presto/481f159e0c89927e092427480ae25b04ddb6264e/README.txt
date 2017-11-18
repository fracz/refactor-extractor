commit 481f159e0c89927e092427480ae25b04ddb6264e
Author: Lukasz Osipiuk <lukasz@osipiuk.net>
Date:   Fri Mar 11 17:23:54 2016 +0100

    Handle literal parameters in ReflectionParametricScalar

    Add support for @LiteralParameters annotation for scalars implemented by
    ReflectionParameterScalar class. ReflectionParameterScalar.specialize now
    builds valid Signature by binding values to all variables listed in
    @LiteralParameters annotation.

    This is temporary solution. Following refactoring move the logic to
    Signature/FunctionRegistry classes.
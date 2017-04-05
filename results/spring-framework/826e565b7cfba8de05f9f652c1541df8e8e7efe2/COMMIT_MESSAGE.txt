commit 826e565b7cfba8de05f9f652c1541df8e8e7efe2
Author: Sam Brannen <sam@sambrannen.com>
Date:   Sun Aug 5 19:09:38 2012 +0200

    Polish GenericTypeResolver

     - renamed resolveParameterizedReturnType() to
       resolveReturnTypeForGenericMethod()
     - fleshed out Javadoc for resolveReturnType() and
       resolveReturnTypeForGenericMethod() regarding declaration of formal
       type variables
     - improved wording in log statements and naming of local variables
       within resolveReturnTypeForGenericMethod()

    Issue: SPR-9493
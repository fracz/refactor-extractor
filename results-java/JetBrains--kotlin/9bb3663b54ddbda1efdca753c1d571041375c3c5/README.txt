commit 9bb3663b54ddbda1efdca753c1d571041375c3c5
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Tue Apr 9 20:01:11 2013 +0400

    Extract generation logic from FunctionCodegen

    Create a new class FunctionGenerationStrategy, which is used to specify exactly
    how the body of a function will be generated. This is made primarily to factor
    out the hard dependency on PSI in FunctionCodegen.generateMethod(). The PSI
    element is now optional and is only used for debug information (as an argument
    to newMethod() and endVisit()). This also helps to refactor the confusing logic
    about generating default property accessors.

    Assert for whether we generate code now for the declared member is now useless,
    since generateMethod() will be used also for generating any possible members.

    Also surround with "if ( != null)" some method call in genJetAnnotations(), as
    null is possible here
commit 429a767fb8f2294c2964d864da2bc789e1544681
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Fri Jan 27 00:08:18 2012 +0100

    refactoring that bases all implementations of JavaCompiler on the JavaCompilerSupport class

    - in cases where one compiler delegates to another, we now copy the compiler's configuration with JavaCompiler.configure() instead of delegating calls to methods like setSource and setTargetCompatibility
    - in retrospect we could have kept the JavaSourceCompiler interface, but I don't think it will be really missed
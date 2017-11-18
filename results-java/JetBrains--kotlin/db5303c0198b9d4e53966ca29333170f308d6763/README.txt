commit db5303c0198b9d4e53966ca29333170f308d6763
Author: Pavel V. Talanov <talanov.pavel@gmail.com>
Date:   Tue Jun 10 16:50:35 2014 +0400

    Implement modules in IDE

    IDE:
    Rewrite AnalyzerFacade and implementations for JS and JVM to support creating separate analyzers for each module
    Introduce ModuleInfo which is an intermediate entity between configuration (tests or idea modules) and ModuleDescriptor
    Implement IdeaModuleInfos which represent IDEA modules, sdks and libraries
    Add (somewhat thin) test checking their behaviour
    Implement getModuleInfo() - utility to obtain IdeaModuleInfo for PsiElement
    Drop Project.getLazyResolveSession() - not possible to obtain resolve session for the whole project any more
    Adjust JavaResolveExtension accordingly
    KotlinSignature Intention/Marker - make sure that analyzed element is cls element (he's not in resolve scope otherwise)

    LightClasses:
    Create separate package light classes for each module
    Java code can only reference light class from the first module among it's dependencies
    Duplicate jvm signature is only reported on package declarations inside one module

    Injectors:
    Receive GlobalSearchScope as paramer for VirtualFileFinder and JavaClassFinder
    which allows to narrow analyzer scope

    JDR:
    Introduce ModuleClassResolver resolves java classes in correct java descriptor resolver (corresponding ModuleDescriptor)
    Add test checking that java classes belong to correct module

    Debugger:
    Provide context to analyze files created by debugger in

    Converter:
    Postprocessor now needs a context to analyze resulting code in

    JetPsiFactory:
    Add verification that files created by psi factory are not analyzed without context (that is almost never a good idea)

    Other:
    Use new API in various tests, utilities, run configuration producers and builtin serializers
    Various "TODO: (module refactoring)" which mark the unfinished parts
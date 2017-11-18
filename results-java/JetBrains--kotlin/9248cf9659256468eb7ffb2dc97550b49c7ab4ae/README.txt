commit 9248cf9659256468eb7ffb2dc97550b49c7ab4ae
Author: Stepan Koltsov <stepan.koltsov@jetbrains.com>
Date:   Wed Feb 15 00:02:03 2012 +0400

    Important JavaDescriptorResolver refactoring

    This patch implement own member filling with supertype scope. Before this patch JDR relied on
    Idea hierarchy resolver.

    This patch does two things:

    * copies FunctionDescriptors from supertype scopes
    * rewrites containingDeclaration similary to how it is done in previous patch

    Patch is incomplete, in particular properties are not yet initialized properly, code needs cleanup,
    however the most important part of refactoring is done, and tests pass.
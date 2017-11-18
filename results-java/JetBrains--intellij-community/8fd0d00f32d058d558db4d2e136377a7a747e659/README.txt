commit 8fd0d00f32d058d558db4d2e136377a7a747e659
Author: Semyon Proshev <Semyon.Proshev@jetbrains.com>
Date:   Wed Apr 20 18:51:18 2016 +0300

    PY-8604 Fixed: Rename refactoring for variable in generator comprehension leads to unresolved references
    PY-18808 Fixed: Incorrect variable highlighting

    Update PyReferenceImpl.resolvesToSameLocal to correctly handle comprehensions
commit 5ad14c1cdeb04a7ec4f77bd4d8488f039dbc0a3d
Author: dpb <dpb@google.com>
Date:   Mon Sep 12 12:07:22 2016 -0700

    Refactoring that changes @Multibindings interfaces

    into @Multibinds methods, either moving those methods up into the
    containing module if possible, or else converting the @Multibindings
    interfaces into @Modules and including them in the enclosing @Module.

    RELNOTES: Add a check/refactoring for Dagger 2 that suggests using
    @Multibinds methods instead of @Multibindings interfaces.

    MOE_MIGRATED_REVID=132902072
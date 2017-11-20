commit 73ba2bddad3479c4552dcd6570274df94af83475
Author: glorioso <glorioso@google.com>
Date:   Fri Aug 11 15:21:34 2017 -0700

    Suggest the use of Boxed.hashCode(primitive)

    methods introduced in JDK8 (falling back on the Guava versions if on
    JDK7).

    RELNOTES: improved suggestions to BoxedPrimitiveConstructor

    MOE_MIGRATED_REVID=165032468
commit 2964c529b98ef043e64097fbaf402c8418503c1c
Author: glorioso <glorioso@google.com>
Date:   Fri Sep 23 12:35:45 2016 -0700

    Update ScopeOrQualifierAnnotationRetention

    to not flag instances where the Qualifier/Scope is a nested annotation
    inside a Dagger module/component, and improve the documentation on the
    checker.

    RELNOTES: ScopeOrQualifierAnnotationRetention won't warn on Qualifier/Scope's
    that are inside a dagger component.

    MOE_MIGRATED_REVID=134104765
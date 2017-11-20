commit f62ee724778577e5f2f27496935f325265119b62
Author: glorioso <glorioso@google.com>
Date:   Wed Sep 21 09:56:47 2016 -0700

    Update JavaxInjectOnAbstractMethod

    to detect and raise an error when @javax.inject.Inject is applied to
    default methods on interfaces, and improve the documentation on the
    check.

    RELNOTES: Update JavaxInjectOnAbstractMethod to call out @Inject on
    interface default methods

    MOE_MIGRATED_REVID=133842040
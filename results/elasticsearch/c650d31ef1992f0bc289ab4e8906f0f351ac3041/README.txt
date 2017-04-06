commit c650d31ef1992f0bc289ab4e8906f0f351ac3041
Author: Ryan Ernst <ryan@iernst.net>
Date:   Thu Nov 19 13:06:47 2015 -0800

    Build: Improve integ test startup behavior

    As part of the refactoring to allow --debug-jvm with gradle run, the way
    java options are passed for integ tests was changed. However, we need to
    make sure the jvm argline passed goes to ES_GC_OPTS because this
    allows overriding things like which garbage collector we run, which we
    do for testing from jenkins. This change adds back ES_GC_OPTS.
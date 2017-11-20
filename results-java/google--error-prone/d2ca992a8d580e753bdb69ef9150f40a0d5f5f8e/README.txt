commit d2ca992a8d580e753bdb69ef9150f40a0d5f5f8e
Author: glorioso <glorioso@google.com>
Date:   Wed Oct 5 10:17:19 2016 -0700

    Add a new Check: QualifierOnMethodWithoutProvides

    as well as refactor of some of the InjectMatchers code.

    RELNOTES: New check: QualifierOnMethodWithoutProvides, to detect when
    qualifiers (ex. @Named("something")) are used on method declarations
    outside of Guice/Dagger Module objects.

    MOE_MIGRATED_REVID=135246722
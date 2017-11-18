commit 05042a7de3d3c4914a1ff68f80aeef8ca236298d
Author: ssaviano <ssaviano@google.com>
Date:   Tue Aug 1 20:55:09 2017 -0700

    Automated g4 rollback of changelist 163915119.

    *** Original change description ***

    A few fixes to make --dev_mode=EVERY_PASS work in more cases, and turn it on for most IntegrationTest methods.
     * Run PropagateConstantAnnotationsOverVars after CollapseProperties since it creates new constant names when it collapses constant properties.
     * Make sure convertToDottedProperties runs after markUnnormalized, since it unquotes string key properties, which violates the Normalize constraints.
     * Remove a spurious reportChangeToEnclosingScope call (which may improve performance as well)

    ***

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=163926975
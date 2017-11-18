commit 120be0535c5581bdcdb9e4e733cf566394856ddc
Author: tbreisacher <tbreisacher@google.com>
Date:   Tue Aug 1 17:53:22 2017 -0700

    A few fixes to make --dev_mode=EVERY_PASS work in more cases, and turn it on for most IntegrationTest methods.
     * Run PropagateConstantAnnotationsOverVars after CollapseProperties since it creates new constant names when it collapses constant properties.
     * Make sure convertToDottedProperties runs after markUnnormalized, since it unquotes string key properties, which violates the Normalize constraints.
     * Remove a spurious reportChangeToEnclosingScope call (which may improve performance as well)

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=163915119
commit 5be01d9f20bc2b8f1d1e5d8a1869ab670b0f3635
Author: tbreisacher <tbreisacher@google.com>
Date:   Wed Aug 2 15:30:45 2017 -0700

    A few fixes to make --dev_mode=EVERY_PASS work in more cases, and turn it on for most IntegrationTest methods.
     * Run PropagateConstantAnnotationsOverVars after CollapseProperties since it creates new constant names when it collapses constant properties.
     * Remove a spurious reportChangeToEnclosingScope call (which may improve performance as well)

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=164048127
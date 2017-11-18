commit 28d343cbe932e16769f5a541f281d5e911cbd59d
Author: dimvar <dimvar@google.com>
Date:   Wed May 17 12:19:55 2017 -0700

    [NTI] Remove the failed-to-unify warning.

    As we have gradually improved unification, this warning happens less and less. After the recent improvement to enum unification, the warning should only happen due to argument contravariance when unifying function types. That's a limitation of our algorithm and not something the user can act on.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=156337842
commit a4ed5705aa5656ca0c56f991f6edb5afd4ea1f33
Author: dimvar <dimvar@google.com>
Date:   Fri Jul 14 16:27:35 2017 -0700

    [NTI] Exclude stray properties when checking for implemented abstract or interface methods.

    Sadly, we still include stray properties in all subtyping checks. But possibly the only way to improve on that is to stop supporting stray property definitions altogether. (Unsure if feasible.)

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=162024658
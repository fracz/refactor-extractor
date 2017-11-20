commit 1885c84dd3576c102e3621eec116be106be40f85
Author: kmb <kmb@google.com>
Date:   Tue Jul 12 11:28:24 2016 -0700

    Improve nullness propagation in a few places:

    - instanceof tests guarantee non-null
    - mark accessed arrays as non-null
    - consider this non-null

    Also added a few more tests for existing features (&&, unboxing)

    RELNOTES: improve NullnessAnalysis precision for instanceof and array accesses

    MOE_MIGRATED_REVID=128608782
commit 10d21b174d129e61a56c025e6ee5cc72272a221c
Author: kmb <kmb@google.com>
Date:   Thu Nov 19 16:58:08 2015 -0800

    NullnessAnalysis improvements

    Visit field initializers in nullness analysis if available to improve precision.

    Also:
    - handle array initializers and conditional expressions
    - add some more built-in knowledge of non-null methods and fields

    RELNOTES: improved NullnessAnalysis precision using field initializers and other improvements
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=108300064
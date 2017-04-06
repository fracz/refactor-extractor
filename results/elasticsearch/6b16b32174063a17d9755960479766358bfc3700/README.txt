commit 6b16b32174063a17d9755960479766358bfc3700
Author: Adrien Grand <jpountz@gmail.com>
Date:   Fri Apr 3 11:13:19 2015 +0200

    Aggregations: Fix multi-level breadth-first aggregations.

    The refactoring in #9544 introduced a regression that broke multi-level
    aggregations using breadth-first. This was due to sub-aggregators creating
    deferred collectors before their parent aggregator and then the parent
    aggregator trying to collect sub aggregators directly instead of going through
    the deferred wrapper.

    This commit fixes the issue but we should try to simplify all the pre/post
    collection logic that we have.

    Also `breadth_first` is now automatically ignored if the sub aggregators need
    scores (just like we ignore `execution_mode` when the value does not make sense
    like using ordinals on a script).

    Close #9823
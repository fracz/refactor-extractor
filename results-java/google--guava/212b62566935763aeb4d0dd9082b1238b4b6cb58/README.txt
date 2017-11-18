commit 212b62566935763aeb4d0dd9082b1238b4b6cb58
Author: jasexton <jasexton@google.com>
Date:   Sat Aug 27 19:25:32 2016 -0700

    Add some javadoc stating that immutable graphs are thread safe.

    Slightly refactor code so enterprising users looking at the implementation don't think that's not the case :) And more seriously, this is a better design that makes us less likely to introduce bugs, so it's a good change all around. Thanks Greg!

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=131507603
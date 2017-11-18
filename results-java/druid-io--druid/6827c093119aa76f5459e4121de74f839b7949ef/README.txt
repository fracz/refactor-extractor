commit 6827c093119aa76f5459e4121de74f839b7949ef
Author: Gian Merlino <gianmerlino@gmail.com>
Date:   Tue Sep 6 14:37:55 2016 -0700

    GroupByBenchmark: Fix queryable index generation, improve memory use. (#3431)

    With the old code, all on-disk segments were the same. Now they're different.
    This will end up altering benchmark results for queryMultiQueryableIndex,
    likely making them slower (since values won't group as well as they used to).

    The memory changes will help test with larger/more segments, since we won't
    have to hold them all in memory at once.
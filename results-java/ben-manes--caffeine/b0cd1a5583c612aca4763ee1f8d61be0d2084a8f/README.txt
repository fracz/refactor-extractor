commit b0cd1a5583c612aca4763ee1f8d61be0d2084a8f
Author: Ben Manes <ben.manes@gmail.com>
Date:   Sat Mar 19 15:37:39 2016 -0700

    Do not admit entries that exceed the maximum weight

    If an entry's weight exceeds the cache's maximum capacity, then evict
    it immediately. This avoids flushing the probation segment, though it
    probably does flush the admission window (1% of the total size).

    Thanks to @blemale for reminding me when I peeked at his WeigherSpec
    test [1]. This was fixed in Guava [2], but I had forgotten to port that
    over after v19 was released.

    Also improved the correction of evaluating the admission candidates when
    evicting multiple entries. When a candidate is accepted or evicted the
    count is decremented and the next is set for evaluation. This most often
    occurs for weighted caches.

    [1] https://github.com/blemale/scaffeine
    [2] https://github.com/google/guava/commit/b3855ef79650e282563561368577253d7abcc385
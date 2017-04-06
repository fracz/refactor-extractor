commit 9f6c8c88f34d0f562622eae783d0058abce8af5c
Author: Shay Banon <kimchy@gmail.com>
Date:   Sat Apr 6 00:02:42 2013 +0200

    improve on shard level filter/id cache stats
    use just the removal listener and back to the IndexReader#coreCacheKey as the actual field as part of the cache key
commit d368fd8d24012eee4259bc9c3fc970641075ea03
Author: Ben Manes <ben.manes@gmail.com>
Date:   Thu Oct 29 19:40:51 2015 -0700

    Use Segmented LRU for the main space cache

    This improves the hit rate by 0-2% in many workloads, especially for
    small caches. The 2 segments allows recency to play a slightly higher
    role in determining the victim.
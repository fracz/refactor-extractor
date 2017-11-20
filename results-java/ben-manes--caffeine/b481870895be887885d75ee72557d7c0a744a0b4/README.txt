commit b481870895be887885d75ee72557d7c0a744a0b4
Author: Ben Manes <ben.manes@gmail.com>
Date:   Sat Oct 17 19:13:56 2015 -0700

    Add variation that segments eden and main spaces

    The ideas behind these two policies is that we can use a better
    strategy than LRU for the W-TinyLfu spaces. The best O(1) policy that
    does not use non-resident entries is Segmented Lru, which protects
    entries that have multiple accesses within a short time window.

    If we can present to TinyLfu the best candidates to chose between,
    we can make its admission more effective. It also lets us protect
    ourselves from TinyLfu making a poor decision, which the window
    mostly resolves.

    The results is that segmenting the main space seems to have a 0-2%
    improvement to the hit rate. Further segmenting the eden space
    seems to be a slight penalty rather than improvement.
commit 92f92f7a79a991d148cc88c9e691030dcebba22b
Author: Ben Manes <ben.manes@gmail.com>
Date:   Wed Apr 13 22:11:14 2016 -0700

    Randomized W-TinyLFU policy added to the simulator

    Instead of LRU/SLRU for the cache spaces, this variant uses random
    eviction. The new entry is added to the window. A random selection of
    the candidate (window) and victim (main) are selected, and the one
    with the lowest frequency is removed.

    The hit rates are slightly lower than W-TinyLFU and the window size
    as expected, but very close. It shows the type of improvement over
    the TinyLFU due to giving the candidate a chance to build up its
    frequency.

    This approach would allow us to not require the read buffer or LRU
    lists for the maximum size. That reduces per-entry overhead and
    improves throughput. That comes at the cost of a 1.5% difference
    on some traces. Some more analysis is needed to determine if the
    tradeoff is favorable, but appears promising.
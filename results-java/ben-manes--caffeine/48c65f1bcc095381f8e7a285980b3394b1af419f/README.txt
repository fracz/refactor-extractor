commit 48c65f1bcc095381f8e7a285980b3394b1af419f
Author: Ben Manes <ben.manes@gmail.com>
Date:   Fri Jul 17 18:36:43 2015 -0700

    Added TinyLfu, an admission policy, to simulator

    Eviction policies that improve upon LRU do so by retaining the usage
    frequency in addition to the recency. To be scan resistent and keep a
    longer history, the more advanced policies have "non-resident" entries
    in the cache. These "ghost" entries are the key and the metadata, like
    the reuse distance in LIRS and ClockPro.

    TinyLFU records the frequency in a probabilistic structure. A newly added
    entry will be rejected if its historic frequency is less than that of the
    policy's chosen victim (e.g. least recently used). This serves the same
    purpose as the non-resident entries without keeping them in the policy,
    making it much simpler and can augment any policy.

    The current implementation uses a CountMinSketch. The author's TinyTable
    structure has not been experimented with yet. The memory cost of the data
    structure needs to be evaluated and the parameters optimized. Early testing
    shows that this approach offers a substantial improvement to the hit rate
    regardless of the eviction policy.
commit e1e6e4eca9bedf71c22463c21e7ea8532c1f41d7
Author: Ben Manes <ben.manes@gmail.com>
Date:   Thu Jul 30 23:24:06 2015 -0700

    Victim cache simulation

    A cache split between a primary and secondary layer, each with their own
    eviction policy. The idea is that the primary layer best handles recent
    entries with a high temporal locality. The secondary layer best handles
    long term entries and tracks their frequency. Thus we use a small LRU
    backed by a FIFO+TinyLfu victim cache.

    In general this seems to stabilize the two policies. An LRU degrades
    sharply in looping workloads (full scans), whereas TinyLfu degrades
    with high temporal locality. The combination retains a stable, but
    moderate improvement on LRU without major drops that each suffer,
    but also without the sharp gains that TinyLfu can provide.
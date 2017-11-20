commit 6884b5ee7832a8d39808fdecb8c352b865b306a8
Author: Ben Manes <ben.manes@gmail.com>
Date:   Sat Dec 17 02:25:55 2016 -0800

    Prototype of the sizing the admission window using hill climbing

    We know that recency-skewed workloads favor a large window to mimic LRU
    and that frequency-skewed workloads favor a smaller window to mimic
    LFU. The idea is that we can sample the hit rate, guess an adjustment,
    and see how it fares. This gives us a shallow view of the curve, which
    would let us climb towards the optimal configuration. If the workload
    changes, then the process should restart.

    This prototype is basic and the hill climbing algorithm needs fine
    tuning. Perhaps using simulated annealing. But it provides the
    scaffolding and shows the desired behavior.

    Also cleaned up the simulator code. This included removing the "fast
    path" evaluation, as the idea didn't improve read concurrency. Renamed
    the "adaptive" TinyLfu prototypes to "feedback" so as to not reserve
    that name.
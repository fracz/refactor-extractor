commit 0d7a166e85b59c5701a9ebf2d9d732eb87c7866f
Author: Ben Manes <ben.manes@gmail.com>
Date:   Fri Dec 30 15:28:40 2016 -0800

    Use simulated annealing to optimize window size

    This builds on the simple hill climbing approach. The idea is that the
    admission window size can be optimized by making an adjustment, observing
    the hit rate change, and adjusting again. Ideally this climbs the hill to
    the optimal configuration, up or down based on the feedback. The simple
    approach had a lot of jitter and moved too slowly, but showed promise.

    Simulated annealing uses a cool down rate to slow the adjustment process
    as it nears an optimal value. This also lets us reduce the pivot to take
    smaller steps as we zero in on the target. The process halts, thereby
    ignoring jitter, until the hit rate changes dramatically (new workload)
    or a random restart is triggered.

    The adaption method should be further fine tuned. There may still be too
    much noise, especially due to resetting the hit rates so frequently. We
    should probably use a weighted average across a few hit rate samples to
    avoid noise. But overall we see a nice improvement.
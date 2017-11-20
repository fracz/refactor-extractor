commit 5deaca08cc35d722b2ca4ad7c084ec3f2d508063
Author: Ben Manes <ben.manes@gmail.com>
Date:   Sun May 1 22:39:48 2016 -0700

    An early prototype of an adaptive W-TinyLFU policy

    Experiments have shown that in many workloads TinyLFU is an excellent
    filter. However, in some workloads where recency plays a larger role
    the frequency filter is too strict. We then devised an admission window
    (W-TinyLFU) to capture sparse bursts. In many workloads a 1% window was
    satisfactory, by not degrading frequency workloads and helping recency
    workloads. However, finding the optimal window size is workload
    specific.

    Can we adjust the window size based on the workload? If we detect that
    TinyLFU mispredicted and the new arrival was rejected twice within a
    short period, then we increase the window size. If we see that the this
    is not the case, then we decrease the window size. Thus for workloads
    like OLTP we see an improvement. For other workloads we see now changes
    as no adjustments are made.

    The implementation needs some refinement, but the idea appears to work.
commit 1bbfc86c1f7a302a6a0ef7fff81bb166cd3c384f
Author: Ben Manes <ben.manes@gmail.com>
Date:   Fri May 8 03:39:00 2015 -0700

    Perform cache maintenance asynchronously by default

    The periodic cache maintence, aka cleanup, was being amortorized on the calling
    threads after a read or write. This avoid lock contention and spread the penalty
    of draining the read/write buffers. This was not asynchronous as the design
    predated Java 8 and the JVM wide fork-join common pool.

    This change delegates the maintenance operations to the configured executor,
    thereby reducing the latency incurred by application threads. If a custom
    executor is defined then a reentrant lock must be used (slightly slower than
    the custom non-reentrant lock). This is because a caller-runs policy may be
    used, resulting in a recursive lock and degrading to the previous model.

    This appears to improve overall performance due to the benchmark threads not
    being penalized. This will primarily be observable as reducing latency than
    throughput for applications. The slightly slower read benchmark is okay as
    the old throughput was not repeatable on the previous revision, perhaps due
    to correctness fixes and can be investigated separately.
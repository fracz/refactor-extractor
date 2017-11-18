commit 36e8c5b09ea48b03d79ad4ffd3c0bd8d6f7c02d6
Author: Mark Schaller <mschaller@google.com>
Date:   Mon Nov 2 19:55:49 2015 +0000

    Introduce ForkJoinQuiescingExecutor, permit its use in evaluation

    This CL introduces a QuiescingExecutor implementation specialized for
    ForkJoinPools with the same interrupt handling, error propagation, and
    task completion semantics as AbstractQueueVisitor. Currently it does
    this by largely sharing its implementation with AQV.

    Future refactoring could let it rely more on ForkJoinPool's own
    awaitQuiescence implementation to avoid the overhead of AQV's
    remainingTasks counter maintenance.

    Subtasks spawned by tasks executing in ForkJoinQuiescingExecutor will
    rely on ForkJoinPool's thread-local task deques for low contention
    and (mostly) LIFO ordering.

    --
    MOS_MIGRATED_REVID=106864395
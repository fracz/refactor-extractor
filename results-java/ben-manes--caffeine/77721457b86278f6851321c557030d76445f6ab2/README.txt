commit 77721457b86278f6851321c557030d76445f6ab2
Author: Ben Manes <ben.manes@gmail.com>
Date:   Sat Apr 18 06:06:24 2015 -0700

    Rewrote the stack to support elimination & combining backoff strategies

    Eliminaton cancels out opposite operations (push and pop). Combining
    merges similar operations (pushes) into a batch operation. Both use an
    arena to transfer elements between threads as a backoff strategy when
    the stack's top reference is contended (CAS failed). This greatly
    improves scalability by reducing the number of threads contending on
    a shared reference.

    The approach taken is much simpler than the previous EliminationStack
    or the DECS paper's algorithm. Both of the alternatives rely on multiple
    arena states to model different scenarios. In this version, the arena slot
    is either empty or full. The consumer may receive multiple elements and it
    mimics a producer after taking one of the elements for itself. This greatly
    simplifies the DECS algorithm, resulting in higher performance despite a
    penalty incurred by the consumer when producing.
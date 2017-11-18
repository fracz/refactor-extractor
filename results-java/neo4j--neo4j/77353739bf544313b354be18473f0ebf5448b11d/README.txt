commit 77353739bf544313b354be18473f0ebf5448b11d
Author: lutovich <konstantin.lutovich@neotechnology.com>
Date:   Wed Dec 9 12:22:56 2015 +0100

    Removed KTI pooling

    Currently KernelTransactionImplementations are pooled and reused. This is done
    with MarshlandPool that has ThreadLocals backed by some other pool. So each
    thread that executed transactions has a KTI in it's thread local storage and
    reuses it.

    Such reuse of KTI instance with their internal state has caused quite some
    issues because restoring KTIs to the pristine state before putting them back
    to the pool is tricky.

    This commit removed pooling of KTIs. So whenever new transaction is started
    new KTI object is created.

    Reasons:
     - biggest reason to pool KTIs is transaction state but it is completely
       reinitialized when KTI is taken from the pool
     - recent storage engine API refactoring made KTI state even smaller making
       even less things added to the pool
     - code simplification
     - removed need to reinitialize KTI state

    Tested:
     - microbenchmarks via Core API show 30% degradation for tiny read
       transactions (read couple nodes by id without labels and properties, etc.)
       and no degradation for read/write transactions of reasonable size
     - microbenchmarks via Core API show same heap usage and GC
     - benchmarking read/write Cypher queries show no performance degradation
     - small LDBC read/write queries shows no performance degradation on a
       machine with 32 hardware threads
     - long running soak test shows no throughput degradation, no excessive heap
       usage and GC activity
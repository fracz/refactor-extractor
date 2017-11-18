commit fb6659ac13b4281d568a1dd42a4229235e10d0df
Author: Stefan Oehme <stefan@gradle.com>
Date:   Sat May 20 14:48:34 2017 +0200

    Add a striped ProducerGuard

    The existing ProduerGuard implementation funnells all calls through a single lock
    to decide whether to block or whether to run the factory. It also allocates
    memory for each call (by using a HashSet). It is doing so because dependency
    resolution (currently) needs the guarantee that calls for different keys will
    never block each other. The other consumers of ProducerGuard didn't need this
    guarantee and were paing the price unnecessarily.

    There are now two implementations:

    Dependency resolution uses the existing implementation. This could possibly be
    improved to notify only the threads blocking on a given key instead of notifying
    all waiters. However, this did not show up as a bottleneck during dependency resolution
    yet. If it ever does, using a Key->Lock cache with weak values could work.

    All other clients use a striped guard instead, which spreads the calls over a fixed
    number of locks, giving almost optimal throughput at much lower memory pressure and thread
    contention. This speeds up file system snapshotting, because there the high number of calls
    was bottlenecked on the synchronization of the existing implementation.
commit abacef9a5f658a54facfb78f060cf75402e14d10
Author: Ben Manes <ben.manes@gmail.com>
Date:   Fri Jan 8 00:32:16 2016 -0800

    Remove ConcurrentLinkedStack; schedule SingleConsumerQueue for removal

    These data structures were written as initial seeds for this project
    and do not fit into the theme in the public API. Instead users should
    look to JCTools which is focused specifically on specialized concurrent
    data structures.

    ConcurrentLinkedStack is unused, marked @Beta, and was never matured to
    be promoted into a completed API. The `@Beta` opts it out of semvar
    rules, allowing it to be deleted in a minor release. The stack uses a
    backoff arena to efficiently eliminate and combine operations.

    SingleConsumerQueue was promoted due to being used by the cache as the
    write buffer. It uses a backoff arena to more efficently handle producer
    contention by batching updates to the queue. This was to avoid the
    write buffer from being a write hotspot, though profiling indicates
    the hash table / entry synchronization dominate. JCTools offers a
    MpscLinkedQueue as an alternative to this class. Due to semvar, SCQ
    is scheduled for removal in the next major release (3.0.0). This will
    also allow us to revisit the design, perhaps prefering a growable
    array queue to be more GC friendly.

    Benchmarks were improved based on the excellent advise in #46.
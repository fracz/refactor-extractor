commit d411227715059d07ad73ab1f31d6e0dd96bb1fce
Author: Eric Evans <eevans@apache.org>
Date:   Thu Apr 8 19:27:34 2010 +0000

    RingCache fixups in the wake of CASSANDRA-44

     * invoke DatabaseDescriptor.loadSchemas from RingCache ctor (needed
       now to populate DD's table list).
     * improved error handling in DD.getReplicaPlacementStrategyClass so that
       any future failures to call loadSchemas are easier to spot.
     * updated TestRingCache. This was never runnable as a unit test, (it
       requires a running instance), and doing static initialization is
       problematic now that RingCache's ctor throws IOExceptions.

    Patch by eevans

    git-svn-id: https://svn.apache.org/repos/asf/cassandra/trunk@932073 13f79535-47bb-0310-9956-ffa450edef68
commit 68b416285e11afe49d2b7a3356305d7d59353180
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Wed May 30 20:15:59 2012 -0400

    Refactored locking to reduce ConcurrentHashMap accesses

    HBase and Cassandra OrderedKeyColumnValueStore implementations now are
    supplied with their locking store and local lock mediator at
    construction, and they pass these two things to LockingTransactions as
    needed.  This replaces what was formerly a ConcurrentHashMap get in
    each case.

    Any refactoring on the distributed locking code makes me uneasy.  The
    full test suite passes on my machine, but these changes affect a
    fragile subsystem.

    On an unrelated note, I added an exclusion for the sonatype gossip
    artifact.  This is an slf4j binding which is a dependency of gremlin
    (possibly with some degree of transitivity between gremlin and
    gossip).  It conflicted with the existing log4j binding for slf4j.
    I'm not sure that log4j is really better for Titan than gossip; I just
    kept log4j because it's been in the Titan codebase for so long.
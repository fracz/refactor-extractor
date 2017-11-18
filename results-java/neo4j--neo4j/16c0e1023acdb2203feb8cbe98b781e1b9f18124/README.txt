commit 16c0e1023acdb2203feb8cbe98b781e1b9f18124
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Thu Nov 28 15:29:54 2013 +0100

    Refactors state machine contexts into interfaces

    This commit is a needed refactoring to make state machines more testable.
     It removes all dependencies from all state machine contexts that before
     had them interconnected and all tangled up, requiring multiple levels
     of mocking. Instead, now they are all implemented by MultiPaxosContext
     which takes care to factor out common needed functionality. While the
     diff is large, it does not represent too big of a change.

    Conflicts:
            enterprise/cluster/src/main/java/org/neo4j/cluster/MultiPaxosServerFactory.java
            enterprise/cluster/src/main/java/org/neo4j/cluster/protocol/ConfigurationContext.java
            enterprise/cluster/src/main/java/org/neo4j/cluster/protocol/TimeoutsContext.java
            enterprise/cluster/src/main/java/org/neo4j/cluster/protocol/atomicbroadcast/multipaxos/AcceptorContext.java
            enterprise/cluster/src/main/java/org/neo4j/cluster/protocol/atomicbroadcast/multipaxos/AtomicBroadcastState.java
            enterprise/cluster/src/main/java/org/neo4j/cluster/protocol/atomicbroadcast/multipaxos/MultiPaxosContext.java
            enterprise/cluster/src/main/java/org/neo4j/cluster/protocol/cluster/ClusterContext.java
            enterprise/cluster/src/main/java/org/neo4j/cluster/protocol/cluster/ClusterState.java
            enterprise/cluster/src/main/java/org/neo4j/cluster/protocol/election/ElectionState.java
            enterprise/cluster/src/test/java/org/neo4j/cluster/protocol/election/ElectionStateTest.java
            enterprise/cluster/src/test/java/org/neo4j/cluster/protocol/heartbeat/HeartbeatContextTest.java
            enterprise/cluster/src/test/java/org/neo4j/cluster/protocol/heartbeat/HeartbeatStateTest.java
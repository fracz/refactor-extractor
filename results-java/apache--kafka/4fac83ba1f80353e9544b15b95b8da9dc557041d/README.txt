commit 4fac83ba1f80353e9544b15b95b8da9dc557041d
Author: Colin P. Mccabe <cmccabe@confluent.io>
Date:   Fri Nov 3 09:37:29 2017 +0000

    KAFKA-6060; Add workload generation capabilities to Trogdor

    Previously, Trogdor only handled "Faults."  Now, Trogdor can handle
    "Tasks" which may be either faults, or workloads to execute in the
    background.

    The Agent and Coordinator have been refactored from a
    mutexes-and-condition-variables paradigm into a message passing
    paradigm.  No locks are necessary, because only one thread can access
    the task state or worker state.  This makes them a lot easier to reason
    about.

    The MockTime class can now handle mocking deferred message passing
    (adding a message to an ExecutorService with a delay).  I added a
    MockTimeTest.

    MiniTrogdorCluster now starts up Agent and Coordinator classes in
    paralle in order to minimize junit test time.

    RPC messages now inherit from a common Message.java class.  This class
    handles implementing serialization, equals, hashCode, etc.

    Remove FaultSet, since it is no longer necessary.

    Previously, if CoordinatorClient or AgentClient hit a networking
    problem, they would throw an exception.  They now retry several times
    before giving up.  Additionally, the REST RPCs to the Coordinator and
    Agent have been changed to be idempotent.  If a response is lost, and
    the request is resent, no harm will be done.

    Author: Colin P. Mccabe <cmccabe@confluent.io>

    Reviewers: Rajini Sivaram <rajinisivaram@googlemail.com>, Ismael Juma <ismael@juma.me.uk>

    Closes #4073 from cmccabe/KAFKA-6060
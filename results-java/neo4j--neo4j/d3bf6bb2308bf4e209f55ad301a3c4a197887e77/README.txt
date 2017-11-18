commit d3bf6bb2308bf4e209f55ad301a3c4a197887e77
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Wed Nov 20 11:16:48 2013 +0100

    Prevents local slave lock acquisition waiting after granted on master

    A slave in an HA cluster acquires locks by first acquiring it on its
    master, and if granted go and acquire it locally. Given that the master is
    the authorative instances w/ regards to locks in the cluster it is
    expected that acquiring a lock that the master just granted should not
    have to await any condition - it's expected to be free to acquire. However
    that is apparently not the case since sometimes the slave comes back from
    the master having just acquired it and it goes and blocks, awaiting the
    lock to be free since some other transactions holds it presumably.

    The problem described above in many cases results in such a lock being
    stuck forever on the master, where the only way to release it would be to
    restart that database.

    This commit doesn't fix the issue that is the confusion that the master is
    not the single authorative entity of locks. The fix here is to prevent a
    local lock acquisition, after master acquired such, to go and wait for that
    lock to be free locally. If that would happen then it will not wait, but
    instead throw a DeadlockDetectedException (actually a subclass thereof)
    containing diagnostics about which locks are present in the local lock
    manager. These deadlock detected exceptions will walk and quack the same
    way a "real" such deadlock and a normal retry of the transaction will put
    things right again.

    The root cause will have to be fixed, but having a few more deadlock
    detected exceptions thrown to the client instead of leaving unreleasable
    locks on the master behind is a massive improvement.

    For reference, this issue caused many unexpected BlockingReadTimeoutExceptions
    to be thrown, exceptions of which there should be much less of after this commit.

    (cherry picked from commit 39be5a3a284c8be60cf058482863b7436d09351b)

    Conflicts:
            community/kernel/src/main/java/org/neo4j/kernel/impl/transaction/LockManager.java
            community/kernel/src/main/java/org/neo4j/kernel/impl/transaction/LockManagerImpl.java
            community/kernel/src/main/java/org/neo4j/kernel/impl/transaction/RWLock.java
            enterprise/ha/src/main/java/org/neo4j/kernel/ha/com/master/MasterImpl.java
            enterprise/ha/src/main/java/org/neo4j/kernel/ha/lock/LockManagerModeSwitcher.java
            enterprise/ha/src/main/java/org/neo4j/kernel/ha/lock/SlaveLockManager.java
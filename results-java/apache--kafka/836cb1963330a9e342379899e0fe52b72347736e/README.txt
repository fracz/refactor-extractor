commit 836cb1963330a9e342379899e0fe52b72347736e
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Tue Jan 12 16:16:10 2016 -0800

    KAFKA-3063; LogRecoveryTest causes JVM to exit occasionally

    Remove deletion of tmp file in `OffsetCheckpoint`'s constructor. This delete causes unintuitive behaviour like `LogRecoveryTest` causing a `System.exit` because the test creates an instance of `OffsetCheckpoint` in order to call `read()` on it (while unexpectedly deleting a file being written by another instance of `OffsetCheckpoint`).

    Also:
    * Improve error-handling in `OffsetCheckpoint`
    * Also include minor performance improvements in `read()`
    * Minor clean-ups to `ReplicaManager` and `LogRecoveryTest`

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Ewen Cheslack-Postava <ewen@confluent.io>

    Closes #759 from ijuma/kafka-3063-log-recovery-test-exits-jvm
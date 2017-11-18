commit f01a2859ee522909fbee4f7b79c73954fae108ea
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Fri Apr 11 01:43:17 2014 -0400

    Fix IDAllocationTest UNIQUE_INSTANCE_ID generation

    IDAuthority instances that required distinct UNIQUE_INSTANCE_ID values
    were given identical ones in the IDAllocationTest setup methods,
    leading to technically nondeterministic but extremly likely failures
    in testMultiIDAcquisition.

    This commit adds a pair of new tests: one that enforces the
    UNIQUE_INSTANCE_ID distinctness constraint, and another that
    stresstests a single IDAuthority instance and partition with
    concurrent block requests from different threads.

    This commit also refactors the IDAllocationTest to remove the
    vestigial openStorageManager method parameter.  None of the
    implementations of the method actually use it anymore.

    All tests pass on my machine.
commit f5925c76f06bec3d53219a372374f7d085445e6e
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Tue Jul 9 20:29:26 2013 -0700

    Hacking ConsistentKeyLockStore/Txn

    Fixes and kludges in equal measure around ConsistentKeyLockStore and
    ConsistentKeyLockTransacton.  This commit focuses on the glue code
    between BackendTransaction and the new locking implementation.
    Expected values checking still needs refactoring.

    * Made the new locking code consider only the earliest expected value
      for any give set of calls to acquireLock(key, column, expectedValue,
      tx) where only expectedValue changes and the other arguments are the
      same between calls

    * Fixed ConsistentKeyLockStore using the same backing store for both
      data and locks
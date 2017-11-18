commit 332e208ccfddc19db85a54ec4adce4369ac3ce61
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Sat Sep 1 22:16:51 2012 -0400

    Fixed two expiration timing bugs in locking code

    One of these timing bugs was brain damage left over from my last
    commit: there were attempted conversions in LockingTransactionHandle
    from milli- to nanoseconds using a factor of 1000 instead of 100000.

    The other timing bug was older than my last commit.
    LockingTransactionHandle read nanosecond timestamps from the storage
    backend, but then proceeded to try to figure out whether those
    timestamps had expired by comparing them to milliseconds without
    conversion.

    I've added test cases in LockKeyColumnValueStoreTest that should look
    for these cases.

    LockingTransactionHandle is pretty fragile at this point.  It could
    probably benefit from some refactoring to reduce method size and
    per-method control structure nesting depth.  It might be wise to make
    it uniformly handle only nanosecond timestamps, too.
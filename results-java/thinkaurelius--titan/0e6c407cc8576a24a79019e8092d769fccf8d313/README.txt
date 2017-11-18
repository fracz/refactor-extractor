commit 0e6c407cc8576a24a79019e8092d769fccf8d313
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Sun May 27 06:06:06 2012 -0400

    HBase locking and ID allocation refactoring

    Initial HBase implementation of locking and ID alloction, along
    with refactoring to the Cassandra implementation of each to support
    a shared implementation where reasonable.  CassandraTransaction
    has finally been abstracted out to a key-column-value-store-agnostic
    diskstorage.locking.LockingTransaction.  CassandraTransaction and
    HBaseTransaction extend LockingTransaction but add and override
    nothing; these subtypes only exist to generate class cast exceptions
    in case a user does something nonsensical, like open an HBase
    transaction and then try to acquire locks on that transaction using
    an unrelated Cassandra store.
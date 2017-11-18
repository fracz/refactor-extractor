commit ce586ef156c744d3f0a947de0932ae7a7a2b27ef
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Sat May 26 13:37:55 2012 -0400

    Cassandra refactoring and ID alloc bugfix

    This commit removes the last of CassandraTransaction's dependencies on
    the CassandraThriftStorageManager type.  CassandraTransaction now has
    a reference of type StorageManager where it formerly needed a
    CassandraThriftStorageManager.  Locking column families are now lazily
    created in the backing Cassandra cluster (whereas they were formerly
    created eagerly during openDatabase()).

    This commit also fixes a bug in the ID allocation code for Cassandra.
    Formerly, this code wasn't waiting after writing an ID block claim for
    the time period mandated by the protocol draft.  This could have
    caused contending claimants on an ID block to fail to detect their
    contention.  Now the code waits after writing an ID block when
    necessary.  This bug wasn't exposed under our current test cases and
    is fundamentally tricky to test because it's racy and distributed.

    This commit also adds commented logic for a close() implementation on
    KCVStores that actually does something.  Cassandra doesn't use or
    really need a functioning close() method right now, so its commented,
    but I'm putting it in this commit for potential future reference.
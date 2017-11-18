commit c0582ebe1d0314713028a0bac2a987eff7c43261
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Sat May 26 12:14:25 2012 -0400

    Fix regression in LockKCVStoreTest

    Logic to configure Rid and LocalLockMediator appropriately for faux-remote
    transactions was lost during test case refactoring.  I added an abstract
    method configureTransactions() to LockKCVSTest for the purpose of
    backend-specific transactions tweaks.  This is not an elegant solution and
    may be refactored out of existence later, but it gets the test case back to
    succeeding for now.  The only subclass of LockKCVStoreTest that actually
    works right now is for Cassandra, but this fix should also apply to HBase
    when we test it.
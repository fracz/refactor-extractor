commit ffa1aead361abda4f689e7f69ce9f9e4b60cfc3b
Author: Jacob Hansson <jakewins@gmail.com>
Date:   Fri Sep 27 15:03:33 2013 +0200

    Move KernelTransaction into WriteTransaction.

    This is a major change. Previously, KernelTransaction was 1-1 mapped to TransactionImpl, the
    JTA transaction in Neo4j. However, this was a flawed design, since the Kernel really is closely
    tied to the Neo data source and it's transactions, not the JTA transactions (which are responsible
    for handling two-phase commits across neo and lucene).

    CHANGES INTRODUCED

    With this change, the 1-1 mapping of TransactionImpl is gone, Kernel is now created by the NeoXADataSource,
    and for each WriteTransaction that data source produces, an accompanying KernelTransaction is created.
    Moving the Kernel into NeoXADataSource allowed making all of the Kernels components final, and removing
    all lifecycle hacks in the kernel class.

    The PersistenceManager, rather than the TransactionManager, is now responsible for providing access to the current KernelTransaction, just
    as it is a bridge to the current WriteTransaction. Since we require "real" transactions for every read now,
    the ReadTransaction class has been removed entirely.

    It is no longer possible to perform further operations in a read-only transaction that has been marked for roll-back.
    Previously, one could have nested transactions that failed, but did not hinder the outer transaction to continue
    performing reads. This behavior is no longer allowed, and a few tests have been changed that depended in this behavior.

    A few minor changes were introduced as well. The Server had an implementation of the KernelAPI, which has now been made into it's own thing.
    It was too soon to make the server use the Kernel directly, better to wait until the kernel is actually ready for prime time.

    The TestTxApplicationSynchronization has been rewritten because it no longer worked after the refactoring, and has resisted our attempts
    to debug it. This extremely complex and arcane test was originally introduced to reproduce a race condition; the race condition was fixed
    by ensuring that commands are applied in the same order whether or not we are recovering. We have therefore replaced the broken test with
    a simpler test that specifically enforces that ordering (WriteTransactionCommandOrderingTest).

    The ShutdownXaDataSource has also been removed by letting the XaDataSourceManager.getXaDataSource method throw an IllegalStateException,
    if the manager is shut down, instead of returning an instance of the ShutdownXaDataSource. Anyone who would now try to get an XaDataSource
    from a manager that has been shut down, will now fail earlier than before, and with a more explicit exception and a more local stack trace.
    I think it's a good change, but it is none the less a change in behaviour. However, this is in kernel/impl, so no one besides us should depend
    on it. Also, no tests failed because of this change, as far as I can tell. I have mixed feelings about that fact.

    The TransactionFailureException in kernel was also simplified.

    NEXT STEPS

    The relationship between WriteTransaction and KernelTransaction is something that will need further refactoring.
    Ideally, the responsibilities of WriteTransaction will move to be performed by subcomponents of KernelTransaction,
    and WriteTransaction will go away. The same thing should happen with XAResourceManager - it should not write to
    logical log, but should delegate to KernelTransaction to handle all IO.

    After that is done, the Kernel will be entirely independent of the JTA code base, and we will be able to run Neo4j
    without the JTA layer, with large performance benefits and lowered complexity as a result.

    Speaking of performance, every database operation currently passes through PersistenceManager, which does a
    synchronized getOrCreate-type call to associate the operation with the current transaction. Since operations
    are moving into KernelTransaction, we should be able to slowly remove all methods from PersistenceManager. This
    will remove a very large amount of synchronization that is currently being performed.

    Once all PersistenceManager methods are removed, it should be possible to merge it with ThreadToStatementContextProvider,
    since they will perform the same function.

    RISKS

    Overall, this is a large change that affects sensitive parts of the code base. However, there is increasingly good code
    coverage, with many tests catching our attempts at this refactoring. The end result changes very little in the sensitive
    code paths of XAResource and WriteTransaction, and where it does, changes are mostly to increase ease of comprehension.

    There is a very important thing to note here, which is that now KernelTransaction is committed along with WriteTransaction,
    which means that locks are released as soon as the Neo data source is done with it's work. Previously, locks were held until
    the JTA transaction was committed in entirety. This only affects two-phase commits, but we should consider if there are any
    ill effects of this with regards to how legacy indexes and neo interact. Do note, however, that it is still possible to
    manually grab locks through the Core API, and that those locks will be held until the end of the JTA transaction lifespan.
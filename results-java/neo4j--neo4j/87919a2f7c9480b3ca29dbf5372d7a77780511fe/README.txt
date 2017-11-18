commit 87919a2f7c9480b3ca29dbf5372d7a77780511fe
Author: Tobias Lindaaker <tobias@thobe.org>
Date:   Fri Nov 29 15:58:09 2013 +0100

    Integrate LockingService into IndexingService.

    Use the LockingService to mutually exclude WriteTransaction and index
    populators from the same nodes. This is done by having WriteTransaction
    acquire locks for each node it updates, releasing them when transaction
    application completes, and by having the store scanner that the indexes
    use acquire a lock for each node that it reads, releasing it after it
    has completed reading that node. This ensures that the node does not
    change while the index populator reads it.

    In order to introduce a clear lock region for when the index populator
    reads a node, the NeoStoreIndexStoreView had to be refactored. It now
    does node processing in two distinct steps:
    1. reading - this is done under lock to ensure a consistent view of the
       node.
    2. processing - this is done after the lock has been released, to allow
       the processing to be something complex, while minimizing the locked
       region.
    Where NeoStoreIndexStoreView previously used the RecordStore.Processor
    API, it now does the iteration itself to allow the lock to be lexically
    scoped, making it much easier to reason about. Otherwise some mechanism
    similar to the previous LabelsReference would have had to been used, and
    LabelsReference was too complicated to follow even for its simple task,
    it would have been even more complicated to use that pattern for locks.

    While this change reduced the "reuse value" of RecordStore.Processor, it
    also reduced the total size of NeoStoreIndexStoreView (slightly), which
    should make it a valuable change in of its own.
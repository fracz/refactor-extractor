commit 794e6dbd14f040d21d3402c5eda22cfa8f5c4b3d
Author: Guozhang Wang <wangguoz@gmail.com>
Date:   Fri May 12 15:01:01 2017 -0700

    KAFKA-5130: Refactor transaction coordinator's in-memory cache; plus fixes on transaction metadata synchronization

    1. Collapsed the `ownedPartitions`, `pendingTxnMap` and the `transactionMetadataCache` into a single in-memory structure, which is a two-layered map: first keyed by the transactionTxnLog, and then valued with the current coordinatorEpoch of that map plus another map keyed by the transactional id.

    2. Use `transactionalId` across the modules in transactional coordinator, attach this id with the transactional marker entries.

    3. Use two keys: `transactionalId` and `txnLogPartitionId` in the writeMarkerPurgatory as well as passing it along with the TxnMarkerEntry, so that `TransactionMarkerRequestCompletionHandler` can use it to access the two-layered map upon getting responses.

    4. Use one queue per `broker-id` and `txnLogPartitionId`. Also when there is a possible update on the end point associated with the `broker-id`, update the Node without clearing the queue but relying on the requests to retry in the next round.

    5. Centralize the error handling callback for appending-metadata-to-log and sending-markers-to-brokers in `TransactionStateManager#appendTransactionToLog`, and `TransactionMarkerChannelManager#addTxnMarkersToSend`.

    6. Always update the in-memory transaction metadata AFTER the txn log has been appended and replicated, and then double check on the cache to make sure nothing has changed since log appending. The only exception is when initializing the pid for the first time, in which we will put a dummy into the cache but set its pendingState as `Empty` (so it will be valid to transit from `Empty` to `Empty`) so that it can be updated after the log append has completed.

    Author: Guozhang Wang <wangguoz@gmail.com>

    Reviewers: Ismael Juma, Damian Guy, Jason Gustafson, Jun Rao

    Closes #2964 from guozhangwang/K5130-refactor-tc-inmemory-cache
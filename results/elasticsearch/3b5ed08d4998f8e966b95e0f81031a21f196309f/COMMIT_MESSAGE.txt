commit 3b5ed08d4998f8e966b95e0f81031a21f196309f
Author: Simon Willnauer <simonw@apache.org>
Date:   Wed Sep 23 23:07:17 2015 +0200

    Refactor StoreRecoveryService to be a simple package private util class

    StoreRecoveryService used to be a pretty heavy class with lots of dependencies.
    This class was basically not testable in isolation and had an async API with a listener.
    This commit refactors this class to be a simple utility classs with a sync API hidden behind
    the IndexShard interface. It includes single node tests and moves all the async properities to
    the caller side.
    Note, this change also removes the mapping update on master from the store recovery code since
    it's not needed anymore in 3.0 because all stores have been subject to sync mapping updates such
    that the master already has all the mappings for documents that made it into the transaction log.

    Closes #13766
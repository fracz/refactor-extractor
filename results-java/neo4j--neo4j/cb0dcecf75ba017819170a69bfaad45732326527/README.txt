commit cb0dcecf75ba017819170a69bfaad45732326527
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Thu Feb 13 11:34:32 2014 +0100

    Batch inserter uses the same record logic code as NeoStoreTransaction

    recent refactorings in and around NeoStoreTransaction has made much of the
    duplicated code in batch inserter redundant. This commit makes the
    batch inserter, to some extent, use the same code that NeoStoreTransaction
    uses for creating, loading, deleted and connecting records in the store.
    Eventually no record logic code will exist in neither batch inserter
    not NeoStoreTransaction.

    As a side effect this adds support for the dense node format in batch
    inserter.

    DirectRecordAccess makes use of Proxy objects quite a lot, so they are
    pooled in a FlyweightPool so they are reused. Also, the batch set that
    keeps changes to be committed is implemented as a sorted map so we can
    write the records in inverse id order, this way ensuring that the file
    is expanded only once for this batch.

    co-author: Chris Gioran, @digitalstain
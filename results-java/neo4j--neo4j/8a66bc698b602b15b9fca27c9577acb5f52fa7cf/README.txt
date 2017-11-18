commit 8a66bc698b602b15b9fca27c9577acb5f52fa7cf
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Mon Aug 11 11:25:42 2014 +0200

    TransactionRepresentation has separate timestamps for start and commit

    Recent refactorings ended up using the same timestamp for when a
    transaction was started to that of when the transaction was committed.
    This commit captures a separate timestamp for when the transaction was
    started, i.e. when the user started it.

    Also lastCommittedTransactionWhenStarted now correctly reports he
    transaction id at the point in time when the transaction was started, not
    when committed. The reason for that is that the latter is unnecessary
    since it's always going to be the previous transaction id, the way
    things work now.
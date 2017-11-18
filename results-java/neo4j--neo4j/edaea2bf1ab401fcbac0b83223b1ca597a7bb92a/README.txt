commit edaea2bf1ab401fcbac0b83223b1ca597a7bb92a
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Fri Jul 5 11:52:08 2013 +0200

    A couple of performance improvements

    o Created and uses PrimitiveLongIterator for getPropertyKeys(). Should be
      used for more as well.
    o Use individual statement operation parts to remove the tiny
      StatementOperations delegation at the top.
    o TxState#hasChanges() is cheaper
    o StateHandlingStatementOperations#getPropertyKeys and getProperty
      just delegates if there's no tx changes.
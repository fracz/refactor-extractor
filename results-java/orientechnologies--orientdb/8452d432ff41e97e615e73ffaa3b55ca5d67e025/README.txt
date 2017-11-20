commit 8452d432ff41e97e615e73ffaa3b55ca5d67e025
Author: l.garulli@gmail.com <l.garulli@gmail.com@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Mon Jan 30 17:59:53 2012 +0000

    Huge refactoring of SQL Select:
    - changed ORecord to OIdentifiable in all the operators to work with any targets (now as Iterable<? extends OIdentifiable>
    - created new OCommandExecutorSQLExtractAbstract class as base class for SELECT and TRAVERSE since they have a lot in common
    - moved OIndexSearchResult in a separate class because Select was enough large
    - removed OStorageEmbedded.browse() methods
    - improved iterators to reuse fetched records
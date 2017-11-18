commit cf7b14f00fb9cf790a8866cc165cd8e7cc1de4e6
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Sat May 19 08:16:46 2012 -0400

    Cassandra locking unit test (initial commit)

    The changes in src/main are refactoring to support testing.
    I added some public methods there which expose internals; this
    would be better executed by repackaging the unit test to match
    CassandraTransaction and dropping the public visibility modifier
    on the added methods
commit 58aa66ad831170283936add04037361d8a45050a
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu Jul 5 01:17:18 2012 -0400

    Astyanax connection pool config refactoring

    The size of the connection pool used for DDL operations ("cluster"
    connections in Astyanax parlance) is now configurable separately from
    the size of the connection pool used for ordinary data operations on
    the AstyanaxStorageManager's keyspace.  The default size of this DDL
    pool is 3.
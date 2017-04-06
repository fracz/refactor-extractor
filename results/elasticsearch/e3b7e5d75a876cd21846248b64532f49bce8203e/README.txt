commit e3b7e5d75a876cd21846248b64532f49bce8203e
Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
Date:   Tue Mar 15 15:07:53 2016 +0100

    percolator: Replace percolate api with the new percolator query

    Also replaced the PercolatorQueryRegistry with the new PercolatorQueryCache.

    The PercolatorFieldMapper stores the rewritten form of each percolator query's xcontext
    in a binary doc values field. This make sure that the query rewrite happens only during
    indexing (some queries for example fetch shapes, terms in remote indices) and
    the speed up the loading of the queries in the percolator query cache.

    Because the percolator now works inside the search infrastructure a number of features
    (sorting fields, pagination, fetch features) are available out of the box.

    The following feature requests are automatically implemented via this refactoring:

    Closes #10741
    Closes #7297
    Closes #13176
    Closes #13978
    Closes #11264
    Closes #10741
    Closes #4317
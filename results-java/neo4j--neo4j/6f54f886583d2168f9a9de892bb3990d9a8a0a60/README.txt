commit 6f54f886583d2168f9a9de892bb3990d9a8a0a60
Author: Tobias Lindaaker <tobias@thobe.org>
Date:   Fri Aug 29 16:07:23 2014 +0200

    Improve performance of index lookup with tx state.

    Performing an index lookup in a transaction with state was implemented
    as a linear scan over the transaction state. Even if the lookup from the
    underlying store was fast, the performance would degrade linearly to the
    size of the active transaction.
    This change set improves the performance of index lookups in a
    transaction in a transaction with state by building up an index-like
    structure in the transaction state as well. This index state is built
    for the indexes defined by the schema.
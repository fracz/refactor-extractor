commit 37687b1f1872ef01ed10f1b677eae73d8184bc22
Author: Adrian Cole <adriancole@users.noreply.github.com>
Date:   Sun Jul 10 09:21:12 2016 +0800

    Over-fetches cassandra trace indexes to improve UX and fixes Cassandra index (#1177)

    * Over-fetches cassandra trace indexes to improve UX

    Even when optimized, cassandra indexes will have more rows than distinct
    (trace_id, timestamp) needed to satisfy query requests. This side-effect
    in most cases is that users get less than `QueryRequest.limit` results
    back. Lacking the ability to do any deduplication server-side, the only
    opportunity left is to address this client-side.

    This over-fetches by a multiplier `CASSANDRA_INDEX_FETCH_MULTIPLIER`,
    which defaults to 3. For example, if a user requests 10 traces, 30 rows
    are requested from indexes, but only 10 distinct trace ids are queried
    for span data.

    To disable this feature, set `CASSANDRA_INDEX_FETCH_MULTIPLIER=1`

    Fixes #1142

    * Fixes Cassandra indexes that lost traces in the same millisecond (#1153)

    A schema bug resulted in Cassandra not indexing more than bucket count
    (10) trace ids per millisecond+search input. This manifested as less
    traces retrieved by UI search or Api query than expected. For example,
    if you had 1000 traces that happened on the same service in the same
    millisecond, only 10 would return.

    The indexes affected are `service_span_name_index`, `service_name_index`
    and `annotations_index` and this was a schema-only change. Those with
    existing zipkin installations should recreate these indexes to solve the
    problem.

    Fixes #1142
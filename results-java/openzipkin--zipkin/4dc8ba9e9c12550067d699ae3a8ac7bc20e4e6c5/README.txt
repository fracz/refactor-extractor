commit 4dc8ba9e9c12550067d699ae3a8ac7bc20e4e6c5
Author: Adrian Cole <adriancole@users.noreply.github.com>
Date:   Sat Jul 9 14:20:10 2016 +0800

    Optimizes out cassandra index inserts that don't improve query results (#1172)

    For example, a trace that fans out into N spans against the same service
    will end up with a fixed amount of cassandra inserts as opposed to a
    function of N.

    This removes extra requests when a collector receives a large amount of
    span data for a single trace. That could be the case when one or more of
    the following are true:

    * instrumentation bundles all local spans in a trace into one message
    * traces are routed consistently to a single storage component
    * there's only one collector

    The overhead of this feature with defaults should be tens of megabytes
    heap. Disable by setting `CassandraStorage.Builder.indexCacheMax(0)`
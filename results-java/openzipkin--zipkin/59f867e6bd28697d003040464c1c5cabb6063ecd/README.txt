commit 59f867e6bd28697d003040464c1c5cabb6063ecd
Author: mck <mick@semb.wever.org>
Date:   Thu Jul 28 22:17:39 2016 +1000

    A new datamodel using Cassandra-3.9 features.

    Data transparency is introduced. All spans, annotations, and endpoints, are easily readable data via any Cassandra CQL
    interface, eg cqlsh. No data is inserted any longer in thrift serialized format.

    Indexing in CQL is simplified by using both MATERIALIZED VIEWs and SASI.
    This has reduced the number of tables from 7 down to 2. This also reduces the write-amplification in CassandraSpanConsumer.
    This write amplification now happens internally to C*, and is visible in the increase write latency (although write latency
    remains performant at single digit milliseconds).
    The 'traces' table keeps the same name. A SASI index on its 'all_annotations' column permits full-text searches against
    annotations and therefore replaces the 'annotations_index' table.
    The 'service_span_name_index' table has been renamed to trace_by_service_span. It now includes both 'trace_id' and 'duration'
    non-primary key columns. A SASI index on the 'duration' column replaces the need for the 'span_duration_index' table.
    A materialized view of the 'trace_by_service_span' table exists as 'trace_by_service' replacing the
    tables 'service_names', 'span_names', and 'service_name_index'.

    Time-To-Live is default now at the table level. It can not be overridden in write requests.

    Time-series data is compacted using TimeWindowCompactionStrategy, a known improved over DateTieredCompactionStrategy. Data is
    optimised for queries with a single day. The penalty of reading multiple days is small, a few disk seeks, compared to the
    otherwise overhead of reading a significantly larger amount of data.

    Benchmarking the new datamodel demonstrates a significant performance improvement on reads. How much of this translates to te
    Zipkin UI is hard to tell due to the compexity of CassandraSpanConsumer and how searches are possible. Benchmarking stress
    profiles are found in traces-stress.yaml and trace_by_service_span-stress.yaml.

    Potential problems:
     -- spanStore.getServiceNames() and spanStore.getSpanNames(serviceName) could be slow in a production system because the
    materialized view 'trace_by_service' is only partitioned by the 'service_name' column. Either a row cache should be applied
    (given that row caches are more tunable now) and something like DeduplicatingExecutor re-introduced.
     -- searches against many service_names and many days will fan out to many cql requests when reading from the
    'trace_by_service_span' table.
     -- The two SASI b-tree indexes take up disk space. The new datamodel is a trade-off from disk-space for performance,
    simplicity, and full-text searching.
     -- when searching with annotations the reverse chronological order of spans/traces may not be honoured.
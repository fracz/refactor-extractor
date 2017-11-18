commit 65accfb0e66335b73699de0e077b2caf4c9e25f3
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Sun May 27 12:40:00 2012 -0400

    Hack to support Cassandra's static dropKeyspace

    Pooled Cassandra OKCVS instances are not being correctly cleared
    from CassandraThriftStorageManager's cache when the static
    dropKeyspace is called.  The three graphdb tests for Cassandra
    use this static method.  The pooling/caching really just needs to be
    refactored to make this unnecessary; this is a stopgap.
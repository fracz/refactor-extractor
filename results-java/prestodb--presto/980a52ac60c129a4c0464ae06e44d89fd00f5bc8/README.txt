commit 980a52ac60c129a4c0464ae06e44d89fd00f5bc8
Author: Andrii Rosa <Andriy.Rosa@TERADATA.COM>
Date:   Wed Mar 15 14:29:27 2017 -0400

    Do not cache partition keys

    This cache was intended to cache binary partition keys for all the partitions ever queried.
    If there are to many of them - asynchronous reload every 2 minutes overloads Presto and Cassandra.

    The first thought was to limit the cache size and increase the reload timeout. But actually there are
    not that many pros of having that cache.

    There are basically 2 workloads that are possible:

    - Paritions are small
    - Partitions are huge

    If the partitions are small - there is a high change that there will be lot of them. So, the cache will be
    overloaded.

    If the partitions are huge - there is only little performance improvement of using this cache. Because
    retrieving keys for partitions directly from Cassandra costs nothing by comparing to the time needed to
    retrieve all the partition rows.

    Also, fwiw,  there is a built-in cache key in Cassandra. And Cassandra itself has been built with "no extra caching needed" in mind.
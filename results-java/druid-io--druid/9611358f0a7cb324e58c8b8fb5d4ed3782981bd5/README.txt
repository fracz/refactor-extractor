commit 9611358f0a7cb324e58c8b8fb5d4ed3782981bd5
Author: Roman Leventov <leventov@users.noreply.github.com>
Date:   Mon Oct 17 20:36:19 2016 +0300

    Small topn scan improvements (#3526)

    * Remove unused numProcessed param from PooledTopNAlgorithm.aggregateDimValue()

    * Replace AtomicInteger with simple int in PooledTopNAlgorithm.scanAndAggregate() and aggregateDimValue()

    * Remove unused import
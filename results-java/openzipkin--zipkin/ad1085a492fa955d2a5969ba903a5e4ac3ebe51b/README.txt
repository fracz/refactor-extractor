commit ad1085a492fa955d2a5969ba903a5e4ac3ebe51b
Author: Adrian Cole <adriancole@users.noreply.github.com>
Date:   Sat Sep 10 08:39:43 2016 +0800

    Removes duration query support from Cassandra 2.2 (#1283)

    The duration query is unusable for most people, yet adds load every time
    a span is written. It is also complicated code that hasn't improved much
    since being written.

    This removes this feature, punting to cassandra3 which can address the
    problem more effectively via SASI indexing.

    Fixes #1224
    Obviates #1204
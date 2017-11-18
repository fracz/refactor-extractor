commit 10ea935691699ae19f7c1a8bfa47a1b3e17312ab
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Fri Apr 11 21:50:11 2014 -0400

    Use dynamically loaded HBase compatibility layer

    titan-hbase-core now dynamically instantiates one of two
    implementations of the HBaseCompat interface depending on what version
    string HBase's VersionInfo class reports.  There's one implementation
    for 0.94 and one implementation for 0.96.  This approach is directly
    inspired by Apache Hive's ShimLoader, which does something similar to
    mask minor incompatibilities between different Hadoop versions.

    The next critical step in this refactoring would be to write an
    assembly that includes all of titan-hbase-094, -096, and -core in one
    jar.

    The titan-hbase-096 and -094 modules both appear to pass their tests
    on my machine.
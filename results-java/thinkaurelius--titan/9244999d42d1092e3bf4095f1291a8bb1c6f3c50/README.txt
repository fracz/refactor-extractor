commit 9244999d42d1092e3bf4095f1291a8bb1c6f3c50
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Wed May 21 04:53:31 2014 -0400

    HBase/Cassandra read op timestamp tweaks

    This commit mostly affects read timestamps used by the Cassandra
    embedded adapter, which could previously break reads on columns with
    TTLs set when Titan was configured for a timestamp resolution other
    than milliseconds.

    All of the HBase and Cassandra backends should continue to retrieve
    the most recent version of rows/columns, regardless of
    TransactionHandleConfig.getStartTime().  The notion of start time is
    generally not practical when using native HBase and Cassandra
    timestamps, since Cassandra only shows the most recent column version
    and HBase retains only between 1 and 3 most recent versions under its
    default configuration (and may well be configured to retain only 1).

    There's also some minor refactoring in DistributedStoreManager; I
    renamed "Timestamp" to "MaskedTimestamp" in an attempt to highlight
    this class's sole function, which is to encapsulate some bitmask
    operations on otherwise ordinary timestamps.

    There is more timestamp refactoring to come after this commit; this
    just focuses on some delicate changes in the Cassandra embedded
    adapter and refactoring in DistributedStoreManager.(Masked)Timestamp
    and its usage sites.
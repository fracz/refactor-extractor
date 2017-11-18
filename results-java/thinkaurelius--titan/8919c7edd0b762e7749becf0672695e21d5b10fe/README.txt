commit 8919c7edd0b762e7749becf0672695e21d5b10fe
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Mon Feb 3 18:52:45 2014 -0500

    Timestamp-related refactoring in core

    * Combined NanoTime and MicroTime classes into a single enum called
      Timestamps.  The TimestampProvider interface is unchanged.

    * Added a TIMESTAMP_PROVIDER ConfigOption to
      GraphDatabaseConfiguration and made StandardTransactionBuilder apply
      its value.  The default is micros.

    * Made ConsistentKeyIDManager use millisecond timestamps in its mutate
      calls.  Before this commit, it seemed to write columns containing
      millisecond time but issue those writes to Cassandra/HBase using
      nanotime at the protocol level.  Now it should be milliseconds
      throughout.

    * Made ConsistentKeyIDManager instantiate a new storage transaction
      configuration for each mutate call.

    * Cleaned up a couple cases of commented code held over from previous
      refactoring commits.
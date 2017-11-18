commit badf4cbfca13f631594352a0f85cc77dc59d6ceb
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Wed Jan 22 15:02:17 2014 -0500

    Fix HBase mutateMany

    EntryList/Buffer refactoring caused a map used by HBase's mutateMany
    to call get with objects never equal to those used in calls to put.
    The bug manifested like this:

    mutateManyWritesSameKeyOnMultipleCFs(com.thinkaurelius.titan.diskstorage.hbase.HBaseMultiWriteKeyColumnValueStoreTest)  Time elapsed: 19.315 sec  <<< FAILURE!
    java.lang.AssertionError: expected:<[  0-  0-  0-  0-  0-  0-  0- 42-> 0-  0-  0-  0-  0-  1- 33-104]> but was:<[]>
            at org.junit.Assert.fail(Assert.java:88)
            at org.junit.Assert.failNotEquals(Assert.java:743)
            at org.junit.Assert.assertEquals(Assert.java:118)
            at org.junit.Assert.assertEquals(Assert.java:144)
            at com.thinkaurelius.titan.diskstorage.MultiWriteKeyColumnValueStoreTest.mutateManyWritesSameKeyOnMultipleCFs(MultiWriteKeyColumnValueStoreTest.java:167)
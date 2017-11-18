commit 97559c0614d900a682d01afc241615cf5627fb4c
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Mon Mar 9 21:13:09 2015 -0700

    Engine: update index buffer size during recovery and allow configuring version map size.

    To support real time gets, the engine keeps an in-memory map of recently index docs and their location in the translog. This is needed until documents become visible in Lucene. With 1.3.0, we have improved this map and made tightly integrated with refresh cycles in Lucene in order to keep the memory signature to a bare minimum. On top of that, if the version map grows above a 25% of the index buffer size, we proactively refresh in order to be able to trim the version map back to 0 (see #6363) . In the same release, we have fixed an issue where an update to the indexing buffer could result in an unwanted exception during recovery (#6667) . We solved this by waiting with updating the size of the index buffer until the shard was fully recovered. Sadly this two together can have a negative impact on the speed of translog recovery.

    During the second phase of recovery we replay all operations that happened on the shard during the first phase of copying files. In parallel we start indexing new documents into the new created shard. At some point (phase 3 in the recovery), the translog replay starts to send operation which have already been indexed into the shard. The version map is crucial in being able to quickly detect this and skip the replayed operations, without hitting lucene. Sadly #6667 (only updating the index memory buffer once shard is started) means that a shard is using the default 64MB for it's index buffer, and thus only 16MB (25%) for the version map. This much less then the default index buffer size 10% of machine memory (shared between shards).

    Since we don't flush anymore when updating the memory buffer, we can remove #6667 and update recovering shards as well. Also, we make the version map max size configurable, with the same default of 25% of the current index buffer.

    Closes #10046
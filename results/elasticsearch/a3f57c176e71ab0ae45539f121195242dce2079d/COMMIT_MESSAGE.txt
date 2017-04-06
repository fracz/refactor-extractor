commit a3f57c176e71ab0ae45539f121195242dce2079d
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Thu Mar 20 10:11:56 2014 +0100

    [Test] IndicesLifecycleListenerTests - added busy waiting for expected results (and improved error message)

    The test currently uses ensureGreen to guaranty all shard allocations has happened. This only guaranties it from the cluster perspective and in some cases the nodes are not fast enough to implement the changes (which is what the test is about).
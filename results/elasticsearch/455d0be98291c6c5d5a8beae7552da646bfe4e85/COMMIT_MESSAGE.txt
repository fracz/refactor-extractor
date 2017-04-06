commit 455d0be98291c6c5d5a8beae7552da646bfe4e85
Author: Shay Banon <kimchy@gmail.com>
Date:   Fri Sep 13 17:34:41 2013 +0200

    improve visibility of clusterState and shardsIt
    In case of retries, we update the clusterState and shardsIt, make sure they are visible using volatile (even though updates will probably go through a memory barrier, this might explain rare failure we see when retry happens)
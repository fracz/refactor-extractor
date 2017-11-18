commit cc3fab45ffcc6d8208a35bcdc1bb9d8f7f7da7d8
Author: Shay Banon <kimchy@gmail.com>
Date:   Fri Jun 22 03:36:54 2012 +0200

    Improve cluster resiliency to disconnected sub clusters + fix a shard allocation bug with quick rolling restarts
    Two main changes:

    Improve cluster resiliency to disconnected sub clusters. If a node pings a master and that node is no longer registered with the master, improve the rejoin process of that node to the cluster. Also, if a master receives a message from another master, pick one to force to rejoin the cluster (based on cluster state versioning).
    On quick rolling restart, without waiting for shard allocation, the shard allocation logic can mess up its counts, causing for strange logic in allocating shards, or validation failures on routing table allocation.
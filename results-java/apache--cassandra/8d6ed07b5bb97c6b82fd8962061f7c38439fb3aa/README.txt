commit 8d6ed07b5bb97c6b82fd8962061f7c38439fb3aa
Author: Jason Brown <jasedbrown@gmail.com>
Date:   Fri Apr 12 09:36:19 2013 -0700

    CASSANDRA-5459: remove node from seeds when it completely leaves the cluster.
    This version slightly refactors Gossiper by pulling th seeds list building code
    out of start() and into a shared method that removeEndpoint() will call.
    Also, reload the seeds list, then remove the dead node.
commit 72fde6f69579655ca6c582c7928cda137753b224
Author: Shay Banon <kimchy@gmail.com>
Date:   Mon May 11 01:19:13 2015 +0200

    Async fetch of shard started and store during allocation
    Today, when a primary shard is not allocated we go to all the nodes to find where it is allocated (listing its started state). When we allocate a replica shard, we head to all the nodes and list its store to allocate the replica on a node that holds the closest matching index files to the primary.

    Those two operations today execute synchronously within the GatewayAllocator, which means they execute in a sync manner within the cluster update thread. For large clusters, or environments with very slow disk, those operations will stall the cluster update thread, making it seem like its stuck.

    Worse, if the FS is really slow, we timeout after 30s the operation (to not stall the cluster update thread completely). This means that we will have another run for the primary shard if we didn't find one, or we won't find the best node to place a shard since it might have timed out (listing stores need to list all files and read the checksum at the end of each file).

    On top of that, this sync operation happen one shard at a time, so its effectively compounding the problem in a serial manner the more shards we have and the slower FS is...

    This change moves to perform both listing the shard started states and the shard stores to an async manner. During the allocation by the GatewayAllocator, if data needs to be fetched from a node, it is done in an async fashion, with the response triggering a reroute to make sure the results will be taken into account. Also, if there are on going operations happening, the relevant shard data will not be taken into account until all the ongoing listing operations are done executing.

    The execution of listing shard states and stores has been moved to their own respective thread pools (scaling, so will go down to 0 when not needed anymore, unbounded queue, since we don't want to timeout, just let it execute based on how fast the local FS is). This is needed sine we are going to blast nodes with a lot of requests and we need to make sure there is no thread explosion.

    This change also improves the handling of shard failures coming from a specific node. Today, those nodes were ignored from allocation only for the single reroute round. Now, since fetching is async, we need to keep those failures around at least until a single successful fetch without the node is done, to make sure not to repeat allocating to the failed node all the time.

    Note, if before the indication of slow allocation was high pending tasks since the allocator was waiting for responses, not the pending tasks will be much smaller. In order to still indicate that the cluster is in the middle of fetching shard data, 2 attributes were added to the cluster health API, indicating the number of ongoing fetches of both started shards and shard store.

    closes #9502
    closes #11101
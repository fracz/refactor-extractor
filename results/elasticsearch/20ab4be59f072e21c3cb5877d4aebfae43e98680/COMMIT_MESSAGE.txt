commit 20ab4be59f072e21c3cb5877d4aebfae43e98680
Author: Ali Beyad <ali@elastic.co>
Date:   Mon Jan 2 12:28:32 2017 -0600

    Cluster Explain API uses the allocation process to explain shard allocation decisions (#22182)

    This PR completes the refactoring of the cluster allocation explain API and improves it in the following two high-level ways:

     1. The explain API now uses the same allocators that the AllocationService uses to make shard allocation decisions. Prior to this PR, the explain API would run the deciders against each node for the shard in question, but this was not executed on the same code path as the allocators, and many of the scenarios in shard allocation were not captured due to not executing through the same code paths as the allocators.

     2. The APIs have changed, both on the Java and JSON level, to accurately capture the decisions made by the system. The APIs also now report on shard moving and rebalancing decisions, whereas the previous API did not report decisions for moving shards which cannot remain on their current node or rebalancing shards to form a more balanced cluster.

    Note: this change affects plugin developers who may have a custom implementation of the ShardsAllocator interface. The method weighShards has been removed and no longer has any utility. In order to support the new explain API, however, a custom implementation of ShardsAllocator must now implement ShardAllocationDecision decideShardAllocation(ShardRouting shard, RoutingAllocation allocation) which provides a decision and explanation for allocating a single shard. For implementations that do not support explaining a single shard allocation via the cluster allocation explain API, this method can simply return an UnsupportedOperationException.
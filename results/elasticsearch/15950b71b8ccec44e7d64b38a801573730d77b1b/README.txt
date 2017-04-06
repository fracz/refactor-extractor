commit 15950b71b8ccec44e7d64b38a801573730d77b1b
Author: Ali Beyad <ali@elastic.co>
Date:   Wed Oct 5 14:23:25 2016 -0400

    BalancedShardAllocator code improvements (#20746)

    This commit improves the logic flow of BalancedShardsAllocator in
    preparation for separating out components of this class to be used
    in the cluster allocation explain APIs.  In particular, this commit:

     1. Adds a minimum value for the index/shard balance factor settings (0.0)
     2. Makes the Balancer data structures immutable and pre-calculated at
        construction time.
     3. Removes difficult to follow labeled blocks / GOTOs
     4. Better logic for skipping over the same replica set when one of
        the replicas received a NO decision
     5. Separates the decision making logic for a single shard from the logic
        to iterate over all unassigned shards.
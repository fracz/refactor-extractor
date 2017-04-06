commit 314499cee078e42353b3373901368152d7c4fa4e
Author: Simon Willnauer <simonw@apache.org>
Date:   Wed Dec 18 11:38:29 2013 +0100

    Use existing datastructures from RoutingNodes to elect unassigned primaries

    Currently we trying to find a replica for a primary that is allocated by
    running through all shards in the cluster while RoutingNodes already has
    a datastructure keyed by shard ID for this. We should lookup this
    directly rather than using linear probing. This improves shard allocation performance
    by 5x.
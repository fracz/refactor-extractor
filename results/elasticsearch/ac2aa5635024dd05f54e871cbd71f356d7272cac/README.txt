commit ac2aa5635024dd05f54e871cbd71f356d7272cac
Author: Luca Cavanna <javanna@users.noreply.github.com>
Date:   Thu Nov 24 09:46:57 2016 +0100

    Cluster search shards improvements: expose ShardId, adjust visibility of some members (#21752)

    * ClusterSearchShardsGroup to return ShardId rather than the int shard id

    This allows more info to be retrieved, like the index uuid which is exposed through the ShardId object but was not available before

    * Make ClusterSearchShardsResponse empty constructor public

    This allows to receive such responses when sending ClusterSearchShardsRequests directly through TransportService (not using ClusterSearchShardsAction via Client), otherwise an empty response cannot be created unless the class that does it is in org.elasticsearch.action, admin.cluster.shards package

    * adjust visibility of ClusterSearchShards members
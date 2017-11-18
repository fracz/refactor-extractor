commit 26368aed3d89bf25d6d5cc4b8a0b13c71c154482
Author: Adrian Cole <adriancole@users.noreply.github.com>
Date:   Tue Jun 28 09:29:13 2016 +0800

    Adds properties that affect Elasticsearch index scalability (#1150)

    This adds the following properties, which help control the scalability
    of Zipkin's index in Elasticsearch.

        * `ES_INDEX_SHARDS`: The number of shards to split the index into. Each shard and its replicas
                             are assigned to a machine in the cluster. Increasing the number of shards
                             and machines in the cluster will improve read and write performance. Number
                             of shards cannot be changed for existing indices, but new daily indices
                             will pick up changes to the setting. Defaults to 5.
        * `ES_INDEX_REPLICAS`: The number of replica copies of each shard in the index. Each shard and
                               its replicas are assigned to a machine in the cluster. Increasing the
                               number of replicas and machines in the cluster will improve read
                               performance, but not write performance. Number of replicas can be changed
                               for existing indices. Defaults to 1. It is highly discouraged to set this
                               to 0 as it would mean a machine failure results in data loss.
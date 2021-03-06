commit e81804cfa47bb485b7736d7fe1575c53460a638a
Author: Simon Willnauer <simonw@apache.org>
Date:   Wed Jul 12 22:19:20 2017 +0200

    Add a shard filter search phase to pre-filter shards based on query rewriting (#25658)

    Today if we search across a large amount of shards we hit every shard. Yet, it's quite
    common to search across an index pattern for time based indices but filtering will exclude
    all results outside a certain time range ie. `now-3d`. While the search can potentially hit
    hundreds of shards the majority of the shards might yield 0 results since there is not document
    that is within this date range. Kibana for instance does this regularly but used `_field_stats`
    to optimize the indexes they need to query. Now with the deprecation of `_field_stats` and it's upcoming removal a single dashboard in kibana can potentially turn into searches hitting hundreds or thousands of shards and that can easily cause search rejections even though the most of the requests are very likely super cheap and only need a query rewriting to early terminate with 0 results.

    This change adds a pre-filter phase for searches that can, if the number of shards are higher than a the `pre_filter_shard_size` threshold (defaults to 128 shards), fan out to the shards
    and check if the query can potentially match any documents at all. While false positives are possible, a negative response means that no matches are possible. These requests are not subject to rejection and can greatly reduce the number of shards a request needs to hit. The approach here is preferable to the kibana approach with field stats since it correctly handles aliases and uses the correct threadpools to execute these requests. Further it's completely transparent to the user and improves scalability of elasticsearch in general on large clusters.
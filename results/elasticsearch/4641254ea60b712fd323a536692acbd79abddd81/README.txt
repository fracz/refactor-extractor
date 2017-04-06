commit 4641254ea60b712fd323a536692acbd79abddd81
Author: Ali Beyad <ali@elastic.co>
Date:   Wed Aug 31 11:58:19 2016 -0400

    Parameter improvements to Cluster Health API wait for shards (#20223)

    * Params improvements to Cluster Health API wait for shards

    Previously, the cluster health API used a strictly numeric value
    for `wait_for_active_shards`. However, with the introduction of
    ActiveShardCount and the removal of write consistency level for
    replication operations, `wait_for_active_shards` is used for
    write operations to represent values for ActiveShardCount. This
    commit moves the cluster health API's usage of `wait_for_active_shards`
    to be consistent with its usage in the write operation APIs.

    This commit also changes `wait_for_relocating_shards` from a
    numeric value to a simple boolean value `wait_for_no_relocating_shards`
    to set whether the cluster health operation should wait for
    all relocating shards to complete relocation.

    * Addresses code review comments

    * Don't be lenient if `wait_for_relocating_shards` is set
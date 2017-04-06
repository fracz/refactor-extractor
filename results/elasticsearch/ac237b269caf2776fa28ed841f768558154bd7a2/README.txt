commit ac237b269caf2776fa28ed841f768558154bd7a2
Author: Jason Tedor <jason@tedor.me>
Date:   Thu Jan 14 11:30:03 2016 -0500

    Add handling of channel failures when starting a shard

    This commit adds handling of channel failures when starting a shard to
    o.e.c.a.s.ShardStateAction. This means that shard started requests
    that timeout or occur when there is no master or the master leaves
    after the request is sent will now be retried from here. The listener
    for a shard state request will now only be notified upon successful
    completion of the shard state request, or when a catastrophic
    non-channel failure occurs.

    This commit also refactors the handling of shard failure requests so
    that the two shard state actions of shard failure and shard started
    now share the same channel-retry and notification logic.
commit ae3a20a1e43f01c1bc1441824fcfd086c89d18e6
Author: Janak Ramakrishnan <janakr@google.com>
Date:   Fri Jan 8 16:24:29 2016 +0000

    Stream result of TargetPattern#eval to a callback instead of returning it directly, and pass a Query callback in when resolving target patterns. This means that the targets a pattern resolves to can be processed incrementally.

    This is the fifth step in a series to allow processing large sets of targets in query target patterns via streaming batches rather than all at once. This should improve performance for SkyQueryEnvironment for certain classes of large queries.

    --
    MOS_MIGRATED_REVID=111696713
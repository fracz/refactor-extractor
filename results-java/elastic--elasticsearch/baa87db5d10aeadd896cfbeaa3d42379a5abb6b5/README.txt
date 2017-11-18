commit baa87db5d10aeadd896cfbeaa3d42379a5abb6b5
Author: Yannick Welsch <yannick@welsch.lu>
Date:   Fri Jul 7 20:04:28 2017 +0200

    Harden global checkpoint tracker

    This commit refactors the global checkpont tracker to make it more
    resilient. The main idea is to make it more explicit what state is
    actually captured and how that state is updated through
    replication/cluster state updates etc. It also fixes the issue where the
    local checkpoint information is not being updated when a shard becomes
    primary. The primary relocation handoff becomes very simple too, we can
    just verbatim copy over the internal state.

    Relates #25468
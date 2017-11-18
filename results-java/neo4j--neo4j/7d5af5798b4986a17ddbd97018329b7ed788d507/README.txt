commit 7d5af5798b4986a17ddbd97018329b7ed788d507
Author: Max Sumrall <max.sumrall@neotechnology.com>
Date:   Tue May 24 15:37:38 2016 +0200

    Safe pruning of the segmented raft log.

    - When attemping to prune, the pruner will not remove files which
    have open readers on the file. This behavior is unchanged but
    clarified by a test.

    - The RaftLogShipper will attempt to send the requested entry.
    If that entry is pruned before the raft log shipper can send it,
    it will send the oldest entry it has, which will likely not be
    accepted by the follower and cause the follower to request again.
     The second request will be seen after pruning has happened,
    and the follower will be notified that a snapshot is needed.

    - Rename the setting "CoreEdgeClusterSetting.raft_log_pruning"
    to "CoreEdgeClusterSetting.raft_log_pruning_strategy" with default value "keep_all".

    - Removed RaftLogCompactedException. There is no need to differentiate
     between pruned indexes and indexes that do not yet exist.

    - Properly parse prune strategy for PhysicalRaftLog.

    - SegmentRaftLog EntryStore is refactored to EntryCursor.

    - Cluster now implements AutoClosable.
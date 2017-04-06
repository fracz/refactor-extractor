commit 93beea1f670f1e0d53bc3fd3ee671bc235d5f1aa
Author: Igor Motov <igor@motovs.org>
Date:   Wed Jun 10 12:57:45 2015 -0400

    Snapshot/Restore: Move in-progress snapshot and restore information from custom metadata to custom cluster state part

    Information about in-progress snapshot and restore processes is not really metadata and should be represented as a part of the cluster state similar to discovery nodes, routing table, and cluster blocks. Since in-progress snapshot and restore information is no longer part of metadata, this refactoring also enables us to handle cluster blocks in more consistent manner and allow creation of snapshots of a read-only cluster.

    Closes #8102
commit 24bc8d331eb28a686233573bdcaf9cadd9a6dc5b
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Mon Oct 6 17:28:14 2014 +0200

    Recovery: refactor RecoveryTarget state management

    This commit rewrites the state controls in the RecoveryTarget family classes to make it easier to guarantee that:
    - recovery resources are only cleared once there are no ongoing requests
    - recovery is automatically canceled when the target shard is closed/removed
    - canceled recoveries do not leave temp files behind when canceled.

    Highlights of the change:
    1) All temporary files are cleared upon failure/cancel (see #7315 )
    2) All newly created files are always temporary
    3) Doesn't list local files on the cluster state update thread (which throw unwanted exception)
    4) Recoveries are canceled by a listener to IndicesLifecycle.beforeIndexShardClosed, so we don't need to explicitly call it.
    5) Simplifies RecoveryListener to only notify when a recovery is done or failed. Removed subtleties like ignore and retry (they are dealt with internally)

    Closes #8092 , Closes #7315
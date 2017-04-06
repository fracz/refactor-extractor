commit 59d9f7e1579ca70973d14e94da0b8794e60f3b85
Author: Igor Motov <igor@motovs.org>
Date:   Tue Jun 2 12:18:44 2015 -1000

    Improve snapshot creation and deletion performance on repositories with large number of snapshots

    Each shard repository consists of snapshot file for each snapshot - this file contains a map between original physical file that is snapshotted and its representation in repository. This data includes original filename, checksum and length. When a new snapshot is created, elasticsearch needs to read all these snapshot files to figure which file are already present in the repository and which files still have to be copied there. This change adds a new index file that contains all this information combined into a single file. So, if a repository has 1000 snapshots with 1000 shards elasticsearch will only need to read 1000 blobs (one per shard) instead of 1,000,000 to delete a snapshot. This change should also improve snapshot creation speed on repositories with large number of snapshot and high latency.

    Fixes #8958
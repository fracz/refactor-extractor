commit f379a13fb412ef5c9d63c24e0550ed42b6be7591
Author: Igor Motov <igor@motovs.org>
Date:   Tue Jun 23 18:01:32 2015 -0400

    Extract all shard-level snapshot operation into dedicated SnapshotShardsService

    Currently the SnapshotsService is concerned with both maintaining the global snapshot lifecycle on the master node as well as responsible for keeping track of individual shards on the data nodes. This refactoring separates two areas of concerns by moving all shard-level operations into a separate SnapshotShardsService.

    Closes #11756
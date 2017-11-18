commit 80665ec28f4fde484c35e2935e8d06aabe902841
Author: Googler <noreply@google.com>
Date:   Thu Mar 17 22:34:52 2016 +0000

    Part 3 of 5: Merging semantics.

    Introduces the AndroidDataMerger, MergeConflict, and UnwrittenMergedAndroidData which is the entry point in the AndroidResourceProcessing *AndroidData lifecycle.

    Also, refactors the AndroidDataSet parsing of resources, making it functionally immutable.

    --
    MOS_MIGRATED_REVID=117492690
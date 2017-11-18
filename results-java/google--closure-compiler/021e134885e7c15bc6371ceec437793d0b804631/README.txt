commit 021e134885e7c15bc6371ceec437793d0b804631
Author: tbreisacher <tbreisacher@google.com>
Date:   Mon Jun 5 11:58:16 2017 -0700

    Small performance improvement. Make ReachablePredicate a singleton so that we don't end up creating a new Predicate object for every scope.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=158042825
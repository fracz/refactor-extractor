commit e97bba143bcb18eef62a18829542abf576f0856a
Author: Eric Fellheimer <felly@google.com>
Date:   Mon Jun 20 17:57:50 2016 +0000

    Minor improvement to cycle detection algorithm: Do not recurse into done nodes, where there is no work to do anyway.

    This triggered some non-determinism that we explicitly workaround in the unit tests.

    Also add a comment about a potential but unrelated optimization.

    --
    MOS_MIGRATED_REVID=125355303
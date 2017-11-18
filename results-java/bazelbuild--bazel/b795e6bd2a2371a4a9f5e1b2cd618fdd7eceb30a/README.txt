commit b795e6bd2a2371a4a9f5e1b2cd618fdd7eceb30a
Author: Nathan Harmata <nharmata@google.com>
Date:   Thu Feb 4 01:10:19 2016 +0000

    Have GlobFunction make use of the assumption that the glob's package exists by having it not declare a dep on the PackageLookupValue for the package. This optimization means that a BUILD file edit doesn't (necessarily) invalidate all the globs in the package; the PackageLookupValue node would get change-pruned but we still pay the very small cost of invalidating unnecessarily.

    Also slightly improve variable naming in GlobFunctionTest.

    --
    MOS_MIGRATED_REVID=113799936
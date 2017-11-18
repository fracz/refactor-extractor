commit bba75d81fe5c740ca79fbb2c179a30e1ba3f0f74
Author: Lukacs Berki <lberki@google.com>
Date:   Tue Jun 14 09:08:29 2016 +0000

    Report cycles involving aspects correctly.

    This involved refactoring BuildViewTestCase a bit so that its behavior is closer to that of Bazel with --experimental_interleave_loading_and_analysis.

    RELNOTES:

    --
    MOS_MIGRATED_REVID=124816624
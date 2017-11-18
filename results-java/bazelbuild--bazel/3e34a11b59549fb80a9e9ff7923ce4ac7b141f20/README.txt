commit 3e34a11b59549fb80a9e9ff7923ce4ac7b141f20
Author: Ulf Adams <ulfjack@google.com>
Date:   Tue Feb 9 16:31:06 2016 +0000

    Properly report loading errors during configuration creation.

    This only applies to interleaved loading and analysis - the production code
    is fine.

    Add tests for the RedirectChaser, the fdoOptimize code path, the XcodeConfig,
    and the Jvm loader. Unfortunately, the configuration factory we internally
    create by default contains a mock Jvm loader implementation. Since that is one
    Yak too many right now, I'm adding a temporary method to the AnalysisMock.

    I added the tests to BuildViewTest for now; technically, they ought to go
    into the language-specific test cases, but that would require more refactoring
    as those don't currently run with interleaved loading and analysis.

    --
    MOS_MIGRATED_REVID=114221476
commit cd20fb98149d198bea492d2052c177fc6554a415
Author: Chengnian Sun <cnsun@google.com>
Date:   Fri Mar 24 19:15:56 2017 +0000

    refactoring: move the code parsing and validating command line arguments into a separate method, so that the main becomes shorter. Also move most of the code in main to an instance method.

    RELNOTES: n/a

    --
    PiperOrigin-RevId: 151155142
    MOS_MIGRATED_REVID=151155142
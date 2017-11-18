commit 04c83e3bfe3c75c237086c8b32d30886273fb31b
Author: Lóránt Pintér <lorant@gradle.com>
Date:   Sat Sep 3 00:39:34 2016 +0200

    Refactor code for easier readability

    - folded some classes into TaskFilePropertyCompareStrategy
    - merged and cleaned up tests for compare strategies
    - better name for TaskFilePropertyPathSensitivity

    +review REVIEW-6170
commit 291a5b03b442e86883fd0ed40192c2eb4a16bc73
Author: bradfordcsmith <bradfordcsmith@google.com>
Date:   Mon May 1 22:23:07 2017 -0700

    Continue refactoring of compilation.

    Remove public access to some methods that shouldn't have it.
    Refactor logic inside checkAndTranspileAndOptimize().
    Next step expose public methods that invoke parts of
    checkAndTranspileAndOptimize() in a compiler thread as it does.
    Rewrite AbstractCommandLineRunner code to use those.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=154799837
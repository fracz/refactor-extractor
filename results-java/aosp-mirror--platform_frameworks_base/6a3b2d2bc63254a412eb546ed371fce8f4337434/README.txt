commit 6a3b2d2bc63254a412eb546ed371fce8f4337434
Author: David Brazdil <dbrazdil@google.com>
Date:   Fri Apr 8 16:09:06 2016 +0100

    Refactor return values of performDexOpt

    PackageDexOptimizer.performDexOpt would return DEX_OPT_PERFORMED if
    dexopt succeeded on the package and DEX_OPT_SKIPPED otherwise, even
    if dexopt failed. This patch fixes that and cleans up the code.

    PackageManagerService.performDexOpt* would return true only if
    PackageDexOptimizer.performDexOpt returned DEX_OPT_PERFORMED.
    Consequently, it would return false when dexopt was not needed. This
    patch refactors the code to return true unless PackageDexOptimizer
    returns DEX_OPT_FAILED and documents the behaviour.

    Bug: 28082762
    Change-Id: Ica73e67ab02025ef5619746bb8c465c96b72846b
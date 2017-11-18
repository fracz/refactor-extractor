commit 2fae552711d1e3f3034831f2207ba2d16f97e4af
Author: johnlenz <johnlenz@google.com>
Date:   Thu Oct 12 10:37:14 2017 -0700

    In preparation for refactoring RemoveUsedVars.CallSiteOptimizer, fix RemoveUnusedVars to properly track unused parameters.  In the process improve the support for destructuring, rest and default parameters

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=171978370
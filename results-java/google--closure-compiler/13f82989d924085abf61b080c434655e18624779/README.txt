commit 13f82989d924085abf61b080c434655e18624779
Author: johnlenz <johnlenz@google.com>
Date:   Mon Oct 30 11:35:51 2017 -0700

    In preparation for the rollfoward of the OptimizeCalls improvements, add support for an IS_UNUSED_PARAMETER Node annotation.  This will be used by the RemoveUnusedVars pass to communicate to the OptimizeParameters pass that a parameter is unused.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=173923415
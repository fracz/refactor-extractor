commit c9d87c4ece55c12bd536870bfa19f55381cdaba9
Author: tbreisacher <tbreisacher@google.com>
Date:   Wed Nov 11 17:42:49 2015 -0800

    A few cleanups and small changes in preparation for adding a SuggestedFix that sorts goog.require/goog.provide statements.

    NodeUtil.getBestJSDocInfo now works on EXPR_RESULT nodes, where the JSDoc is typically on the first child of the node. This eliminates a couple TODOs in the refactoring tools.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=107641393
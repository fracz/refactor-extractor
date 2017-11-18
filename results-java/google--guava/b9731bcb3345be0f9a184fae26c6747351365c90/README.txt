commit b9731bcb3345be0f9a184fae26c6747351365c90
Author: jasexton <jasexton@google.com>
Date:   Thu Feb 18 13:17:37 2016 -0800

    Create an abstract base class for our graph implementations. Eliminate lots of redundant code! Also improves the (currently incorrectly documented) runtime of removeNode() in IncidenceSetUndirectedGraph on multigraphs from O(degree^2) to O(degree).
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=114995238
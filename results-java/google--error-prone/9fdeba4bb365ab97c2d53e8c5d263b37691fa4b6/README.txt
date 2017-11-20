commit 9fdeba4bb365ab97c2d53e8c5d263b37691fa4b6
Author: Eddie Aftandilian <eaftan@google.com>
Date:   Thu Apr 23 11:03:19 2015 -0700

    Multiple improvements to WaitNotInLoop.

    Changes:
    1) Now detects only the case where the wait is not in a loop. No longer detects
       the case where it is in a loop, but the synchronized block is inside the
       loop. (I will move that to a separate check)
    2) Much better explanation in the markdown file, including suggestions for how
       to fix common patterns.
    3) Now matches Condition.await() in addition to Object.wait().
    4) Now produces a suggested fix for the simple case of changing the enclosing
       if to a while.

    RELNOTES:
    - Improvements to WaitNotInLoop. Detects a somewhat different set of problems,
      but explains them better, and fixes a subset.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=91902359
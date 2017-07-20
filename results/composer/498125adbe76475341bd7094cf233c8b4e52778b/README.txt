commit 498125adbe76475341bd7094cf233c8b4e52778b
Author: Nils Adermann <naderman@naderman.de>
Date:   Tue Jun 7 22:43:26 2011 +0200

    Assertions are now properly decided before running the main solver.

    Updating does not work yet. The test case works fine for installing/removing
    the correct packages however. Weak (update/feature) rules are entirely ignored
    so far.

    Watches are on literal ids rather than literals to save on function calls
    and memory usage. So a few methods for literals now have an id counter part.
    This should probably be refactored to have the literal versions call the
    id ones instead.
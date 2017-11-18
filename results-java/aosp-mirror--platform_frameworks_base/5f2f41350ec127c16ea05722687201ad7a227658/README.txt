commit 5f2f41350ec127c16ea05722687201ad7a227658
Author: Christopher Tate <ctate@google.com>
Date:   Mon Sep 26 13:10:38 2011 -0700

    Don't hang in restore if the transport reports failure

    Casualty of the recent refactoring: in this particular error case,
    the restore sequence wasn't being directed into the finalization
    state.  Fixes bug 5336295.

    Change-Id: Ibf5570cd1003e123da8b561685de8479663340ce
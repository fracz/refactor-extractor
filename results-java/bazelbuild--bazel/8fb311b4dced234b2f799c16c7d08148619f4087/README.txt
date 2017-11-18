commit 8fb311b4dced234b2f799c16c7d08148619f4087
Author: Lukacs Berki <lberki@google.com>
Date:   Tue Feb 14 11:36:47 2017 +0000

    Reinstate idleness checks where the server self-terminates when it's idle and there is either too much memory pressure or the workspace directory is gone.

    Arguably, it should kill itself when the workspace directory is gone regardless of whether it's idle or not, but let's first get us back to a known good state, then we can think about improvements.

    --
    PiperOrigin-RevId: 147454329
    MOS_MIGRATED_REVID=147454329
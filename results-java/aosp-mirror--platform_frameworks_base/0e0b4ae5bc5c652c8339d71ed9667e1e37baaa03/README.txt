commit 0e0b4ae5bc5c652c8339d71ed9667e1e37baaa03
Author: Christopher Tate <ctate@android.com>
Date:   Mon Aug 10 16:13:47 2009 -0700

    Don't let bmgr leave a restore session hanging on error

    Specifically, don't wait for the RestoreObserver to be informed that the restore
    has completed unless performRestore() ran.  We were winding up in a case where
    bmgr was hanging forever waiting on a nonexistent restore process instead of
    calling endRestoreSession().

    Also improve the documentation, explicitly calling out the need to call
    endRestoreSession() even if previous operations on the session were
    unsuccessful.
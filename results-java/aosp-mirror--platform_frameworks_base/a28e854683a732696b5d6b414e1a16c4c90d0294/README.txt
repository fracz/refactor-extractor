commit a28e854683a732696b5d6b414e1a16c4c90d0294
Author: Christopher Tate <ctate@google.com>
Date:   Mon Sep 12 13:45:21 2011 -0700

    Move full backup/restore onto dedicated threads

    Running full backup/restore on the Backup Manager looper thread causes problems.
    It not only interfered with the delayed-Message timeout processing; in the case
    of installing apks during restore it also interfered fatally with the interaction
    between the Package Manager and install-time restore of data from the cloud.

    The long-term right thing to do here will be a refactoring of full backup and
    restore to be structured as the sort of state-machine process that incremental
    backup and restore now use.  This is particularly thorny in the case of full
    restore (due to the Package Manager interactions), and full backup/restore are
    considered experimental at this point, so that refactoring is deferred to a
    future release.  The current process is essentially standalone, so the bug is
    fixed here pro tem by letting it run to completion on its own thread, freeing
    the looper for normal work.

    Fixes bug 5173450

    Change-Id: I659a61afa18ffe7fde1a07f7fa0e860d5e8d5a89
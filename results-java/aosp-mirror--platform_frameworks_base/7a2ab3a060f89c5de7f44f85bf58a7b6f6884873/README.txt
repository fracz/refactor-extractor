commit 7a2ab3a060f89c5de7f44f85bf58a7b6f6884873
Author: Matthew Williams <mjwilliams@google.com>
Date:   Wed Sep 11 14:25:51 2013 -0700

    Fix deadlock occurring on account add in SyncManager.

    deadlock caused by autosync from changing sync settings.
    Some minor refactoring of the function that caused the deadlock
    Bug: 10666901
    Change-Id: I7cf901b1954e59dbb0bc71a5de23117353b460b1
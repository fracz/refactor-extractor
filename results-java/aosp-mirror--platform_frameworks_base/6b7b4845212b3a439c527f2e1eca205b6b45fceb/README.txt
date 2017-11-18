commit 6b7b4845212b3a439c527f2e1eca205b6b45fceb
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon Jun 14 17:17:44 2010 -0700

    Various improvements to battery stats collection

    We now clear the battery stats when unplugging after the
    battery is full.  This allows us to use the "total" stats as
    a new "since last charged" stat.  Total is gone.  I never used
    it, it was worthless.  Since last charged is a lot more
    interesting.

    The battery history now collects a lot more stats, and keeps
    control over how much it can collect.  Printing is now more
    descriptive.

    The kinds of stats have been renamed to SINCE_UNPLUGGED and
    SINCE_DISCHARGED.  The other two stats are still there, but
    no longer printed; a future change will eliminate them
    completely along with all of their state.

    Change-Id: I4e9fcfcf8c30510092c76a8594f6021e9502fbc1
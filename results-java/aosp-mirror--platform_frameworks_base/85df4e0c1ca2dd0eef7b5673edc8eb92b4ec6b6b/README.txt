commit 85df4e0c1ca2dd0eef7b5673edc8eb92b4ec6b6b
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Jul 23 19:05:45 2013 -0700

    Fix another problem with procstats bucketing.

    We were now propagating the screen on state when updating
    all process states, so they would get pushed into the screen
    off bucket always even if the screen was on.  Oops!

    Also improve the summary output when dumping a single package
    to be more summary-like.

    Change-Id: I16c640f9dc02d6db8c66aeb1c720f67beab60635
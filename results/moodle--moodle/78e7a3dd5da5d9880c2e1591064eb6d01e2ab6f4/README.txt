commit 78e7a3dd5da5d9880c2e1591064eb6d01e2ab6f4
Author: tjhunt <tjhunt>
Date:   Tue Jul 8 16:33:47 2008 +0000

    MDL-15452 - Put the OU quiz navigation improvements into the Moodle codebase - quite a lot of progress, sorry I am committing this as a big lump, but it took me a while to get the code back to a working state.

    MDL-15537 - create oo attemptlib.php to hold shared code between attempt, summary and review.php
    MDL-15541 - Refactor starting a new attempt into a new file startattempt.php
    MDL-15538 - Rework attempt.php to use attemptlib.php
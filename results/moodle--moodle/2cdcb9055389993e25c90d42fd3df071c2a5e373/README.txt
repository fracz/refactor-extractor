commit 2cdcb9055389993e25c90d42fd3df071c2a5e373
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Sat Jul 14 09:06:07 2012 +0100

    MDL-34164 quiz reports: only try to show graphs if GD is installed.

    if (empty($CFG->gdversion)) { seems to be the common idiom for this.
    I refactored the graph output into the renderer, to avoid having to
    duplicate that test three times.
commit 1a466e2f03eede375e574998b5db4b3d65949942
Author: raveren <raveren@gmail.com>
Date:   Tue Sep 23 12:53:24 2014 +0300

    Huge update, months of work, I'm piling all these changes together so I don't break any commit. Notable changes:
     * removed ajax detection and all associated features :(
     * Kint::enabled() can now be used to force output mode: Kint::enabled(Kint::MODE_CLI);
     * the '~' modifier is now used to force whitespace output
     * added ddd() function - equivalent to dd() for Laravel users
     * refactored and improved cli detection, cli colors and added various whitespace output improvements
     * removed keyFilterCallback and traceCleanupCallback options. Not the proper way to solve the problem they've been devised to solve.
     * timestamp parser now detects javascript microsecond times
     * added php 5.5 source parser fixes
     * lots of minor fixes, refactorings & improvements
commit 3d193d5519df6b3b4c8deaa0f15434d700fd896d
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Wed Mar 25 19:59:00 2015 +0300

    Prevent insertion of redundant import statement in the original file of moved element

    It happens if we move global variable that references itself, e.g.
    VAR = lambda: VAR. Redundant import was optimized out at the end of
    refactoring though.
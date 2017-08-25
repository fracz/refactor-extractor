commit 931bba6054cf2d9c536b1d59bedacb8b41dc7933
Author: David Mudrák <david@moodle.com>
Date:   Tue Oct 11 20:21:30 2016 +0200

    MDL-54945 workshop: Introduce workshop::check_group_membership() method

    This is just a refactored code block present at two different places. We
    will need the same logic in the portfolio caller class soit made sense
    to implement it as a new method, rather than make another copy&paste.

    As a side effect, the logic is now properly unit tested.
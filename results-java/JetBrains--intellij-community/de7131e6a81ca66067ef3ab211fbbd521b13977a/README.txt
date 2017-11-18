commit de7131e6a81ca66067ef3ab211fbbd521b13977a
Author: Denis.Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Tue Feb 26 13:55:24 2013 +0400

    CR-IC-41 Vertical line block match disapear view line in java code (IDEA-99572)

    1. Limit 'shiftForward()' by line end offset;
    2. Minor refactoring;
    3. Don't show indent guide for commented lines which would remove indent on un-commenting;
commit d65b912df28a8c5abc2e47c6978eb873c1166d6c
Author: Damyon Wiese <damyon@moodle.com>
Date:   Fri Sep 18 17:01:42 2015 +0800

    MDL-51139 tool_lp: Fix some javascript that was broken by a merge

    The user learning plans page menus were broken by some refactoring.

    Fixed during merge by Fred:
    - Added space between icons and strings in action menu
    - Recent changes caused the plans page to die when listing templates
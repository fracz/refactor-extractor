commit 32b76de89394f47bf864d583155e1805a1f9e823
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Fri Apr 24 16:41:36 2015 +0300

    PY-11552 Keep maximum 1 empty line between declarations and in code by default

    Fix affected test data where reformat is invoked during refactoring.
    Also use entire sample provided by user in the corresponding test case
    for the issue.
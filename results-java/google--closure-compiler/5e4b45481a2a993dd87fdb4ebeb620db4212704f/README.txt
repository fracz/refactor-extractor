commit 5e4b45481a2a993dd87fdb4ebeb620db4212704f
Author: lharker <lharker@google.com>
Date:   Wed Nov 15 11:41:55 2017 -0800

    Associate transpilation passes with the specific features they transpile.

    This allows transpiling from ES2017 or ES2016 down to ES2015.

    With some additional refactoring, this can also speed up some builds, because we can skip transpilation passes for scripts not containing the specific features that a certain pass handles. (Right now this is impossible because we don't have an up-to-date set of all Features that a script contains.)

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=175856605
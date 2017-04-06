commit 15f33e61b74d4c65291f0ad157f89bd898f3ab96
Author: Jason Tedor <jason@tedor.me>
Date:   Mon May 2 12:22:40 2016 -0400

    Kill redundant conditional in BootstrapCheck#check

    This commit removes an unnecessary if statement in Bootstrap#check. The
    removed if statement was duplicating the conditionals in the nested if
    statements and was merely an artifact of an earlier refactoring.
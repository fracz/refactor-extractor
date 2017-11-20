commit 20ea90891d96e3fb19be71cc710fd79e1838fd3c
Author: Eddie Aftandilian <eaftan@google.com>
Date:   Thu May 7 17:02:32 2015 -0700

    Update CovariantEquals check

    Detects more cases and doesn't provide a fix when it doesn't make sense. Also
    stops flagging static equals methods.  And improves the documentation.

    RELNOTES:
    - Improvements to CovariantEquals/NonOverridingEquals check.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=93082349
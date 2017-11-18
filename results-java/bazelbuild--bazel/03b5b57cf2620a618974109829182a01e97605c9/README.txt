commit 03b5b57cf2620a618974109829182a01e97605c9
Author: Florian Weikert <fwe@google.com>
Date:   Wed Nov 2 13:34:09 2016 +0000

    Multiplying strings with negative numbers no longer leads to an exception.

    This CL also contains a small refactoring that should make the introduction of list-int-mulitplication easier.

    --
    MOS_MIGRATED_REVID=137938998
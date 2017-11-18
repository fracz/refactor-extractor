commit 8973201eac47ce14afd6b50e9c4ef3f016251bd2
Author: Rumou Duan <rduan@google.com>
Date:   Fri Feb 17 15:42:47 2017 +0000

    Switch J2ObjC to use compile-time jars (interface and header jars) instead of actual class jars. This should improve the action cache hit for J2ObjC translation actions.

    --
    PiperOrigin-RevId: 147836893
    MOS_MIGRATED_REVID=147836893
commit 01ee6f6cf7de9fb98fc3a88509b43416caa71040
Author: Jeff Brown <jeffbrown@google.com>
Date:   Fri May 2 16:59:26 2014 -0700

    Plumb display state and interactive information to BatteryStats. (DO NOT MERGE)

    Fixes an issue where dozing was treated the same as the screen
    being fully on.  Now dozing is treated the same as the screen
    being fully off which is slightly better.  The decision of how
    to represent this state is now internal to the battery stats
    so it can be improved later.

    Removed noteInputEvent() since it is unused.

    Bug: 14480844
    Change-Id: Iee8cf8dce1a1f91c62678bb6d3d9fe567ad6db42
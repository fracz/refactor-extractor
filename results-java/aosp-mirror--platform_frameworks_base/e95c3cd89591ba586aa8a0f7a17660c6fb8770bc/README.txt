commit e95c3cd89591ba586aa8a0f7a17660c6fb8770bc
Author: Jeff Brown <jeffbrown@google.com>
Date:   Fri May 2 16:59:26 2014 -0700

    Plumb display state and interactive information to BatteryStats.

    Fixes an issue where dozing was treated the same as the screen
    being fully on.  Now dozing is treated the same as the screen
    being fully off which is slightly better.  The decision of how
    to represent this state is now internal to the battery stats
    so it can be improved later.

    Removed noteInputEvent() since it is unused.

    Bug: 14480844
    Change-Id: Iee8cf8dce1a1f91c62678bb6d3d9fe567ad6db42
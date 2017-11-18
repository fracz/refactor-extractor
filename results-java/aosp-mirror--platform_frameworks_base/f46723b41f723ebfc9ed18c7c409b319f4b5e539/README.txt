commit f46723b41f723ebfc9ed18c7c409b319f4b5e539
Author: Christopher Tate <ctate@google.com>
Date:   Thu Jan 26 14:19:24 2012 -0800

    Implement background vs foreground broadcasts

    Before now, receiving a broadcast would cause a process to be hoisted
    to foreground priority / cgroup.  This is no longer the case: broadcasts
    by default are handled in the background, with a suitably increased
    timeout interval.  When a given broadcast needs to be dealt with in a
    more timely manner, the issuer can set the new FLAG_BROADCAST_FOREGROUND
    flag on the Intent, which will produce the old foreground-priority
    behavior.

    To avoid priority inversions, foreground broadcasts are tracked on a
    separate outgoing queue and can be in flight simultaneously with a
    background-priority broadcast.  If there is already a background-level
    broadcast in flight to a given app and then a foreground-level one is
    dispatched to that app, the app [and its handling of both broadcasts]
    will be properly hoisted to foreground priority.

    This change is also essentially the first step towards refactoring the
    broadcast-handling portions of the Activity Manager into a more
    independent existence.  Making BroadcastQueue a top-level class and
    regularizing its operation viz the primary Activity Manager operation
    is the next step.

    Change-Id: If1be33156dc22dcce318edbb5846b08df8e7bed5
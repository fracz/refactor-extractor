commit 58482c55d0848f9675b551cf2fea4990336f0b31
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Mon Feb 8 17:49:17 2016 -0700

    Make ServiceWatcher encryption-aware.

    This utility class automates the process of binding to the best
    matching service, including rebinding as packages change over time.

    This design means it's easy to become encryption-aware: we use the
    default PackageManager behavior that filters components based on
    their encryption-aware status, and we just kick off another
    evaluation pass once the user is unlocked.

    This change cleans up some of the internal logic so we only rebind
    when the implementation switches, and it fixes several bugs along
    the way.  For example, we would never trigger a rebind if a service
    was refactored to a different ComponentName.  Another subtle bug was
    that we'd never fallback to an older implementation if a higher
    version was uninstalled/disabled.  And finally, if all implementations
    were uninstalled/disabled, we'd leave the last connection bound.

    Bug: 26280056
    Change-Id: I259af78e6564d61353a772ac03cf5799a398d535
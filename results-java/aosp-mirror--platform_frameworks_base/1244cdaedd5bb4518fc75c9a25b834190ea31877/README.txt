commit 1244cdaedd5bb4518fc75c9a25b834190ea31877
Author: Jeff Brown <jeffbrown@google.com>
Date:   Tue Jun 19 16:44:46 2012 -0700

    Clean up PowerManager API.

    Mostly just moved the code around and improved the docs.

    Fixed a minor inefficiency in WakeLock.setWorkSource() where
    we would tell the power manager that the work source was changed
    even when the old work source and new work source were both null.

    Fixed a bug in WakeLock.setWorkSource() where we would not
    notify the power manager that the work source was changed if
    there was no work source previously specified.

    Added synchronized to WakeLock.setReferenceCounted.

    Added a checked in WakeLock.acquireLocked() and WakeLock.release()
    to check whether the wake lock is actually not held / held
    before performing the corresponding operation.  This change avoids
    making redundant calls into the power manager service in the
    case where the wake lock is not reference counted and acquire()
    or release() have been called multiple times.

    Made the PowerManager and WakeLock classes final.  They are not
    directly instantiable by applications so this change does not
    break the API.

    Removed a little dead code (one private constructor and an
    unused constant).

    Change-Id: I4e10cf893506115938a35756136c101256dccf30
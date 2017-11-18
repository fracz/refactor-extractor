commit 260c3c77d9b340164e055f87002c64d78da6e836
Author: Dianne Hackborn <hackbod@google.com>
Date:   Sun Jan 30 16:55:55 2011 -0800

    Fix issue #3381489: IllegalStateException: attempt to re-open...

    ...an already-closed object: android.database.sqlite.SQLiteQuery

    It turns out there is a state we are missing -- the loader is
    still needed, but in the inactive list.  In this case the loader
    needs to continue holding on to its current data, and not deliver
    any new data (which would result in it releasing its old data).

    This introduces the new state to Loader, and uses it in
    AsyncTaskLoader so all subclasses of that should get the new
    correct behavior.

    A further improvement would be to unregister CursorLoader's
    content listener when going in to this state, but that can
    wait for later.

    Change-Id: I6d30173b94f8e30b5be31d018accd328cc3388ec
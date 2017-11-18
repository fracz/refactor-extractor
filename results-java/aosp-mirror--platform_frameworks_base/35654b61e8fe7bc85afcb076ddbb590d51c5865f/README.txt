commit 35654b61e8fe7bc85afcb076ddbb590d51c5865f
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon Jan 14 17:38:02 2013 -0800

    More work on App Ops service.

    Implemented reading and writing state to retain information
    across boots, API to retrieve state from it, improved location
    manager interaction to monitor both coarse and fine access
    and only note operations when location data is being delivered
    back to app (not when it is just registering to get the data at
    some time in the future).

    Also implement tracking of read/write ops on contacts and the
    call log.  This involved tweaking the content provider protocol
    to pass over the name of the calling package, and some
    infrastructure in the ContentProvider transport to note incoming
    calls with the app ops service.  The contacts provider and call
    log provider turn this on for themselves.

    This also implements some of the mechanics of being able to ignore
    incoming provider calls...  all that is left are some new APIs for
    the real content provider implementation to be involved with
    providing the correct behavior for query() (return an empty
    cursor with the right columns) and insert() (need to figure out
    what URI to return).

    Change-Id: I36ebbcd63dee58264a480f3d3786891ca7cbdb4c
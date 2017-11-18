commit 593334ab70a8341c7d24d71a377ab5617e3f4ab7
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Jun 30 14:38:17 2015 -0700

    Fix issue #22124996: VI: Command Request not Active

    Just forgot to add the request to the active set.

    Also eradicate a bunch of old cruft that has been replaced
    by the final APIs, and improve voice interaction test to
    sit fully on top of the final APIs and have a test for
    command request.

    Change-Id: Ieff7a6165ebf2a4c5fb80c1ebd020511a2ae63ee
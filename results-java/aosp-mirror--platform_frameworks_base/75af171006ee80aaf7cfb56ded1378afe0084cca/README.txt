commit 75af171006ee80aaf7cfb56ded1378afe0084cca
Author: Jeff Brown <jeffbrown@google.com>
Date:   Sun Nov 10 18:54:03 2013 -0800

    Changes to support new screen cast settings screen.

    Fixed the Preference ordering code to consider the case where
    two preferences might have the same order.  In that case, it
    falls back on the title to disambiguate.  Previous behavior was
    undefined (and technically not stable).

    Expose the wifi display device address.

    Perform wifi display scans every 10 seconds instead of every 15
    to improve reponsiveness.

    Make sure to define routes for wifi displays that we are connecting
    to even if they are not yet paired.  Simplified the logic for
    adding and removing these routes to avoid possibly getting out
    of sync and leaving stale routes behind.

    Fix wifi display notification icon.

    Bug: 11257292
    Change-Id: I8ac15fb17d83758c0bdce80399e12723c367b83c
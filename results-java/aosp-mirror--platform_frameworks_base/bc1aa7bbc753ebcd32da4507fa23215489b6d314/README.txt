commit bc1aa7bbc753ebcd32da4507fa23215489b6d314
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Sep 20 11:20:31 2011 -0700

    Fix issue #5312624: Lock screen very flickery

    The key thing was to fix isVisibleOrBehindKeyguardLw() so that it
    wouldn't count a window as not visible if it was just currently
    in the process of drawing due to an orientation change.

    Also improve logic in deciding when to turn screen on to better ensure
    the screen is in a stable state, in particular treating screen off
    as a frozen screen and not allowing it to turn on until the
    update of the screen due to any config change is done.

    Change-Id: If82199f3773270b2d07f9c7de9da2dad8c7b28d7
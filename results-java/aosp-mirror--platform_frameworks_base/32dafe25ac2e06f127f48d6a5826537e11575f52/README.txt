commit 32dafe25ac2e06f127f48d6a5826537e11575f52
Author: Jeff Brown <jeffbrown@google.com>
Date:   Fri Oct 19 17:04:30 2012 -0700

    Reduce screen on/off latency.

    Reduce latency of screen on/off and improve how it is synchronized with
    backlight changes.  Screen state changes are no longer posted to vsync
    which should save time.  What's more, the state change occurs on a
    separate thread so we no longer run the risk of blocking the Looper
    for a long time while waiting for the screen to turn on or off.

    Bug: 7382919
    Bug: 7139924
    Change-Id: I375950d1b07e22fcb94efb82892fd817e2f780dc
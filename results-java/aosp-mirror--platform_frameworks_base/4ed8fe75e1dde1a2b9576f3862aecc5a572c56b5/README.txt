commit 4ed8fe75e1dde1a2b9576f3862aecc5a572c56b5
Author: Jeff Brown <jeffbrown@google.com>
Date:   Thu Aug 30 18:18:29 2012 -0700

    More improvements to the display manager.

    Added more complete support for logical displays with
    support for mirroring, rotation and scaling.

    Improved the overlay display adapter's touch interactions.

    A big change here is that the display manager no longer relies
    on a single-threaded model to maintain its synchronization
    invariants.  Unfortunately we had to change this so as to play
    nice with the fact that the window manager wants to own
    the surface flinger transaction around display and surface
    manipulations.  As a result, the display manager has to be able
    to update displays from the context of any thread.

    It would be nice to make this process more cooperative.
    There are already several components competing to perform
    surface flinger transactions including the window manager,
    display manager, electron beam, overlay display window,
    and mouse pointer.  They are not manipulating the same surfaces
    but they can collide with one another when they make global
    changes to the displays.

    Change-Id: I04f448594241f2004f6f3d1a81ccd12c566bf296
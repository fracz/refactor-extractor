commit bd6e1500aedc5461e832f69e76341bff0e55fa2b
Author: Jeff Brown <jeffbrown@google.com>
Date:   Tue Aug 28 03:27:37 2012 -0700

    Add initial multi-display support.

    Split the DisplayManager into two parts.  One part is bound
    to a Context and takes care of Display compatibility and
    caching Display objects on behalf of the Context.  The other
    part is global and takes care of communicating with the
    DisplayManagerService, handling callbacks, and caching
    DisplayInfo objects on behalf of the process.

    Implemented support for enumerating Displays and getting
    callbacks when displays are added, removed or changed.

    Elaborated the roles of DisplayManagerService, DisplayAdapter,
    and DisplayDevice.  We now support having multiple display
    adapters registered, each of which can register multiple display
    devices and configure them dynamically.

    Added an OverlayDisplayAdapter which is used to simulate
    secondary displays by means of overlay windows.  Different
    configurations of overlays can be selected using a new
    setting in the Developer Settings panel.  The overlays can
    be repositioned and resized by the user for convenience.

    At the moment, all displays are mirrors of display 0 and
    no display transformations are applied.  This will be improved
    in future patches.

    Refactored the way that the window manager creates its threads.
    The OverlayDisplayAdapter needs to be able to use hardware
    acceleration so it must share the same UI thread as the Keyguard
    and window manager policy.  We now handle this explicitly as
    part of starting up the system server.  This puts us in a
    better position to consider how we might want to share (or not
    share) Loopers among components.

    Overlay displays are disabled when in safe mode or in only-core
    mode to reduce the number of dependencies started in these modes.

    Change-Id: Ic2a661d5448dde01b095ab150697cb6791d69bb5
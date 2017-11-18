commit 4c1e3183baf39ab69c0289c1511877a8bb0b0f75
Author: Dianne Hackborn <hackbod@google.com>
Date:   Fri Oct 5 18:37:54 2012 -0700

    Fix issue #7296314, issue #7296314.

    7296314 Crashing dreams are stuck
    7296510 Transition from lock screen to dreaming is really bad

    The window layer for dreams is now moved down below the keyguard,
    so that some of the expected stuff like crash and ANR dialogs can
    be seen on top of them.  While doing this, I reorganized how we
    define the layers so the constants are just in the switch statement,
    so it is much less crazy-making trying to read how things go
    together.

    We now have some special cases for when a dream is being shown
    to turn off its animation if the keyguard is currently shown.
    Since we know it will be hiding the keyguard we need it to be
    shown immediately so that you don't see whatever is behind it.

    Cleaned up some handling of when the lock screen is displayed
    while a FLAG_SHOW_WHEN_LOCKED window is displayed, so that the
    lockscreen doesn't transiently get shown and mess up the fullscreen
    or system UI state.  This also fixes problems with any normal
    activity that is doing this.

    Hid the methods on DreamService for setting lights out mode.  It
    doesn't make sense to have such methods on DreamService, because
    you can just as well do that on your own View that is showing the
    dream content, and when you can do that you can fully participate
    in the (required) interactions about it such as being told when
    the mode goes away.

    The DreamService method for going fullscreen now uses the window
    flag for doing this, which is what you want, because you want this
    state to persistent on that window and not get knocked out if
    something above the window tickles the system UI state.

    Also fixed the problem where dreams that hid the status bar would
    have a jerky animation when going away, since they were causing the
    activity behind them to be layed out without the lock screen.  This
    is a kind-of ugly special case in the window manager right now to
    just not layout windows that are behind a dream.  Good enough for MR1.

    Change-Id: Ied2ab86ae068b1db0ff5973882f6d17b515edbcd
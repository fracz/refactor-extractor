commit e06b065f6e0d0f939b8519ccd3699b90aef818e9
Author: Jason Monk <jmonk@google.com>
Date:   Wed Mar 2 16:35:27 2016 -0500

    Fix QS edit state being out of sync

    Do this by making SignalCallbacks send out initial state immediately
    rather than posting this state.  This requires a little refactoring
    to how SignalControllers work.

    Bug: 27061469
    Change-Id: Iba6b91a4a5d1d13cce0f0d308b6f85f0340bff39
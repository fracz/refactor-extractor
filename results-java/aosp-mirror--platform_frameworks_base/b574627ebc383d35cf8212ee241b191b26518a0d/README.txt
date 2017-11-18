commit b574627ebc383d35cf8212ee241b191b26518a0d
Author: Jason Monk <jmonk@google.com>
Date:   Wed Nov 12 16:50:31 2014 -0500

    SysUI: Actual MSIM status bar support

    Expand SignalClusterView and NetworkController to handle multiple
    SIMs.  It does this by creating multiple MobileSignalControllers
    for each of the active subscriptions on the device.

    Also some minor changes for followup on the NetworkController
    refactor that went in before this.

    Bug: 18222975
    Change-Id: Ic7a857cfc5cadb46d51bb9ded0df8187eea799f7
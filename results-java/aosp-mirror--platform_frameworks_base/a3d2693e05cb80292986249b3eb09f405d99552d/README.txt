commit a3d2693e05cb80292986249b3eb09f405d99552d
Author: Andrei Stingaceanu <stg@google.com>
Date:   Fri Jul 15 11:12:20 2016 +0100

    Keyboard shortcuts: minor Activity broadcast refactor

    Since the broadcast intents have an action, specifying an explicit
    component name is not needed. Specified only the package name and
    left it for the system to resolve the full component name for the
    receiver that handles the action in that package. Also got rid of
    warning: "Calling a method in the system process without a
    qualified user" by correctly sending the broadcast as the SYSTEM
    user.

    Bug: 28012198
    Change-Id: Ia572fb1b7f2f3c96160d16e2842d6aff3b7f10a1
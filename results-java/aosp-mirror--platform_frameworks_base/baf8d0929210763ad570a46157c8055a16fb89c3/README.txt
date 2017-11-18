commit baf8d0929210763ad570a46157c8055a16fb89c3
Author: David Brown <dab@google.com>
Date:   Mon Mar 8 21:52:59 2010 -0800

    Accessibility: optionally allow Power key to end the current call.

    This is part 2 of the fix for bug 2364220 "Accessibility improvements for
    ending calls".

    This change updates the POWER key logic in interceptKeyTq() to check the
    value of Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR, which allows the
    user to specify that the Power button should hang up while in-call,
    instead of just turning off the screen.

    Bug: 2364220

    Change-Id: If6d8e3155f7d60142ab6fd61f0a9db7f0b0d95ab
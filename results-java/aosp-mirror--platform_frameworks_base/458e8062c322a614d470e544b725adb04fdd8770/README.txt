commit 458e8062c322a614d470e544b725adb04fdd8770
Author: David Brown <dab@google.com>
Date:   Mon Mar 8 21:52:11 2010 -0800

    Add INCALL_POWER_BUTTON_BEHAVIOR setting.

    This is part 1 of the fix for bug 2364220 "Accessibility improvements for
    ending calls".  This change adds a new (hidden) constant
    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR, for a setting that controls
    whether the Power button hangs up the current call, or just turns off the
    screen.

    Bug: 2364220

    Change-Id: I14d4a89b27e99cac51dc16826d217072d999b2d6
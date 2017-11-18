commit ce15e157a6cf56fc73961ccb8c5ab18b1bf70280
Author: Amith Yamasani <yamasani@google.com>
Date:   Thu Sep 19 12:30:32 2013 -0700

    Fix a regression in pausing activity immediately on sleep

    At some point during refactoring of ActivityStack, the code to pause the current
    activity got deleted. Added back that line of code. Activity will now pause
    as soon as the screen is turned off, rather than after 5 seconds (sleep timeout).

    Bug: 10632898

    Change-Id: If3cc8708d692d29a13dbd8cfd9edda8883b38c2e
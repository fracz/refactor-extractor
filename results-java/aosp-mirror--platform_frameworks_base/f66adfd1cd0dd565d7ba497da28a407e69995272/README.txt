commit f66adfd1cd0dd565d7ba497da28a407e69995272
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Apr 13 11:01:48 2017 -0700

    Add new facility to find out when a PendingIntent is canceled.

    This is just an internal API in the platform, not (yet?) available
    in the SDK.  But it will be useful for system services that want to
    clean up state if a pending intent that has been registered with them
    is canceled (either explicitly by the app, through the app being
    uninstalled, etc).

    Also improve the activity manager's dump of pending intents to
    organize them by package, making it much easier to read (now that
    we have so many active pending intents these days).

    Test: ran and booted.  no CTS, since no API.

    Change-Id: Iad029cfedcd77e87357eca7da1b6ae94451dd981
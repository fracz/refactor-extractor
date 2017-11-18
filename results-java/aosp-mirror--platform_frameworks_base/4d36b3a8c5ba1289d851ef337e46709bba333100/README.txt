commit 4d36b3a8c5ba1289d851ef337e46709bba333100
Author: Makoto Onuki <omakoto@google.com>
Date:   Wed Apr 27 12:00:17 2016 -0700

    ShortcutManager: finishing touches

    - Change back the throttling quota to 10 calls / day
    - Foreground apps are no longer throttled, and when an app comes to
    foreground the call counter will be reset.
    - When the system locale changes, reset throttling for all packages
    for all users.
      See LocalService.onSystemLocaleChangedNoLock() for how it's performed.
      Because the reset must happen before any other apps have a chance to
      publish shortcuts, the logic is not straightforward.

    - Added an internal API to reset the throttling upon inline-reply
    from a notification.

    - Stop supporting icons from "content:" URIs
    - Improved javadoc on several APIs.

    Also internal refactor needed to this:
    - ShortcutUser.getAllPackages()/getAllLaunchers() are no longer
    accessible to outer code to prevent accidentally adding/removing the
    content.  Outer code should use forAllPackages() / forAllLaunchers().

    Bug 27923857

    Change-Id: I002511193d1d33718163bb1dabe77610bde58198
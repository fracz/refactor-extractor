commit 1a4c4e35397adb964fb53695f66e1ac1ace39ff1
Author: Torne (Richard Coles) <torne@google.com>
Date:   Tue Jan 10 15:57:41 2017 +0000

    Handle multiprocess flag in WebView update service.

    Instead of letting DevelopmentSettings manage the setting directly and
    observing the changes from WebViewUpdateService, have the update service
    manage the setting and just expose IPCs for the settings app to use to
    get/set the setting. This means we can set a more flexible policy for
    whether multiprocess is enabled by default and change it without
    touching the settings code, though for now this CL does not change the
    behaviour and is just a refactoring.

    Bug: 21643067
    Test: Toggle multiprocess WebView in developer settings
    Change-Id: I3057c09d99f5f6f472a5195a8e14e9164ea5733a
commit 6bb41a42cbabb6fba57e28db7425868d0e33e5a0
Author: fionaxu <fionaxu@google.com>
Date:   Fri Mar 10 10:16:09 2017 -0800

    integrate portal webview to the default app

    Based on the UX review feedback, we plan to implement portal webview
    function inside the carrier default app instead of reusing the existing
    portal app. This will give us more flexibility and control, also will
    improve UX flow by getting rid of the some unwanted dialogues.

    new added CaptivePortalLoginActivity is a copy paste from
    com.android.captiveportallogin/CaptivePortalLoginActivity combined
    with logic from deleted LaunchCaptivePortalActivity.
    All webview UI was inherited from com.android.captiveportal

    Test: Manual
    Bug: 36002256
    Merged-in: I2627d5a43039ce433006c058bb4f2c1a39113e59
    Change-Id: If422fa12c5f24d9b9e2c9380b3edf94df74bb85f
commit f7eea406c950e447608c6f62de05158334682365
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Thu May 11 09:58:14 2017 +0900

    CaptivePortal: login activity UI improvements

    Similarly to commit 2e0915f14384901f25a41d698b39ef0add201550 for the
    carrier portal login, this patch changes the default settings of the
    webview used for the system captive portal login activity to allow
    better user experience on "wide" login pages designed for desktop.

    Differently from commit 2e0915f14384901f25a41d698b39ef0add201550, the
    zooming buttons are not displayed (i.e zooming is possible only with
    gesture).

    Test: manually tested with captive portals.
    Bug: 31813936, 19228946, 36532213
    Change-Id: I2579994da37f3b0f4c08e24e59c81f31835ab832
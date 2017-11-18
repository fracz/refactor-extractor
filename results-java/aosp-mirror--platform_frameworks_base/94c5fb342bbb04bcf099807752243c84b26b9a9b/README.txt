commit 94c5fb342bbb04bcf099807752243c84b26b9a9b
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Thu May 11 09:58:14 2017 +0900

    CaptivePortal: login activity UI improvements

    Similarly to commit 2e0915f14384901f25a41d698b39ef0add201550 for the
    carrier portal login, this patch changes the default settings of the
    webview used for the system captive portal login activity to allow
    better user experience on "wide" login pages designed for desktop.

    Test: manually tested with captive portals.
    Bug: 31813936, 19228946, 36532213
    Change-Id: Ib84fd351e47e951d24f297bc7de1b035b51cf24f
    Merged-In: I2579994da37f3b0f4c08e24e59c81f31835ab832

    (cherry picked from commit f7eea406c950e447608c6f62de05158334682365)
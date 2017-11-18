commit 04d78601ab0bbf7deee9ef97526fd9d45587aec1
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Wed Jul 19 21:20:53 2017 +0900

    CaptivePortalLoginActivity: improve logging

    - fix a type in onPageStarted
    - map ssl error codes to names in onReceivedSslError

    Bug: 62332137
    Test: build
    Change-Id: Ic789f54e06f539e47b60a67225b04b30cacded55
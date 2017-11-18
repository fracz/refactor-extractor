commit 9484603c0fa738b67980c18b4abfd3505778ae74
Author: Dianne Hackborn <hackbod@google.com>
Date:   Fri Mar 31 17:55:23 2017 -0700

    Framework support to improve bg check CTS tests.

    (Finally) introduce a new ServiceConnection callback to
    tell you when the binding has died.  This allows you to robustly
    have a weak service monitoring, and also is an easy way to find
    out about breakages due to app updates etc.

    Also clean up some debug output.

    Test: moved to own suite and ran them.

    Change-Id: I526cc00816c384fa9eb1312b92406f38085cbff9
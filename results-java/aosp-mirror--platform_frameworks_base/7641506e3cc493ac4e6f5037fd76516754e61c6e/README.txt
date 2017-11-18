commit 7641506e3cc493ac4e6f5037fd76516754e61c6e
Author: Jim Miller <jaggies@google.com>
Date:   Mon Sep 24 18:38:08 2012 -0700

    Fix wrong password attempt count in DevicePolicyManager

    Keyguard wasn't resetting dpm's count when a successful password
    was made.  The result is the device would get wiped earlier than
    it should.

    Also fixes a TODO left over from keyguard refactoring that
    allowed face unlock to trigger the same logic (ouch!).

    Fixes bug 7219258

    Change-Id: I2bd13c50a9beb8225d3237e86d5e34b73d0eb3cf
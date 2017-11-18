commit f13b4b39f266e43239e40e3bf7d6997600555068
Author: Jason Monk <jmonk@google.com>
Date:   Fri Nov 7 16:39:34 2014 -0500

    SysUI: Add basic test coverage for signal levels

    Add some tests that verify for varios wifi, and mobile signal
    strengths and types that the correct icons are sent out in the
    callbacks. Still in prep for MSIM refactoring.

    Bug: 18222975
    Change-Id: I477bf9a90e5c32fb1cba9c150ec6314f4b707108
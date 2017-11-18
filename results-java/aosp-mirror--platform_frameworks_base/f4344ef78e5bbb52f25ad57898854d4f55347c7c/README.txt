commit f4344ef78e5bbb52f25ad57898854d4f55347c7c
Author: Matthew Ng <ngmatthew@google.com>
Date:   Thu Apr 20 15:19:58 2017 -0700

    Dismiss docked stack when ShowWhenLocked activity is over keyguard (1/2)

    Activities with ShowWhenLocked flag when docked should dismiss the
    docked stack and appear after screen turns back on. This incorporates
    missing code from ag/1013634 that was missed during refactoring.

    Fixes: 36166079
    Test: run-test CtsServicesHostTestCases
    android.server.cts.KeyguardTests#testShowWhenLockedActivityWhileSplit
    Change-Id: I003cdd0be46fe6b9640e2bfbfca582150f2723b7
commit 7c1772110d1f024021699e1b3d88fb341d8dccaa
Author: Googler <noreply@google.com>
Date:   Fri Jun 16 15:46:55 2017 +0200

    Change TestTimeout's rangeMax values so that isInRangeFuzzy will flag tests that are potentially timeout flaky and getSuggestedTestTimeout is less likely to suggest timeouts that can result in timeout flakiness.

    Also modernized and refactored TestTimeout to be more understandable.

    RELNOTES: Adjust the thresholds for --test_verbose_timeout_warnings so that it can recommending timeout increases and won't recommend timeouts that are too close to the actual timeout.
    PiperOrigin-RevId: 159222380
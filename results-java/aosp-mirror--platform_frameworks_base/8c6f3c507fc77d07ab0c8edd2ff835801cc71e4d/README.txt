commit 8c6f3c507fc77d07ab0c8edd2ff835801cc71e4d
Author: Bookatz <bookatz@google.com>
Date:   Wed May 24 12:00:17 2017 -0700

    Fix batterystat Counter misreporting when charging

    The BatteryStatsImpl.Counter would previously increment the count even
    when the timeBase was off. Then, when the timeBase was turned back on,
    the count would be decreased back to the correct value. Thus, when the
    timeBase was on, the reported count would be correct, but when the
    timeBase was off, the reported count would be wrong (too high). Here, we
    fix this.
    We also make some other minor improvements.

    Bug: 36728346
    Test: runtest -x frameworks/base/core/tests/coretests/src/com/android/internal/os/BatteryStatsTests.java
    Change-Id: I2fa566a8a4cad4cdff0e6caef37b1eef36a3f5c4
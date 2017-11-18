commit d857f676744af55c79c4871c881bf9598f6b21e9
Author: Neil Fuller <nfuller@google.com>
Date:   Thu Jul 20 11:00:35 2017 +0100

    Logging improvements for time zone updates

    Logging improvements for time zone updates:
    1) Add EventLog entries time zone update service interactions.
    2) Add more information to dumpsys logs to improve debugging.

    Unit tests run with:

    make -j30 FrameworksServicesTests
    adb install -r -g \
      "out/target/product/angler/data/app/FrameworksServicesTests/FrameworksServicesTests.apk"
    adb shell am instrument -e package com.android.server.timezone -w \
      com.android.frameworks.servicestests \
      "com.android.frameworks.servicestests/android.support.test.runner.AndroidJUnitRunner"

    Bug: 31008728
    Test: See above for unit testing.
    Test: Internal xTS tests
    Test: adb shell dumpsys timezone
    Test: adb logcat -b events -v threadtime -v printable -v uid -d *:v
    Change-Id: I9356f4694e60b49e4b06aadd632d1bad517b8a29
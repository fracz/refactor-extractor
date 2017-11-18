commit dc6cf4b683c6b8af678dc7f50c069879013f9fa5
Author: Sundeep Ghuman <sghuman@google.com>
Date:   Wed Mar 8 16:18:29 2017 -0800

    Necessary AccessPoint visibility change for WifiNetworkDetailsFragment.

    Refactor constant name in WifiTracker for readability.

    Bug: 34713316
    Test: runtest --path
    frameworks/base/packages/SettingsLib/tests/integ/src/com/android/settingslib/wifi/AccessPointTest.java
    runtest --path
    frameworks/base/packages/SettingsLib/tests/integ/src/com/android/settingslib/wifi/WifiTrackerTest.java

    Change-Id: I41b0d69303d0452e2c2c22bcbddc34ae3932e99e
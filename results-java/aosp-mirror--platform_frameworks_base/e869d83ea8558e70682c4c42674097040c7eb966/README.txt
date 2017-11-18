commit e869d83ea8558e70682c4c42674097040c7eb966
Author: Sundeep Ghuman <sghuman@google.com>
Date:   Wed Jan 25 16:23:43 2017 -0800

    Create Settings Flags to Disable Scoring UI changes.

    When disabled, this will prevent badges from being shown in the status
    bar or wifi picker as well as prevent access points in the picker from
    being rearranged based on ranking scores.

    Fix missing permission dropped from previous CL to run
    NetworkControllerWifiTest and refactored tests to enable new setting.

    Bug: 34712533
    Test: runtest --path
    frameworks/base/packages/SettingsLib/tests/integ/src/com/android/settingslib/wifi/WifiTrackerTest.java
    and runtest --path
    frameworks/base/packages/SystemUI/tests/src/com/android/systemui/statusbar/policy/NetworkControllerWifiTest.java

    Change-Id: I79c97f2205ebb70c0f7f5b1f66f7207055e5769b
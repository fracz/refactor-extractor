commit 02896ab71376aa3d9386c5e5abc51c97b69b10c5
Author: Wale Ogunwale <ogunwale@google.com>
Date:   Wed Oct 5 09:08:43 2016 -0700

    Fixed issue with screen rotation not working for landscape<->seascape

    Problem as introduced in the refactor to change how configuration works
    in activity manager in ag/1460784. With the new change we don't call back
    into window manager to set new configuration if there are no changes.
    The new change is correct, however window manager was relying on this to
    unfreeze the display since the rotation changed.
    We now have activity manager return if it updated the configuration to
    window manager when the rotation changes do window manager can decide
    if it needs to perform additional actions like unfreezing the display.

    Bug: 31916697
    Test: CTS and Manual testing.
    CTS: cts-tradefed run commandAndExit cts-dev --module CtsServicesHostTestCases --test android.server.cts.ActivityManagerDockedStackTests
    Manual: Launch an app that suports all orientations and rotate it from
    landscape to seascape.
    Change-Id: I36ddeff1ccc9f6089227147b117a865571b8571e
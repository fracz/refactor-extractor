commit 858f98d13e10afba6420daba0a42b2e056452e24
Author: Amith Yamasani <yamasani@google.com>
Date:   Wed Feb 22 12:59:53 2017 -0800

    AssistStructure improvements

    Added isOpaque() for Views
    Added getAcquisitionStartTime(), ...EndTime()
    Added isHomeActivity()

    Bug: 30895450
    Test: cts-tradefed run cts-dev -m CtsAssistTestCases -t
    android.assist.cts.AssistStructureTest#testAssistStructure

    Change-Id: I294a089aa3454ebfebf0442036d98ccb86cb2827
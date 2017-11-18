commit 1dd872260b3ccfbe492d1be0bdbb3f98235b3ba3
Author: Phil Weaver <pweaver@google.com>
Date:   Tue Jan 26 17:15:15 2016 -0800

    Optionally support accessibility with UiAutomator

    Adding a flag to AccessibilityServiceInfo that only works
    for UIAutomator that supresses other services. This flag
    is set by default for UIAutomation to match the current
    behavior, but tests may clear the flag and enable other
    services.

    Needed to improve cts coverage of AccessibilityService.

    Bug: 26592034
    Change-Id: Icfc2833c1bd6546a22a169008d88a6b15e83989c
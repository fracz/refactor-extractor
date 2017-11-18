commit 545e47d7585b0a9627ee860d489d776096d5f4d7
Author: Rakesh Iyer <rni@google.com>
Date:   Mon Jan 23 18:10:21 2017 -0800

    Fix crashloop in CarStatusBar.

    System ui seems to be undergoing a refactor to bring in
    dependency injection, in the process, mBatteryController
    in CarStatusBar was not being set, causing a NPE.

    Since it looks like the work is still ongoing, this is
    just a spot fix for the crash, once the dependency injection
    migration is complete, we can reexamine the CarStatusBar
    implementation.

    Bug: 34633087
    Test: Verified that system booted correctly.
    Change-Id: Idd3e0286fd87196ae572349904d5bef744d54353
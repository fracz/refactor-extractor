commit 9abca5e9f88c47579f8334c6c48741a259185b9b
Author: Jason Monk <jmonk@google.com>
Date:   Fri Nov 11 16:18:14 2016 -0500

    Cleanup and refactoring of test utilities

     - Make leak checking faster by converting to fakes
        - Requires making clean interfaces for all CallbackControllers
     - Integrate leak checking into the TestableContext

    Test: runtest systemui
    Change-Id: Ic57a06360d01a0323ef26735a543e9d1805459e2
commit 74238e3b3e7a0ab7c8bf4fe5c7afa796bc62483b
Author: Roman Shevchenko <roman.shevchenko@jetbrains.com>
Date:   Thu Sep 21 19:32:22 2017 +0300

    [updater] improves patch application workflow and GUI (IDEA-178309)

    - shows separate notifications about prepare/apply/revert errors
    - pauses a process while cancel confirmation is shown
    - replaces custom event processing with EDT (required for the above)
    - changes exception propagation and handling to better cover possible errors
    - removes [impossible] cancellation checks from patch creation
    - returns exit code 0 only when a patch was fully applied
    - tests for missed cases
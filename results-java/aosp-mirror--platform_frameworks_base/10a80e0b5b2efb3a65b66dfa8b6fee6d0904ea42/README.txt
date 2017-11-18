commit 10a80e0b5b2efb3a65b66dfa8b6fee6d0904ea42
Author: Filip Gruszczynski <gruszczy@google.com>
Date:   Fri Nov 6 09:21:17 2015 -0800

    Fix windows disappearing when resizing freeform or docked.

    Also includes some code clarity improvements: mHasSurface is set using a
    setter, some fields get private.

    Change-Id: I2f834880493c008fdccf07ff6ebfebd2e26690a9
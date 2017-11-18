commit 4d656885ed9afec7d758c1862df6f040f5fe16a9
Author: Jeff Brown <jeffbrown@google.com>
Date:   Wed Apr 3 14:39:19 2013 -0700

    Clear mCurSender when mCurChannel is modified.

    This fixed an issue where an InputEventSender might outlive
    its usefulness and continue to be used well after it should
    have been disposed or recreated.

    Also improves the queue management somewhat.

    Bug: 8493879
    Change-Id: I7e0b6a3c43cbe72f8762991f5d36560feebd214b
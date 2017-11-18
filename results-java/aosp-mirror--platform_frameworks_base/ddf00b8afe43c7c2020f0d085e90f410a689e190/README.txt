commit ddf00b8afe43c7c2020f0d085e90f410a689e190
Author: Gilles Debunne <debunne@google.com>
Date:   Wed Feb 23 17:25:13 2011 -0800

    Text handles movement improvements.

    Bug 3329346

    Making sure the cursor is never hidden by the finger. Some
    vertical movement is not repercuted on the handles' position
    if it moves the finger closer to its 'ideal' touch position,
    where both the insertion line and the top of the handle are
    visible.

    Also removed the hysteresis line filter which is not that
    usefull and feels sluggy.

    Change-Id: I6ad0fed0cf66753c6571b3bc620b1a0f2397c7b2
commit 8e7be8e808b0e7f27e745985c4f3672437f3ee2f
Author: Robert Carr <racarr@google.com>
Date:   Tue Jan 31 13:20:31 2017 -0800

    Fix PiP animation errors introduced in crop refactoring.

    Firstly, pinned stack now uses final crop only, so we need to scale
    to the width/height computed there, not as computed for the crop.
    Furthermore mTmpClipRect is no longer used directly to set the crop,
    so clearing it in the pinned animation code had no effect. This fixes
    the worst of the visual errors so people stop filing bugs while I
    rework this logic entirely.

    Bug: 34823229
    Bug: 32881014
    Bug: 33245930
    Test: Manual with PiP activities on Fugu.
    Change-Id: I45d2def5138f3cc278408a9209b5e5c40cece80b
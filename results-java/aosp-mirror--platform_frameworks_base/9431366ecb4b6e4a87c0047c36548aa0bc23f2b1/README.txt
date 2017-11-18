commit 9431366ecb4b6e4a87c0047c36548aa0bc23f2b1
Author: Brian Colonna <bcolonna@google.com>
Date:   Fri Apr 6 13:01:29 2012 -0400

    Separated FUL functionality from LockPatternKeyguardView

    This is the first step toward fix 5460649.  All of the FUL functions
    were pulled out of LockPatternKeyguardView into their own FaceUnlock
    class.  LockPatternKeyguardView now has an mFaceUnlock member, which
    is new'd inside of the LockPatternKeyguardView constructor, passing
    it some objects it needs to do FUL.  FUL calls are now made from
    LockPatternKeyguardView by doing mFaceUnlock.foo().  Some of the
    function names were reduced to avoid redundancy (e.g.
    mFaceUnlock.start() instead of mFaceUnlock.startFaceLock()).

    This change is just a refactoring and is not intended to change
    any functionality.  There will be other cleanups in the near
    future...this is basically just the minimum changes needed to get
    the FUL functionality into its own class.

    Change-Id: I7dc5b22857bbf1659238b0e2d113069f7bf9ffe7
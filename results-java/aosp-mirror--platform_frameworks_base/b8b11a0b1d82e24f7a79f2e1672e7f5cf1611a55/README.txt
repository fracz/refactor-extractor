commit b8b11a0b1d82e24f7a79f2e1672e7f5cf1611a55
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Mar 10 15:53:11 2010 -0800

    Further improvements to window management!

    Fix issue #2493497: Stuck in the Emergency dialer - Home/Back keys doesn't work
    This was another case of not updating the window focus when needed, this time
    when the lock screen was hidden.

    Also re-arrange the layout/animate flow to address issues where you would see
    a flicker of whatever was behind the lock screen when showing a new activity that
    hides the lock screen.  This was because we were deciding to hide the lock screen
    during the layout phase, which meant we had to do it without considering whether
    it had drawn.  So we could hide the lock screen before the window is shown for the
    first time after being drawn.  Now we can do this in the policy during animate, so
    we can wait until the window is drawn and actually being shown.

    The flow in perform layout is thus significantly changed, where the layout and
    animate loops are both under the same repeating loop.  The actual flow from this
    should be the same, but it now allows the policy to request a new layout after
    the animation loop is done.  This actually cleans up a number of things in this
    code as the complexity has increased.

    Finally this includes a change to the ui mode manager when switching modes, to do
    the resource configuration switch at a different time.  This makes transitions
    between modes much cleaner (though not yet perfect).

    Change-Id: I5d9e75c1e79df1106108dd522f8ffed6058ef82b
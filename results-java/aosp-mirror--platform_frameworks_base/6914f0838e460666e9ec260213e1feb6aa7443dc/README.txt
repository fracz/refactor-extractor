commit 6914f0838e460666e9ec260213e1feb6aa7443dc
Author: Robert Carr <racarr@google.com>
Date:   Mon Mar 20 19:04:30 2017 -0700

    Fix various flashes when moving stacks.

    In this CL we fix two new pinned stack reparenting flashes and
    implement a new approach to an old docked stack flash fix, which had
    been broken in refactoring.

    First we examine the case of dismissing the docked stack and
    WindowState#notifyMovedInStack. Previously we invoked this
    when reparenting from the docked to the fullscreen stack
    (by way of position in stack). It was used to solve an issue
    where we were visually hidden by the docked stack crop, but we were
    still waiting on an animation pass to set the hidden flag. Our old solution
    was if mJustMovedInStack was set, we would just defer updating our crop until
    one animation pass had occurred.

    We broke this incidentally in refactoring by not calling the
    method that sets it anymore. However it's somewhat brittle so I was
    hesitant to restore it. The fundamental requirement is for the
    ActivityManager to perform multiple operations (change stack, update
    visibility) in a single atomic step and this wasn't expressed clearly.

    This mirrors some challenges we have with the pinned stack transitions
    as well.

    1. When dismissing the pinned stack, we move the task to the
    fullscreen stack. We need a mechanism to prevent its bounds from
    updating before its visibility is updated.
    2. When moving to fullscreen while over home, we have layering issues
    with the home stack, as we will be moved to the fullscreen stack before the
    fullscreen stack is brought to the front of the home stack. This may
    not seem like a visibility issue, but if the home activity were simply
    hidden the layering wouldn't matter!

    Evidently, all three of these issues can be solved with a batching
    mechanism from ActivityManager to WindowManager. As all changes are
    ultimately Surface changes, SurfaceControl.open/closeTransaction
    provides such a mechanism. The only additional complication is that
    normally visibility updates on SurfaceControl are deferred to the
    animation thread, which may not execute within the bounds of our
    transaction. This however, is easily dealt with: In AppWindowToken, if
    we are becoming hidden without animation, then we simply apply this
    change without waiting for the UI thread

    Bug: 35396882
    Bug: 34857388
    Bug: 36393204
    Bug: 36462635
    Test: Intensive manual testing of dismissing docked and pinned stack + pinned->fullscreen transition.
    Change-Id: Ic110797670cc7ff656a580fd186d4deb44fa54dd
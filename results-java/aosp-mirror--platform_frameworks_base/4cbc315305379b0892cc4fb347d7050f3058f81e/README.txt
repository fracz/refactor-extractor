commit 4cbc315305379b0892cc4fb347d7050f3058f81e
Author: Filip Gruszczynski <gruszczy@google.com>
Date:   Mon Dec 7 11:50:57 2015 -0800

    Fix thumbnail header animations in freeform to recents.

    The thumbnail header animations were constructed based on opening apps,
    in this case Recents. This is obviously wrong, but used to work because
    there was only one closing app in non-multi window world.

    For the animation to work correctly, i.e. each thumbnail have its own
    header animation, we need to correctly construct animations for either
    opening apps or closing apps (depending on the transition type we are
    seeing).

    The CL also refactors handleAppTransition into smaller methods.

    Bug: 24913782
    Change-Id: I9f30a53d2f90f0628bb8c6238b2d0f35592b8f63
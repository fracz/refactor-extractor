commit f34a04cca9b7f34eff533866ec11233777876ebf
Author: Filip Gruszczynski <gruszczy@google.com>
Date:   Mon Dec 7 15:05:49 2015 -0800

    Fix freeform to recents animating window being cuttoff.

    The window will appear cutoff during the animation if the window was
    cropped due to stack or decor bounds before the animation started. We
    need to disable the cropping (both from decor and from stack bounds)
    for the duration of the animation.

    Unfortunately, by disabling cropping of a freeform window to the stack
    bounds, we will make it appear above the docked window during the
    animation (because the animation will lift the layer). To fix this, we
    need to treat the docked stack like the pinned stack and assume it's
    always on top for the layering purposes.

    CL also includes refactoring of mSystemDecorRect and
    mLastSystemDecorRect which can be moved from WindowState to
    WindowStateAnimator and made private there.

    Bug: 24913782
    Change-Id: Idbea99ba04e9449d0563d0c02636f8b4b63087f7
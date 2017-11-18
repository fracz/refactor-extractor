commit ad3a49310634f9befe8e7b1a057691ece1685a59
Author: Robert Carr <racarr@google.com>
Date:   Tue Jun 20 14:55:21 2017 -0700

    SurfaceView: Fix positioning issue when toggling visibility.

    There is an issue (seemingly preexisting) with getPositionInWindow
    after toggling view visibility. We see it when showing a view,
    hiding it, and then showing it again. At this point we end up
    with this call-stack:
       SurfaceView#setVisibility->SurfaceView#updateSurface
       ->View#getPositionInWindow
    and getPositionInWindow fills in the wrong values. This newly discovered
    bug is tracked as 62839113.

    In a second bug, introduced in the SurfaceView refactoring,
    we are not appropriately clearing the last RenderThread reported
    position when toggling visibility. This means that even after the
    setVisibility call when getPositionInWindow begins returning
    the correct values, we don't update the position. This CL fixes
    that and fixes 62653411 as a result. However we still have a flicker
    as we did in N as the initial position is wrong.

    Test: Manual from bug, go/wm-smoke
    Change-Id: I1037b8dfdb343f9ce8c8616eb9197c6d039ed133
    Fixes: 62653411
    Bug: 62839113
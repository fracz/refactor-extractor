commit 24966d4789c7a2054650ec1a5ed7450f0d691224
Author: Filip Gruszczynski <gruszczy@google.com>
Date:   Sat Sep 5 15:00:00 2015 -0700

    Refactorings for Window Manager module.

    Following improvements were applied:
    * extract code from a very large method
    WindowSurfacePlacer.performSurfacePlacementInner into
    WindowsurfacePlacer.applySurfaceChangesTransaction; smaller methods
    are easier to understand;
    * WindowStateAnimator.showSurfaceRobustlyLocked can be privatized, it
    is only called from one place; also there is unnecessary check for
    whether mSurfaceControl is not null;
    * prepareAppTransition code can be mostly moved into AppTransition;
    it calls mostly methods from this class; as a result some methods
    from AppTransition can be privatized;
    * requestTraversalLocked can be moved into WindowSurfacePlacer, which
    allows mTraversalScheduled to be a private field inside the placer;
    this way WindowSurfacePlacer can nicely control and hide the need for
    layouts.

    Change-Id: I99006c859ef224f5dad933f5c15d2cb2b9738a9e
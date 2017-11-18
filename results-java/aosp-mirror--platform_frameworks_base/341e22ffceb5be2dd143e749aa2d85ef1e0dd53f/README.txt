commit 341e22ffceb5be2dd143e749aa2d85ef1e0dd53f
Author: Derek Sollenberger <djsollen@google.com>
Date:   Wed Jun 2 12:34:34 2010 -0400

    Refactor zoom logic from WebView to ZoomManager.

    This CL is one in a series of CL's intended to remove the zoom
    logic from WebView. This CL focuses on refactoring the initialScale
    variable as well as the onSizeChanged() and NEW_PICTURE_MSG portions
    of the code.

    Change-Id: Id44bce50378aa7cdfc1c8110818e18500679c517
    http://b/2671604
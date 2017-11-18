commit c35a829b10946fe88b53fd3354b55218ecaff00e
Author: Gilles Debunne <debunne@google.com>
Date:   Fri Dec 10 14:36:17 2010 -0800

    Modified OverScroller curves

    When OverFlinged, the scroller goes back to the edge using a spline
    instead of a parabolic curve. This ensures that the final velocity of
    this movement is null, thus removing the visual discontinuity that can
    be observed with the current version.

    Bouncing coefficient is deprecated. Changed doc accordingly.

    New more expressive spline tension tuning coefficients.
    These were tuned to match the one used before the refactoring of CL 81532.

    Change-Id: I80dbccebea11544595935077463ad7737c3392e9
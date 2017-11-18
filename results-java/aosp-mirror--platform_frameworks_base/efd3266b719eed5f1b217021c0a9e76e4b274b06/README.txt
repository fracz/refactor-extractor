commit efd3266b719eed5f1b217021c0a9e76e4b274b06
Author: Jeff Brown <jeffbrown@google.com>
Date:   Tue Mar 8 15:13:06 2011 -0800

    Input improvements and bug fixes.

    Associate each motion axis with the source from which it comes.
    It is possible for multiple sources of the same device to define
    the same axis.  This fixes new API that was introduced in MR1.
    (Bug: 4066146)

    Fixed a bug that might cause a segfault when using a trackball.

    Only fade out the mouse pointer when touching the touch screen,
    ignore other touch pads.

    Changed the plural "sources" to "source" in several places in
    the InputReader where we intend to refer to a particular source
    rather than to a combination of sources.

    Improved the batching code to support batching events from different
    sources of the same device in parallel.  (Bug: 3391564)

    Change-Id: I0189e18e464338f126f7bf94370b928e1b1695f2
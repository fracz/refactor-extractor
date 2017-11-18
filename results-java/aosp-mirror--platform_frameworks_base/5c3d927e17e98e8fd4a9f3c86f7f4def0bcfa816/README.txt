commit 5c3d927e17e98e8fd4a9f3c86f7f4def0bcfa816
Author: Florin Malita <fmalita@google.com>
Date:   Thu May 8 10:35:36 2014 -0400

    Add a native Canvas wrapper.

    Instead of storing a direct SkCanvas reference, Canvas now tracks
    an opaque native wrapper class. The native wrapper can be used to
    store additional info for emulating deprecated Skia features
    (at this point it only stores a canvas).

    Some notes:

    * all native handle -> SkCanvas conversions are routed through a
      handful of native utility methods.
    * safeCanvasSwap() refactored as a lower level setNativeBitmp() - which
      is what clients need.
    * removed unused get_thread_msec() (Canvas.cpp)

    Change-Id: I715a5a6f1e1621c1cfc1e510ae4f2ea15cf11114
commit f2a02018e2fa3089f6d39fc838a04818ae6cf26b
Author: Gilles Debunne <debunne@google.com>
Date:   Thu Oct 27 11:10:14 2011 -0700

    Scroll performance improved in multiline TextEdit

    Measuring line widths, glyph by glyph slows down the scrolling
    process for long text (for some reason, width measure efficiency
    is affectedi by text length, maybe because the whole text has to
    be passed to JNI layers).

    This optimization avoids this computation in the case where there
    is no possible horizontal scroll.

    Change-Id: I2082e3d0eedace1a86122a03e4b21f90f3bc8522
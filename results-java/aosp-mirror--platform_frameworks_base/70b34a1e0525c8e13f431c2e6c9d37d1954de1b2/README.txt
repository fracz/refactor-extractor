commit 70b34a1e0525c8e13f431c2e6c9d37d1954de1b2
Author: Gilles Debunne <debunne@google.com>
Date:   Thu Oct 27 11:10:14 2011 -0700

    Scroll performance improved in multiline TextEdit

    Measuring line widths, glyph by glyph slows down the scrolling
    process for long text (for some reason, width measure efficiency
    is affectedi by text length, maybe because the whole text has to
    be passed to JNI layers).

    This optimization avoids this computation in the case where there
    is no possible horizontal scroll.

    This is a cherry pick of 145957 into ICS-MR1

    Change-Id: I2082e3d0eedace1a86122a03e4b21f90f3bc8522
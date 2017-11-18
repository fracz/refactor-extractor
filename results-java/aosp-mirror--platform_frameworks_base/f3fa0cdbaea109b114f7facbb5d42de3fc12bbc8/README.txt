commit f3fa0cdbaea109b114f7facbb5d42de3fc12bbc8
Author: Gilles Debunne <debunne@google.com>
Date:   Thu Feb 3 14:17:05 2011 -0800

    Bugfixes in StaticLayout.

    Bug 3422121

    With ellipsize, lines starting with a very long word that does not
    fit inside the width were simply ignored. Cut the long word instead.

    start - widthStart index offset shift in BiDi.

    The original ellipsize-end patch that added '...' after the last
    word on end-ellipsized lines has been punted in favor of a true
    ellipsize support in I.

    I believe the StaticLayout calculateEllipsise is a no-op since textwidth <= avail
    by construction: fitWidth and okwidth are < outerWidth. The only exception is the
    paraEnd != here case in generate (when not a single character fits in width).
    This case is exercised by StaticLayoutTest in cts (width of 8 pixels) and revealed
    an offset error in widstart.

    All in all, it looks like this code was probably never really tested. I tried some
    typical text configuration to make sure these changes improved the situation.

    Change-Id: Ibee410bd7db453abf93e10e8beb844eae998922c
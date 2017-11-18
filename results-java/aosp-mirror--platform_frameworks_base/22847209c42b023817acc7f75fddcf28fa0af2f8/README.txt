commit 22847209c42b023817acc7f75fddcf28fa0af2f8
Author: Gilles Debunne <debunne@google.com>
Date:   Thu Feb 3 14:17:05 2011 -0800

    Bugfixes in StaticLayout. DO NOT MERGE.

    Bug 3422121

    Cherry-picked from master's 95472.

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

    Change-Id: I6c2cb26436a21f0f89078c275a89e891f0f23b92
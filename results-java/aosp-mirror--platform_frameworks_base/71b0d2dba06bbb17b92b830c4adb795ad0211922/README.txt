commit 71b0d2dba06bbb17b92b830c4adb795ad0211922
Author: Filip Gruszczynski <gruszczy@google.com>
Date:   Wed Aug 12 18:52:26 2015 -0700

    Thumbnail enter animations for multiwindow.

    The multi-window differs from the full screen entrance animation by
    using translation to make the window travel from the thumbnail to the
    final position. It also takes into account the surface insets when
    determining scaling animation. It doesn't use clipping at the moment,
    but that would be a near future improvement.

    Change-Id: I7f310850713448b820b9e94ac2f8fbf74563068c
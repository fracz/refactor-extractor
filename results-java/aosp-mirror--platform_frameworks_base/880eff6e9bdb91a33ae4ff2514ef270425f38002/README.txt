commit 880eff6e9bdb91a33ae4ff2514ef270425f38002
Author: Jaewan Kim <jaewan@google.com>
Date:   Wed Apr 27 10:36:35 2016 +0900

    Prevent pinned stack from having extra elevation

    Pinned stack doesn't have focus, so there's no need for setting extra
    elevation to show shadow for focused case.

    This removes extra eleavtion for pinned stack, and improves the PIP
    animation quality by preventing extra surface size change
    at the end of animation.

    Bug: 27364161
    Change-Id: Id099a78de48b2e038a69600c94454b5cbfe0628f
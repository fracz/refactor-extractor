commit 1f407647d1ac45dc8b8bb7f388c8bae38cd82ea3
Author: Tim Murray <timmurray@google.com>
Date:   Thu Oct 1 17:07:12 2015 -0700

    Send POWER_HINT_INTERACTION on rotate.

    Send a POWER_HINT_INTERACTION to improve redraw performance when the
    phone is rotated.

    bug 24583227

    Change-Id: I1978f0dfb9a25c00ad4da5b44d10410ad7412001
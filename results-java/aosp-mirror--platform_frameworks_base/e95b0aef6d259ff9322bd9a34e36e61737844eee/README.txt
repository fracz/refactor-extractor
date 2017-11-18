commit e95b0aef6d259ff9322bd9a34e36e61737844eee
Author: Filip Gruszczynski <gruszczy@google.com>
Date:   Wed Sep 30 10:55:33 2015 -0700

    Improve visibility and layering of dock divider.

    We adjust the visiblity of the divider now after every layout.

    The divider was far too high in the priority list, based on the wrong
    assumption that as a part of the system UI it needs to be constantly
    visible. It should stay at the same level as applications, because it's
    almost as a part of application.

    Layering gets improved by having the relaunch animation receive zorder
    top, just as if it was entering. The window that is being replaced fakes
    this too, since it's not being animated, but should share the behavior.

    Bug: 24500829

    Change-Id: Iad3369a5ab6721b1bf7a94e8979dcf33e0805c7f
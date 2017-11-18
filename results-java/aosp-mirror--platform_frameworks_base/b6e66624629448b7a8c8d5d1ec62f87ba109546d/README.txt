commit b6e66624629448b7a8c8d5d1ec62f87ba109546d
Author: Filip Gruszczynski <gruszczy@google.com>
Date:   Sun Oct 25 16:05:27 2015 -0700

    Improve infrastructure for replacing windows.

    We need to be more precise when removing the window that is being
    replaced. We used to depend on the fact that we can remove it after
    the first draw of the new added window. However, due to resizing the
    old window might reset its draw state and that will trigger a removal
    of that window.

    We need to add an information about the window that is replacing the
    old one and only when this new window draws itself, we remove the old
    one.

    This improves the transition after maximizing docked window. This is
    a situation where first resize operation finishes and immediately
    after we have a replacement operation.

    Bug: 24914011
    Change-Id: Ia8e5bb7872787e663ba23f26588f9c4db1a5e574
commit 9a04856d5ecb07dea564feae2942fd485b53f3dd
Author: Fabrice Di Meglio <fdimeglio@google.com>
Date:   Wed Sep 26 14:55:56 2012 -0700

    Other improvements for bug #6427629 Clean up layout direction APIs

    - hide isLayoutRtl() from public API

    - canResolveXXX() is now smarter: use recursion to get its returned value

    - in ViewGroup, if resolution cannot be done then dont ask resolution for
    its children

    - in ViewGroup, addViewInner() needs to ask to resolve the child. This is
    needed for example by ListView which is using the same measurespec before
    and after its childs being attached.

    It also take care of the general case where a measure pass is done when not
    attached to a parent (and thus asking for resolution that will "fail" if we
    are using IHNERIT) and never done again. That would lead to never do a
    resolution.

    - some code refactoring

    Change-Id: I120dd2fef7397944f5ba8deff0686b108dc827d2
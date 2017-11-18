commit f52dd205b9d31e0edcfdfff4ed98259c07ca38b7
Author: Filip Gruszczynski <gruszczy@google.com>
Date:   Sun Nov 15 20:36:38 2015 -0800

    Don't animate move triggered by resizing using dock divider.

    Also includes some small, nice refactoring:
    * move code that sets the move animation into WindowStateAnimator;
    * a few fields can be made private in WindowStateAnimator this way;
    * one boolean flag in WindowStateAnimator popped out as unused after
    being privatized, so could be deleted.

    Bug: 25690109
    Change-Id: I8144114244892c4f27aff21455e8e76eddbd039f
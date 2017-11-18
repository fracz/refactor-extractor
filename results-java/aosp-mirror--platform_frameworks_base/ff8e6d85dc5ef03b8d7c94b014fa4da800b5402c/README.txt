commit ff8e6d85dc5ef03b8d7c94b014fa4da800b5402c
Author: George Mount <mount@google.com>
Date:   Thu Aug 28 16:58:01 2014 -0700

    Use offsetLeftAndRight and offsetTopAndBottom in ChangeBounds.

    Bug 17015836

    When the size does not change, use offsetLeftAndRight and
    offsetTopAndBottom to improve performance.

    Change-Id: I3e70c783321346bb98867ec60bd899c39293c9e7
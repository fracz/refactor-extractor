commit 88990da58d0231b5a1b9a27b32c37839e95c7487
Author: Stephen Hines <srhines@google.com>
Date:   Mon Sep 9 17:56:07 2013 -0700

    Don't set Allocation mSize when we have no Type.

    Bug: 10667740

    A3D creates Allocations before knowing the underlying Type (and thus size).
    Moving the mSize calculation to be conditional for concrete Allocations
    eliminates the bug. In the future, we could let A3D dynamically update the
    Allocation size if it is necessary to improve heap behavior.

    Change-Id: I520246806b6ead0387a1a41372dade1a6e7c2271
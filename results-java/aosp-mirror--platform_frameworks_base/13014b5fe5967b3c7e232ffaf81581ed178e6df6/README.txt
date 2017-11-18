commit 13014b5fe5967b3c7e232ffaf81581ed178e6df6
Author: Jeff Brown <jeffbrown@google.com>
Date:   Mon Apr 7 19:45:27 2014 -0700

    Move certain internal activity manager methods to new class.

    This is a little bit of refactoring in preparation for changing how
    the power manager notifies system components about changes in power
    state.

    Deleted the startRunning method since it is no longer useful.

    Bug: 13133142
    Change-Id: I7f845c61ecc7ee890154ed0cbd90795de609b7ea
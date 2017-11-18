commit 1eee1996f5d57b42d4beed2790321480fbe03a4c
Author: Felipe Leme <felipeal@google.com>
Date:   Tue Feb 16 13:01:38 2016 -0800

    Cancel notifications when user tap on Details or Take Screenshot after
    service died.

    There are scenarios when the user is running low on resources and it
    kills Shell after it start monitoring a dumpstate process, in which case
    the BugreportInfo is not available anymore when the user tap a
    notification action.

    We could add a mechanism to recover that info (like persistenting the
    user-provided values in a shared preference), but would incur in more
    costs when the device is already in a resource-constrained state, so
    it's better to just stop monitoring and switch back to the traditional
    model where the user is notified after the bugreport finishes (the
    drawback is that all user-provided information will be lost).

    Also improved how info.name is checked to avoid crash in similar cases.

    BUG: 27186542
    BUG: 27203559
    Change-Id: I57076b098a3fce493e1a27121b6e070366808668
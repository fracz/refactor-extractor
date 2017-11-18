commit 42bf39edbdad19f51497938d0a3469dd772f19e8
Author: Craig Mautner <cmautner@google.com>
Date:   Fri Feb 21 16:46:22 2014 -0800

    Reset deferred task removal when app token added.

    A task is scheduled for deletion after the final activity has
    been removed and has animated away. But if another activity is then
    added to the task the deletion flag must be reset.

    Also added improved debugging.

    Fixes bug 12987986.

    Change-Id: I207ea6e9592a9e036d67aa5d1465b4acc5bdd120
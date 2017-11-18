commit 46a6f45626f2c6dba077f8994fd6911c2f50a535
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Sep 11 09:51:14 2013 -0700

    Fix issue #10688644: Java crash in com.android.phone:

    java.lang.SecurityException: Operation not allowed

    There was a situation I wasn't taking into account -- components
    declared by the system has a special ability to run in the processes
    of other uids.  This means that if that code loaded into another
    process tries to do anything needing an app op verification, it will
    fail, because it will say it is calling as the system package name but
    it is not actually coming from the system uid.

    To fix this, we add a new Context.getOpPackageName() to go along-side
    getBasePackageName().  This is a special call for use by all app ops
    verification, which will be initialized with either the base package
    name, the actual package name, or now the default package name of the
    process if we are creating a context for system code being loaded into
    a non-system process.

    I had to update all of the code doing app ops checks to switch to this
    method to get the calling package name.

    Also improve the security exception throw to have a more descriptive
    error message.

    Change-Id: Ic04f77b3938585b02fccabbc12d2f0dc62b9ef25
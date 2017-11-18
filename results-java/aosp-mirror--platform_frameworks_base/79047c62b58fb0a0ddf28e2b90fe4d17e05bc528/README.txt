commit 79047c62b58fb0a0ddf28e2b90fe4d17e05bc528
Author: Christopher Tate <ctate@google.com>
Date:   Tue Mar 21 11:37:06 2017 -0700

    API refactor: context.startForegroundService()

    Rather than require an a-priori Notification be supplied in order to
    start a service directly into the foreground state, we adopt a two-stage
    compound operation for undertaking ongoing service work even from a
    background execution state.  Context#startForegroundService() is not
    subject to background restrictions, with the requirement that the
    service formally enter the foreground state via startForeground() within
    5 seconds.  If the service does not do so, it is stopped by the OS and
    the app is blamed with a service ANR.

    We also introduce a new flavor of PendingIntent that starts a service
    into this two-stage "promises to call startForeground()" sequence, so
    that deferred and second-party launches can take advantage of it.

    Bug 36130212
    Test: CTS

    Change-Id: I96d6b23fcfc27d8fa606827b7d48a093611b2345
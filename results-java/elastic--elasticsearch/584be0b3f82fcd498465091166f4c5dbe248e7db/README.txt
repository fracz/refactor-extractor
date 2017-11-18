commit 584be0b3f82fcd498465091166f4c5dbe248e7db
Author: Jason Tedor <jason@tedor.me>
Date:   Tue May 17 13:05:36 2016 -0400

    Refactor JvmGcMonitorService for testing

    This commit refactors the JvmGcMonitorService so that it can be
    tested. In particular, hooks are added to verify that the
    JvmMonitorService correctly observes slow GC events, and that the
    JvmGcMonitorService logs the correct messages.

    Relates #18378
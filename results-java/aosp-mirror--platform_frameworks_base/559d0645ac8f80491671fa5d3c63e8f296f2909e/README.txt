commit 559d0645ac8f80491671fa5d3c63e8f296f2909e
Author: Jeff Brown <jeffbrown@google.com>
Date:   Wed Feb 29 10:19:12 2012 -0800

    Refactor SQLiteOpenHelper.

    Combine the code for opening readable and writable databases.
    This improves the handling of the case where a database cannot
    be opened because it cannot be upgraded.  Previously we would
    open the database twice: first read-write, then read-only, each
    time failing due to the version check.  Now only open it once.

    Removed the goofy locking logic related to upgrading a read-only
    database to read-write.  We now do it in place by reopening the
    necessary connections in the connection pool.

    Change-Id: I6deca3fb90e43f4ccb944d4715307fd6fc3e1383
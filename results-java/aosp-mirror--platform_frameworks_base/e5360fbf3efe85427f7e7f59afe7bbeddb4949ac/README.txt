commit e5360fbf3efe85427f7e7f59afe7bbeddb4949ac
Author: Jeff Brown <jeffbrown@google.com>
Date:   Mon Oct 31 17:48:13 2011 -0700

    Rewrite SQLite database wrappers.

    The main theme of this change is encapsulation.  This change
    preserves all existing functionality but the implementation
    is now much cleaner.

    Instead of a "database lock", access to the database is treated
    as a resource acquisition problem.  If a thread's owns a database
    connection, then it can access the database; otherwise, it must
    acquire a database connection first, and potentially wait for other
    threads to give up theirs.  The SQLiteConnectionPool encapsulates
    the details of how connections are created, configured, acquired,
    released and disposed.

    One new feature is that SQLiteConnectionPool can make scheduling
    decisions about which thread should next acquire a database
    connection when there is contention among threads.  The factors
    considered include wait queue ordering (fairness among peers),
    whether the connection is needed for an interactive operation
    (unfairness on behalf of the UI), and whether the primary connection
    is needed or if any old connection will do.  Thus one goal of the
    new SQLiteConnectionPool is to improve the utilization of
    database connections.

    To emulate some quirks of the old "database lock," we introduce
    the concept of the primary database connection.  The primary
    database connection is the one that is typically used to perform
    write operations to the database.  When a thread holds the primary
    database connection, it effectively prevents other threads from
    modifying the database (although they can still read).  What's
    more, those threads will block when they try to acquire the primary
    connection, which provides the same kind of mutual exclusion
    features that the old "database lock" had.  (In truth, we
    probably don't need to be requiring use of the primary database
    connection in as many places as we do now, but we can seek to refine
    that behavior in future patches.)

    Another significant change is that native sqlite3_stmt objects
    (prepared statements) are fully encapsulated by the SQLiteConnection
    object that owns them.  This ensures that the connection can
    finalize (destroy) all extant statements that belong to a database
    connection when the connection is closed.  (In the original code,
    this was very complicated because the sqlite3_stmt objects were
    managed by SQLiteCompiledSql objects which had different lifetime
    from the original SQLiteDatabase that created them.  Worse, the
    SQLiteCompiledSql finalizer method couldn't actually destroy the
    sqlite3_stmt objects because it ran on the finalizer thread and
    therefore could not guarantee that it could acquire the database
    lock in order to do the work.  This resulted in some rather
    tortured logic involving a list of pending finalizable statements
    and a high change of deadlocks or leaks.)

    Because sqlite3_stmt objects never escape the confines of the
    SQLiteConnection that owns them, we can also greatly simplify
    the design of the SQLiteProgram, SQLiteQuery and SQLiteStatement
    objects.  They no longer have to wrangle a native sqlite3_stmt
    object pointer and manage its lifecycle.  So now all they do
    is hold bind arguments and provide a fancy API.

    All of the JNI glue related to managing database connections
    and performing transactions is now bound to SQLiteConnection
    (rather than being scattered everywhere).  This makes sense because
    SQLiteConnection owns the native sqlite3 object, so it is the
    only class in the system that can interact with the native
    SQLite database directly.  Encapsulation for the win.

    One particularly tricky part of this change is managing the
    ownership of SQLiteConnection objects.  At any given time,
    a SQLiteConnection is either owned by a SQLiteConnectionPool
    or by a SQLiteSession.  SQLiteConnections should never be leaked,
    but we handle that case too (and yell about it with CloseGuard).

    A SQLiteSession object is responsible for acquiring and releasing
    a SQLiteConnection object on behalf of a single thread as needed.
    For example, the session acquires a connection when a transaction
    begins and releases it when finished.  If the session cannot
    acquire a connection immediately, then the requested operation
    blocks until a connection becomes available.

    SQLiteSessions are thread-local.  A SQLiteDatabase assigns a
    distinct session to each thread that performs database operations.
    This is very very important.  First, it prevents two threads
    from trying to use the same SQLiteConnection at the same time
    (because two threads can't share the same session).
    Second, it prevents a single thread from trying to acquire two
    SQLiteConnections simultaneously from the same database (because
    a single thread can't have two sessions for the same database which,
    in addition to being greedy, could result in a deadlock).

    There is strict layering between the various database objects,
    objects at lower layers are not aware of objects at higher layers.
    Moreover, objects at higher layers generally own objects at lower
    layers and are responsible for ensuring they are properly disposed
    when no longer needed (good for the environment).

    API layer: SQLiteDatabase, SQLiteProgram, SQLiteQuery, SQLiteStatement.
    Session layer: SQLiteSession.
    Connection layer: SQLiteConnectionPool, SQLiteConnection.
    Native layer: JNI glue.

    By avoiding cyclic dependencies between layers, we make the
    architecture much more intelligible, maintainable and robust.

    Finally, this change adds a great deal of new debugging information.
    It is now possible to view a list of the most recent database
    operations including how long they took to run using
    "adb shell dumpsys dbinfo".  (Because most of the interesting
    work happens in SQLiteConnection, it is easy to add debugging
    instrumentation to track all database operations in one place.)

    Change-Id: Iffb4ce72d8bcf20b4e087d911da6aa84d2f15297
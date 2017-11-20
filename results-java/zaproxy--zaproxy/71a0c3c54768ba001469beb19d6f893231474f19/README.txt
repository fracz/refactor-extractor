commit 71a0c3c54768ba001469beb19d6f893231474f19
Author: thc202 <thc202@gmail.com>
Date:   Tue May 24 23:36:46 2016 +0100

    Do not discard the session for file based database

    Ignore calls to discard the session if the database implementation is
    file based (for example, HSQLDB), the files of those databases are
    deleted when a session is discarded (that is, not to be saved), just
    before creating a new one.
    For file based databases the change improves the time it takes to create
    new sessions when the previous session was not (effectively) saved.

    More detailed changes:
     - Session, change to delegate the discard of the session to Database
     (which known the "type" of database it is);
     - Database, add method discardSession(long);
     - ParosDatabase, add implementation of discardSession(long), which
     (being HSQLDB) does not need to discard the session;
     - SqlDatabase, add implementation of discardSession(long), which only
     discards the session if it's not file based, for example, MySQL.
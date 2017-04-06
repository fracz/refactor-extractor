commit 8c71454f47bbcdf82693a0501acac3e8fe6e08cf
Merge: 735e9a4 50ec828
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat May 17 18:24:28 2014 +0200

    bug #10908 [HttpFoundation] implement session locking for PDO (Tobion)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [HttpFoundation] implement session locking for PDO

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #4976 for PDO
    | License       | MIT

    This is probably the first Session Handler for databases that actually works with locking. I've seen many implementations of session handlers (mostly only for one database vendor) while researching and none used locking. Not even the [PHPs SQLite session handler](https://github.com/php/php-src/blob/PHP-5.3/ext/sqlite/sess_sqlite.c) or [PECL Postgres Handler](http://svn.php.net/viewvc/pecl/session_pgsql/trunk/session_pgsql.c?revision=326806&view=markup) implemented locking  correctly which is probably the reason why they have been discontinued. [Zend Session](https://github.com/zendframework/zf2/blob/master/library/Zend/Session/SaveHandler/DbTableGateway.php) seems not to use locking either. But it saves the lifetime together with the session which seems like a good idea because you could have different lifetimes for different sessions.

    - Implements session locking for MySQL, Postgres, Oracle, SQL Server and SQLite.
    Only tested it for MySQL. So would be good if someone can confirm it works as intended on the other databases as well.
    - Also removed the custom RuntimeException which is not useful and a PDOException extends RuntimeException anyway, so no BC break.
    - I added a default for the table name to be in line with the DoctrineSessionHandler.
    - Check session.gc_maxlifetime in read(). Imagine we have only ever one user on an app. If maxlifetime is not checked in read, his session would never expire! What I don't get is why PHP calls gc() after read() instead of calling it before... Strange decision. For this reason I also had to do the following to improve performance.
    - I delay gc() to close() so that it is executed outside the transactional and blocking read-write process. This way, pruning expired sessions does not block them from being started while the current session is used.
    - Fixed time update for Oracle and SQL Server.

    Commits
    -------

    50ec828 [HttpFoundation] implement session locking for PDO
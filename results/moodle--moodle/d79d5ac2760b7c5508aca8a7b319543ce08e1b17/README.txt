commit d79d5ac2760b7c5508aca8a7b319543ce08e1b17
Author: Petr Škoda <commits@skodak.org>
Date:   Sun Sep 8 08:38:52 2013 +0200

    MDL-31501 rework user session architecture

    List of changes:
     * New OOP API using PHP namespace \core\session\.
     * All handlers now update the sessions table consistently.
     * Experimental DB session support in Oracle.
     * Full support for session file handler (filesystem locking required).
     * New option for alternative session directory.
     * Official memcached session handler support.
     * Workaround for memcached version with non-functional gc.
     * Improved security - forced session id regeneration.
     * Improved compatibility with recent PHP releases.
     * Fixed borked CSS during install in debug mode.
     * Switched to file based sessions in new installs.
     * DB session setting disappears if DB does not support sessions.
     * DB session setting disappears if session handler specified in config.php.
     * Fast purging of sessions used in request only.
     * No legacy distinction -  file, database and memcached support the same functionality.
     * Session handler name included in performance info.
     * Fixed user_loggedin and user_loggedout event triggering.
     * Other minor bugfixing and improvements.
     * Fixed database session segfault if MUC disposed before $DB.

    Limitations:
     * Session access time is now updated right after session start.
     * Support for $CFG->sessionlockloggedinonly was removed.
     * First request does not update userid in sessions table.
     * The timeouts may break badly if server hosting forces PHP.ini session settings.
     * The session GC is a lot slower, we do not rely on external session timeouts.
     * There cannot be any hooks triggered at the session write time.
     * File and memcached handlers do not support session lock acquire timeouts.
     * Some low level PHP session functions can not be used directly in Moodle code.
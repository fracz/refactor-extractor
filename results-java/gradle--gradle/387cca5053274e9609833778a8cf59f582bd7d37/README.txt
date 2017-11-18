commit 387cca5053274e9609833778a8cf59f582bd7d37
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sun Jul 28 13:27:25 2013 +0200

    The old dependency graph is now flushed to disk completely.

    1. The implementation currently creates a file per configuration - we probably want to change it so that we have less files (e.g. file per thread or per process).
    2. The cleanup of those files only happens when build exits 'naturally' (e.g. not ctrl+c, etc.). Do we want to improve it?
    3. The files are created in the system's temp, it feels cleaner if we do it in the project/.gradle dir (and add integ test coverage for the cleanup of those files).
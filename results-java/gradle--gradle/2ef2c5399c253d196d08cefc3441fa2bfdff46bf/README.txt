commit 2ef2c5399c253d196d08cefc3441fa2bfdff46bf
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Jun 19 19:10:25 2013 +0200

    REVIEW-2455 Attempted to unify the implementation of FileLockContentionHandlers.

    1. The attempt has failed. Down the road we want to have a single FileLockManager per process. Getting to that point is not trivial however. The default file lock manager opens a datagram port before creating a lock and requires the clients of the locks to provide reasonable callbacks when lock contention happens. At the moment, we use the default file lock manager in few places where the lock contention handling is absent and adding it is tricky (I'm open to suggestions). Hence perhaps we allow the no-op implementation of lock contention handler in those places for the time being until we have a better reason to push harder for a single file lock manager per process.

    2. Rename job and refactoring. Added dedicated method for enabling file lock contention handling on the file lock manager.
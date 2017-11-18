commit a5ee2121451ea269f70a6e38e31688fe8675fb90
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu Jul 11 19:07:16 2013 -0400

    Locking refactoring and javadoc cleanup

    * Moved AbstractLocker & AbstractLockStatus up from
      locking.consistentkey to just locking

    * Added and rewrote javadoc on Locker.  It's now up-to-date with the
      current inerface semantics

    * Updated ConsistentKeyLock* javadocs

    * Deleted unused classes made obsolete by locking refactoring
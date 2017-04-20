commit 0e9e64c550eb867d720c8a8ec05c5cecea913f93
Author: CommitSyncScript <jeffmo@fb.com>
Date:   Fri Jun 7 22:04:14 2013 -0700

    Replace `persistentCloneOf` with `persist`

    There are to reasons to prefer a `persist` method on the event rather than a static method:

     - In open source, people do not have access to `AbstractEvent`.
     - This will allow people to persist events without requiring another module.
     - This will make refactors easier and more flexible.
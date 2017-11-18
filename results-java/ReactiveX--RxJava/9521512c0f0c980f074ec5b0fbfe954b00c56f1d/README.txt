commit 9521512c0f0c980f074ec5b0fbfe954b00c56f1d
Author: David Karnok <akarnokd@gmail.com>
Date:   Wed Nov 15 11:24:45 2017 +0100

    2.x: distinguish between sync and async dispose in ScheduledRunnable (#5715)

    * 2.x: distinguish between sync and async dispose in ScheduledRunnable

    * Coverage improvement and fix a missing state change

    * Add test case for the parent-done reordered check
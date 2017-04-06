commit a72c167be2d5da22fca75fc1ef685630d58dbd0a
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Sun Jun 29 08:59:24 2014 +0200

    [Test] improved recovery slow down in rerouteRecoveryTest

    only change recovery throttling to slow down recoveries. The recovery file chunk size updates are not picked up by ongoing recoveries. That cause the recovery to take too long even after the default settings are restored.

    Also - change document creation to reuse field names in order to speed up the test.
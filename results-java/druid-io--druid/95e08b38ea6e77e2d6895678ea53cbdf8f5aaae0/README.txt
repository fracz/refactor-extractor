commit 95e08b38ea6e77e2d6895678ea53cbdf8f5aaae0
Author: Charles Allen <charles@allen-net.com>
Date:   Fri Sep 16 11:54:23 2016 -0700

    [QTL] Reduced Locking Lookups (#3071)

    * Lockless lookups

    * Fix compile problem

    * Make stack trace throw instead

    * Remove non-germane change

    * * Add better naming to cache keys. Makes logging nicer
    * Fix #3459

    * Move start/stop lock to non-interruptable for readability purposes
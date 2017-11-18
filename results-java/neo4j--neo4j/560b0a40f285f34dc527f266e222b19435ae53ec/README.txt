commit 560b0a40f285f34dc527f266e222b19435ae53ec
Author: Tobias Lindaaker <tobias@thobe.org>
Date:   Tue Jul 7 13:56:08 2015 +0200

    Improve throughput by reducing synchronized scope

    Forcing the log channel does not need to be done under synchronization
    since one thread forcing the changes of another is perfectly fine. In
    fact, allowing different threads to force the changes of one another
    improves throughput by (as measured with 8 threads) 3.5x.
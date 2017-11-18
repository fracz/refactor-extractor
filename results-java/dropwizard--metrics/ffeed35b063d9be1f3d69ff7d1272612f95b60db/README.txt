commit ffeed35b063d9be1f3d69ff7d1272612f95b60db
Author: Coda Hale <coda.hale@gmail.com>
Date:   Sun Apr 7 10:10:21 2013 -0700

    Swap out some AtomicLongs for LongAdders.

    This allows us to trade a bit of memory for a huge improvement in the performance of contended counters and meters. Striped64 and LongAdder are from the JSR166e project led by Doug Lea and are in the public domain. They're intended to be included in JDK8, so Metrics should remove these copied implementations once it moves to JDK8.
commit 39ccf43b5651c1484b5237666ae3b1d637aed993
Author: Chris Vest <mr.chrisvest@gmail.com>
Date:   Fri Apr 10 09:28:14 2015 +0200

    Make PageCache.flushAndForce do sequential IO

    This improves the performance of the page cache flushing, which is important for speeding up the time critical log rotations.
commit 49785e16b53f1e06aba8a93ee3e83a3d4228cedd
Author: Adam Murdoch <adam.murdoch@gradleware.com>
Date:   Wed Dec 26 17:59:50 2012 +1100

    - Added some additional performance test builds with lots of tests that generate no logging output.
    - Run performances tests against 1.0 and latest, at minimum. Some tests also include 1.2.
    - Tweaked performance test thresholds, to lock in performance improvements and to avoid false negatives for heap consumption.
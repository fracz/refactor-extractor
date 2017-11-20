commit c25d137fe28fcff7c58890fc007304ad3d7bf965
Author: Ben Manes <ben.manes@gmail.com>
Date:   Sun Jun 14 22:30:13 2015 -0700

    BoundedLocalCache#computeIfAbsent with CacheWriter

    Previously the prescreening was doing all of the validation and cleaning
    up if an entry needed to be evicted (expired, GC'd). This really should
    have been done within the computeIfAbsent, but it had to be refactored
    to use a map.compute() handle present but invalid nodes. This is now done
    to provide correctness and straightforward CacheWriter integration
commit f1951b253ce64495b840d488375c0429b8493c21
Author: Xavier Léauté <xavier@metamarkets.com>
Date:   Fri Jun 19 12:35:36 2015 -0700

    Fix bad distribution of cache keys across nodes

    With the existing hash function some nodes could end up with 3 times the
    number of keys as others. The following changes improve that to roughly
    less than 5% differences across nodes.

    - switch from fnv-1a to murmur3_128 hash
    - increase repetitions for ketama algorithm
    - test to analyze distribution

    Also updates spymemcached for recent bugfixes
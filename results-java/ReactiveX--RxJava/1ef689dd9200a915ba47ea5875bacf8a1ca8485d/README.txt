commit 1ef689dd9200a915ba47ea5875bacf8a1ca8485d
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Sat Feb 15 10:27:07 2014 -0800

    Lift Performance

    Using `f.lift()` directly instead of `subscribe` improves ops/second on the included test from 5,907,721 ops/sec to 10,145,486 ops/sec
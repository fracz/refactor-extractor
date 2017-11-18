commit 346d0ca5382800f4bbc670b80660408705742392
Author: Armin Braun <me@obrown.io>
Date:   Mon Sep 18 10:38:26 2017 +0100

    MINOR: Fix needless GC + Result time unit in JMH

    Fixes two issues with the JMH benchmark example:
    * Trivial: The output should be in `ops/ms` for readability
    reasons (it's in the millions of operations per second)
    * Important: The benchmark is not actually measuring the
    LRU Cache performance as most of the time in each run is
    wasted on concatenating `key + counter` as well as
    `value + counter`. Fixed by pre-generating 10k K-V pairs
    (100x the cache capacity) and iterating over them. This
    brings the performance up by a factor of more than 5 on
    a standard 4 core i7 (`~6k/ms` before goes to `~35k/ms`).

    Author: Armin Braun <me@obrown.io>

    Reviewers: Bill Bejeck <bbejeck@gmail.com>, Guozhang Wang <wangguoz@gmail.com>, Ismael Juma <ismael@juma.me.uk>

    Closes #2903 from original-brownbear/fix-jmh-example
commit 726e0a88a44284a2f28a7c209deb312e079b5d0c
Author: Eric Wendelin <eric@gradle.com>
Date:   Mon May 15 12:46:45 2017 -0700

    Instantiate progress operation children sets lazily

    This should incrementally improve performance by avoiding creation
    of HashSets when they're never used.
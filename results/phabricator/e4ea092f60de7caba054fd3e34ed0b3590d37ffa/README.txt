commit e4ea092f60de7caba054fd3e34ed0b3590d37ffa
Author: epriestley <git@epriestley.com>
Date:   Sat May 10 10:10:13 2014 -0700

    Implement a chunked, APC-backed graph cache

    Summary:
    Ref T2683. This is a refinement and simplification of D5257. In particular:

      - D5257 only cached the commit chain, not path changes. This meant that we had to go issue an awkward query (which was slow on Facebook's install) periodically while reading the cache. This was reasonable locally but killed performance at FB scale. Instead, we can include path information in the cache. It is very rare that this is large except in Subversion, and we do not need to use this cache in Subversion. In other VCSes, the scale of this data is quite small (a handful of bytes per commit on average).
      - D5257 required a large, slow offline computation step. This relies on D9044 to populate parent data so we can build the cache online at will, and let it expire with normal LRU/LFU/whatever semantics. We need this parent data for other reasons anyway.
      - D5257 separated graph chunks per-repository. This change assumes we'll be able to pull stuff from APC most of the time and that the cost of switching chunks is not very large, so we can just build one chunk cache across all repositories. This allows the cache to be simpler.
      - D5257 needed an offline cache, and used a unique cache structure. Since this one can be built online it can mostly use normal cache code.
      - This also supports online appends to the cache.
      - Finally, this has a timeout to guarantee a ceiling on the worst case: the worst case is something like a query for a file that has never existed, in a repository which receives exactly 1 commit every time other repositories receive 4095 commits, on a cold cache. If we hit cases like this we can bail after warming the cache up a bit and fall back to asking the VCS for an answer.

    This cache isn't perfect, but I believe it will give us substantial gains in the average case. It can often satisfy "average-looking" queries in 4-8ms, and pathological-ish queries in 20ms on my machine; `hg` usually can't even start up in less than 100ms. The major thing that's attractive about this approach is that it does not require anything external or complicated, and will "just work", even producing reasonble improvements for users without APC.

    In followups, I'll modify queries to use this cache and see if it holds up in more realistic workloads.

    Test Plan:
      - Used `bin/repository cache` to examine the behavior of this cache.
      - Did some profiling/testing from the web UI using `debug.php`.
      - This //appears// to provide a reasonable fast way to issue this query very quickly in the average case, without the various issues that plagued D5257.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley, jhurwitz

    Maniphest Tasks: T2683

    Differential Revision: https://secure.phabricator.com/D9045
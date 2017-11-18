commit c17de49a6dc1d54fcfee3754211ae67a06bdcec7
Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
Date:   Wed Apr 19 20:37:20 2017 +0200

    [percolator] Fix memory leak when percolator uses bitset or field data cache.

    The percolator doesn't close the IndexReader of the memory index any more.
    Prior to 2.x the percolator had its own SearchContext (PercolatorContext) that did this,
    but that was removed when the percolator was refactored as part of the 5.0 release.

    I think an alternative way to fix this is to let percolator not use the bitset and fielddata caches,
    that way we prevent the memory leak.

    Closes #24108
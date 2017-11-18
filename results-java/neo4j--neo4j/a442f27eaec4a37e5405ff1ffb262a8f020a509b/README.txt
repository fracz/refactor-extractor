commit a442f27eaec4a37e5405ff1ffb262a8f020a509b
Author: Jacob Hansson <jakewins@gmail.com>
Date:   Fri Mar 7 16:01:42 2014 +0100

    Faster upgrade.

     o Changes strategy from scanning node store to scanning rel store, leading to much less random access.
       Upgrade now requires a single sequential scan through rel store, plus random access into node store,
       one access for each node.
     o Modifies the scanning strategy to use 4K-aligned scan buffer instead of 33b buffer, improves scan speed
       one order of magnitude.
     o Remaining: Memory usage is still very high, need to investigate performance when RAM runs out.
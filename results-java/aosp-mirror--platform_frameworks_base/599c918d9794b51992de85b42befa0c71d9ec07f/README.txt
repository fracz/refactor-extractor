commit 599c918d9794b51992de85b42befa0c71d9ec07f
Author: Andy McFadden <>
Date:   Wed Apr 8 00:35:56 2009 -0700

    AI 144931: Added a (hidden) way to "pre-cache" register maps.
      The 50 methods that appeared on the GC stacks of the most applications
      require 13KB of native heap for their uncompressed register maps, and
      the full set took 5ms to uncompress.  Pre-computation doesn't represent
      a significant improvement in space or time, at the cost of a big pile
      of strings in ZygoteInit.
      I'm leaving the method in ZygoteInit, but it's not called, and the
      static final String[] of method descriptors is empty.  We may want to
      revisit this later.
      BUG=1729570

    Automated import of CL 144931
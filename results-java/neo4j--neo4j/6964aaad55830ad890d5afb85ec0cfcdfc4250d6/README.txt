commit 6964aaad55830ad890d5afb85ec0cfcdfc4250d6
Author: Chris Vest <mr.chrisvest@gmail.com>
Date:   Sun Mar 22 15:29:14 2015 +0100

    Improvements to LinearHistoryPageCacheTracer

    * Significantly improve the print-out speed of large histories by buffering
      output to reduce the system call overhead on non-buffered PrintStreams.
    * Fix a couple of formatting errors where two events could end up on the same
      line.
    * Always record the PageSwapper in eviction events, even when the flush fails.
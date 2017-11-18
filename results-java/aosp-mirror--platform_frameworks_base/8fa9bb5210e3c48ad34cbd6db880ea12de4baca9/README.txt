commit 8fa9bb5210e3c48ad34cbd6db880ea12de4baca9
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Tue Mar 26 17:37:46 2013 -0700

    Benchmarks for IndentingPrintWriter.

    Current timings on a mako listed below, which show minimal overhead
    compared to writing directly. The increased code readability and
    automatic indenting is worth the overhead.

    Also worth noting is that writing concatenated strings is
    substantially faster than printing components separately.

               benchmark      us linear runtime
           ComplexDirect 10712.7 =========================
        ComplexIndenting 12623.7 ==============================
           PairIndenting    45.8 =
                 PairRaw    82.1 =
            SimpleDirect   282.2 =
         SimpleIndenting   294.4 =

    Change-Id: I7c38690c14b017fa46248ebb0be69f6beff03371
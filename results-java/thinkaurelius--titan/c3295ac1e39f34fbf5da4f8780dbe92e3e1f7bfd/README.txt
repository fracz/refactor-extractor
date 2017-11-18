commit c3295ac1e39f34fbf5da4f8780dbe92e3e1f7bfd
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Mon May 7 05:37:45 2012 -0400

    HBase storage adaptor updates

    * Replaced single private-final HTable instance in each HBase KCVStore
      with an HTablePool.  Each method pulls an HTable off of this pool
      and onto its stack, then returns it in a finally block.

    * Reimplemented the mutate method to use HTable#batch instead of
      separate HTable#delete and HTable#put calls.  I would rather use
      RowMutations, but that only appears in 0.94 which is not yet on the
      public Maven repositories

    * Deleted obsolete, commented method bodies from before the
      mutate/mutateMany refactoring a couple weeks ago
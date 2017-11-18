commit 4a887b2fbaa2c7681e256fd28461d35984e8942e
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Sun Mar 6 14:18:28 2016 +0100

    Bigger page size during import

    to 8MiB instead of the default 8KiB. Amount mapped memory is set to
    a low enough number of pages to be able to handle a couple of stores
    updates during a single stage and leaving some room for the page cache
    to maneuver. Also no longer explicitly flushes stores after each batch
    due to the low number of pages.

    The effects of this change are:
    - configuration code simpler due to not needing to calculate memory size
      from batch size.
    - less overhead spent in reading/writing pages, which improves performance
      of import overall since those steps usually being the bottlenecks.
    - helps with a scenario during Relationship-->Relationship linkback stage
      where on Windows OS/FS memory consumption seemingly increasing slightly
      with each I/O access during this backward scan of the relationship store.
      Bigger page size means less I/O accesses and therefore heavily reducing
      this problem.

    Overall this change have been seen to improve I/O access at least 30-50%
    when doing an import.
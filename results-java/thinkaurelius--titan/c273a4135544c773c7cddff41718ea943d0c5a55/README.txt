commit c273a4135544c773c7cddff41718ea943d0c5a55
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Fri Oct 4 18:04:06 2013 -0400

    Make InMemoryKCVS respect column slice bounds

    This hack could probably be improved by caching the column-values
    entries for each row when they are partially read to calculate
    hasNext() in the store's RowIterator implementation.
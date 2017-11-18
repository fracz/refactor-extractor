commit e933d5edbcd9d46d3a636c1265250e9c45861804
Author: Janak Ramakrishnan <janakr@google.com>
Date:   Fri Jan 8 16:43:54 2016 +0000

    When transforming labels into targets in SkyQueryEnvironment, stream the result to a callback instead of returning it directly. This means that the targets a precomputed pattern resolves to can be processed incrementally.

    This is the sixth and hopefully final step in a series to allow processing large sets of targets in query target patterns via streaming batches rather than all at once. This should improve performance for SkyQueryEnvironment for certain classes of large queries.

    --
    MOS_MIGRATED_REVID=111697983
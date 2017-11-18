commit 4fb9bb7b8be98454d1bc5fcbc6631dcd310d4f34
Author: Martin Traverso <martint@fb.com>
Date:   Tue Mar 18 15:39:24 2014 -0700

    Improve performance when computing partitions domain summary

    This brings down the computation time from 70s (for 12k partitions) down to 250ms on my mac.
    The previous algorithm was O(N^2) on the number of partitions times some non-obvious factor due
    to how SortedRangeSet.union is implemented. There's still some room for improvement there.
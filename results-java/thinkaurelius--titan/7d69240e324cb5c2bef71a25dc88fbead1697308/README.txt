commit 7d69240e324cb5c2bef71a25dc88fbead1697308
Author: Matthias Broecheler <me@matthiasb.com>
Date:   Thu Sep 26 21:37:22 2013 -0700

    Fixes #335 by way of introducing an LRU VertexCache. Added configuration option tx-cache-size to make its size configurable on a per transaction basis.
    Greatly refactored the PerformanceTest Case to verify that the LRU cache indeed solves the running out of memory problem as well as test for other memory leaks.
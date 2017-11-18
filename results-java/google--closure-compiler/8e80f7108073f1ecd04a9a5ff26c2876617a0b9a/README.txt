commit 8e80f7108073f1ecd04a9a5ff26c2876617a0b9a
Author: Ben Lickly <blickly@google.com>
Date:   Fri Jun 21 16:18:18 2013 -0700

    This does three things:
    1) It makes using es3 keywords and reserved words non fatal by default, it rewrites it to be a quoted property access.
    2) It improves the error message displayed when a property like this is seen.
    3) It introduces an ES3 diagnostic group that can be used to make these warnings errors again.

    Fixes issue 1030
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=48372507
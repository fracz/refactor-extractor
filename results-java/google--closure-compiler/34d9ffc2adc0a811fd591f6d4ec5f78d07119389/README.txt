commit 34d9ffc2adc0a811fd591f6d4ec5f78d07119389
Author: blickly <blickly@google.com>
Date:   Fri Jun 24 16:52:14 2016 -0700

    Skip the iterator creation in NodeTraversal.traverseBlockScope

    Since this is called many times, the performance impact may be significant.
    Also refactor some duplicated code.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=125830750
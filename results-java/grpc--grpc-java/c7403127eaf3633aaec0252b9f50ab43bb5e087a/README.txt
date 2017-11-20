commit c7403127eaf3633aaec0252b9f50ab43bb5e087a
Author: Eric Anderson <ejona@google.com>
Date:   Wed Jun 24 08:41:56 2015 -0700

    Revert swapping to the "canonical HTTP mapping"

    The mapping is poorly suited for gRPC. C and Go don't even do any
    mapping. We can improve the mapping in the future, but it is very
    important that users don't start depending on the current mapping.

    This change is "inspired by" the original code, but is even more
    conservative.

    Fixes #477
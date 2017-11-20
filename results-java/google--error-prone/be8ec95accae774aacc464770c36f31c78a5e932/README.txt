commit be8ec95accae774aacc464770c36f31c78a5e932
Author: bangert <bangert@google.com>
Date:   Tue Apr 26 16:03:31 2016 -0700

    A generic checker for restricted APIs

    (e.g. insecure crypto APIs, things that should be refactored away).

    This generalizes the deprecation-like mechanism used in the pluggable
    type checker of allowing legacy callers with an annotation and providing
    a separate annotation for reviewed callers.

    RELNOTES: Generic checker to restrict APIs to white-listed call-sites.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=120863028
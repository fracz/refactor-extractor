commit 8e5bf4b22a5426df605aa763e3acdeed4c8e8f1f
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Tue May 2 13:15:31 2017 +0200

    Polish CachePublicMetrics

    CachePublicMetrics wasn't explicitly tested and was still using field
    injection. This commit improves the situation in preparation of the fix
    for gh-8984
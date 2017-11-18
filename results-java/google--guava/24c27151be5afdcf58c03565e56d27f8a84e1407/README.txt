commit 24c27151be5afdcf58c03565e56d27f8a84e1407
Author: Chris Povirk <cpovirk@google.com>
Date:   Fri Oct 24 07:14:22 2014 -0700

    Remove the array copy from Fingerprint2011.hashBytes(byte[], int, int) method.

    Conclusion: for hashFunctionWithOffset the improvements are visible in terms of bytes used, but also in terms of execution speed (before: 297,340; after: 173,506). Interestingly enough, there are small improvements for the hashBytes without offset version (before: 184,860; after: 164,804).
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=78433787
commit b2f0d0fad93f11bb86a2ce1770bcb43fd014fa06
Author: Kurt Kluever <kak@google.com>
Date:   Fri Mar 1 12:02:43 2013 -0500

    Delete EmptyImmutableList, replacing it with a singleton instance of RegularImmutableList.  This makes the common case for ImmutableList bimorphic instead of trimorphic, resulting in significant improvements to benchmarks.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=43197187
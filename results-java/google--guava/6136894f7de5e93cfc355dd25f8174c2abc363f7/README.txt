commit 6136894f7de5e93cfc355dd25f8174c2abc363f7
Author: Chris Povirk <cpovirk@google.com>
Date:   Thu Dec 6 09:43:00 2012 -0500

    New implementation of Objects.ToStringHelper:

    - More efficient, doesn't create a list of temporary holders.
    - Some javadoc improvements.
    - New benchmark.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=39701659
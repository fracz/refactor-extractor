commit c5a469757a46757302030da1a00cedc22c6afa6a
Author: cpovirk <cpovirk@google.com>
Date:   Mon Aug 3 11:36:33 2015 -0700

    Suppress tests on Android.
    - BigInteger and BigDecimal appear to be buggy on Android (at least Gingerbread, where I'm testing now, since we claim to work with it).
    - Some c.g.c.math utilities appear to be failing there, presumably because of other Android bugs, but I suppose it's conceivable that there's something wrong with our code. Ideally we will investigate further at some point, perhaps after testing on newer versions of Android.

    Also, check in some of the failure-message improvements I put together while debugging.

    (Note that we don't actually run these tests on Android at HEAD. This will be changing soon.)
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=99743836
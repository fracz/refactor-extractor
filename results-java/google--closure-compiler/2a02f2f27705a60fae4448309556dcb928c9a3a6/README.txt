commit 2a02f2f27705a60fae4448309556dcb928c9a3a6
Author: Ben Lickly <blickly@google.com>
Date:   Fri Jul 25 14:52:26 2014 -0700

    [NEW TYPE INFERENCE] Remove locations to improve reuse of types.

    Unfortunately, we lose some inference with this change.
    Depending on other perf improvements, we may re-add locations in the future.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=71875828
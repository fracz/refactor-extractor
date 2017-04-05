commit 1c74a1a0fe8553223a635b5752eb2f174e3e6a38
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Mon Feb 20 15:58:04 2017 +0100

    Improve allowNullValue handling when a null value is provided

    This commit improves `AbstractValueAdaptingCache` to throw a dedicated
    exception if `allowNullValues` is `false` and a `null` value is provided
    anyway. This avoid a lower-level exception from the cache library that
    will miss some context.

    Issue: SPR-15173
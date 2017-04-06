commit 641e13acc180000a8b1293a1bf9dfef29673e2ea
Author: Georgios Kalpakas <kalpakas.g@gmail.com>
Date:   Fri Dec 9 14:24:00 2016 +0200

    refactor(*): replace `HashMap` with `NgMap`

    For the time being, we will be using `NgMap`, which is an API-compatible
    implementation of native `Map` (for the features required in Angular). This will
    make it easy to switch to using the native implementations, once they become
    more stable.

    Note:
    At the moment some native implementations are still buggy (often in subtle ways)
    and can cause hard-to-debug failures.)

    Closes #15483
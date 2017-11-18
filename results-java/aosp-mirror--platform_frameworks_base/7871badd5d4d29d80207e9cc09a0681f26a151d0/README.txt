commit 7871badd5d4d29d80207e9cc09a0681f26a151d0
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon Dec 12 15:19:26 2011 -0800

    SDK only: now that support lib is in SDK, we can link to it.

    Update some of the platform documentation to directly link to
    relevent support lib docs.  Yay!

    Also improve BroadcastReceiver documentation to more clearly
    discussion security around receivers, and how the support
    lib's LocalBroadcastManager can help.

    Change-Id: I563c7516d5fbf91ab884c86bc411aff726249e42
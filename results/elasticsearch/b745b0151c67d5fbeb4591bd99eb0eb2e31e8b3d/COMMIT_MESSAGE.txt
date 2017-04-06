commit b745b0151c67d5fbeb4591bd99eb0eb2e31e8b3d
Author: Adrien Grand <jpountz@gmail.com>
Date:   Wed Aug 27 13:21:23 2014 +0200

    Fielddata: Remove soft/resident caches.

    These caches have no advantage compared to the default node cache. Additionally,
    the soft cache makes use of soft references which make fielddata loading quite
    unpredictable in addition to pushing more pressure on the garbage collector.

    The `none` cache is still there because of tests. There is no other good
    reason to use it.

    LongFieldDataBenchmark has been removed because the refactoring exposed a
    compilation error in this class, which seems to not having been working for a
    long time. In addition it's not as much useful now that we are progressively
    moving more fields to doc values.

    Close #7443
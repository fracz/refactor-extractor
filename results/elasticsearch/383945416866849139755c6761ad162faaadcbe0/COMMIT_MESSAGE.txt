commit 383945416866849139755c6761ad162faaadcbe0
Author: Adrien Grand <jpountz@gmail.com>
Date:   Wed Feb 26 16:40:15 2014 +0100

    Add tracking of allocated arrays.

    The BigArrays utility class is useful to generate arrays of various sizes: when
    small, arrays will be allocated directly on the heap while larger arrays are
    going to be paged and to recycle pages through PageCacheRecycler. We already
    have tracking for pages but this is not triggered very often since it only
    happens on large amounts of data while our tests work on small amounts of data
    in order to be fast.

    Tracking arrays directly helps make sure that we never forget to release them.

    This pull request also improves testing by:

     - putting random content in the arrays upon release: this makes sure that
       consumers don't use these arrays anymore when they are released as their
       content may be subject to use for another purpose since pages are recycled

     - putting random content in the arrays upon creation and resize when
       `clearOnResize` is `false`.

    The major difference with `master` is that the `BigArrays` class is now
    instanciable, injected via Guice and usually available through the
    `SearchContext`. This way, it can be mocked for tests.
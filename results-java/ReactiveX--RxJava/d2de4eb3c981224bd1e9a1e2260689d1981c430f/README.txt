commit d2de4eb3c981224bd1e9a1e2260689d1981c430f
Author: akarnokd <akarnokd@gmail.com>
Date:   Sat Sep 5 23:56:42 2015 +0200

    2.x: classes Single and NbpObservable

    `Single` is a deferred single-value emitting Observable.

    `NbpObservable` is the non-backpressure Observable, named this way until
    the main Observable gets all its tests ported so a refactor can deal
    with all of them.

    I've implemented the most basic construction and operator methods on
    them.
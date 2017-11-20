commit 81bce90abfa2bcff08ad8ef8a6d6afa275f4137a
Author: Ben Manes <ben.manes@gmail.com>
Date:   Wed Dec 30 17:04:48 2015 -0800

    Add conservative update option to the simulator

    This option improves the accuracy of the sketch at the cost of an
    additional query. This tends to have up to a 0.3% increase to the
    hit rate, but can have surprising negative effects like on the LIRS
    cs trace (-1.3%). The use case as a filter seems to indicate that
    this technique isn't especially valuable, which is why it wasn't
    used in Caffeine's 4-bit sketch. But, its useful to have in the
    simulator for others to observe when writing their own caching
    libraries.
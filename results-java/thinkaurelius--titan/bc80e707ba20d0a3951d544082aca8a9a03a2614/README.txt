commit bc80e707ba20d0a3951d544082aca8a9a03a2614
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu Apr 9 17:38:48 2015 -0400

    Addition compile error fixes

    The only remaining compile errors in TitanGraphTest following this
    commit should be inside testTinkerPopOptimizationStrategies and the
    private helper method called only by that method (assertNumStep).  It
    is possible to refactor this test to be TP3-strategy-oblivious, but
    then much of its distinctive value would be lost.  Leaving it broken
    for the time being.

    I gave this only one close read through the git-diff before
    committing.  This hasn't been tested at all.  There could easily be
    some runtime errors in here that I missed.
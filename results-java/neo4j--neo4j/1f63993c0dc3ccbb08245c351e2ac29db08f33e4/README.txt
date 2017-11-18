commit 1f63993c0dc3ccbb08245c351e2ac29db08f33e4
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Fri Apr 8 15:51:29 2016 +0200

    Robust core state snapshot and install.

    Cleans up and makes this area more robust. Previously the framework for
    core-to-core copy was put in place, but now the actual copying of state
    is made robust. More specifically by implementing a correct order of
    copying state machine states versus the store and taking immutable
    copies where appropriate.

    The wiring of different components has also been refactored into a
    simpler and more natural setup. Some complexity is still left around
    the creation of the commit process factory in the interface between
    ECEM and NSDS, but it has been vastly simplified.
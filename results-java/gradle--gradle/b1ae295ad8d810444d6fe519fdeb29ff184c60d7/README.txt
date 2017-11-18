commit b1ae295ad8d810444d6fe519fdeb29ff184c60d7
Author: Cedric Champeau <cedric@gradle.com>
Date:   Wed Sep 20 21:56:06 2017 +0200

    Improve version range selection

    This commit fixes a couple of bugs in range selection. It refactors the code, making it clearer what the different states
    of a module can be. In particular, the previous version included both `Conflict` and `Evicted`, but in practice there was
    no difference in using one or the other!

    Now the selection uses four different states (`Selected`, `Selectable`, `Evicted` and `Orphan`) which allows us to
    discriminate between cases a node was evicted in conflict resolution, or when it was simplify preferred over another
    possible choice.
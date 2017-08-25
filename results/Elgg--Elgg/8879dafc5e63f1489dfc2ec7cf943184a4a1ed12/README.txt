commit 8879dafc5e63f1489dfc2ec7cf943184a4a1ed12
Author: Juho Jaakkola <juho@elgg.org>
Date:   Tue Nov 8 20:30:49 2016 +0200

    chore(core): overhauls async upgrade process

    Allows core to declare async upgrades.
    Batch methods renamed to add clarity.
    Docs improved.

    Async upgrades no longer require knowing how many items must be processed.
    The batch can simply keep processing items and mark the result object
    complete at the end. Client-side the progress bar just moves ahead 50% of
    the remaining distance.

    Fixes #10513
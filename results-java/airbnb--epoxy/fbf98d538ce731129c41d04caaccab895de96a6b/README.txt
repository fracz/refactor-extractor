commit fbf98d538ce731129c41d04caaccab895de96a6b
Author: Nicholas Corder <niccorder25@gmail.com>
Date:   Mon Oct 2 14:47:24 2017 -0700

    Replace UnmodifiableList with java collection's. (#302)

    This is a small clean-up to reduce a class which is not necessary as we
    can use the `Collections.unmodifiableList(...)` instead of using the
    custom `UnmodifiableList` epoxy had itself. This is not a refactor of
    functionality, but redundancy.
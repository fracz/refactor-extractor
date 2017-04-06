commit 4f49a261a748b6fa75e0da46b11f4f4a8011ab47
Author: Jason Tedor <jason@tedor.me>
Date:   Tue Jun 21 19:07:03 2016 -0400

    Refactor InternalEngine inner methods

    This commit refactors InternalEngine#innerIndex and
    InternalEngine#innerDelete to collapse some common logic into a single
    method. This has the advantage that it shrinks the bytecode size of
    InternalEngine#innerIndex so that it can be inlined.
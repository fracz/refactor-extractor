commit a62c9d67f0b411a2a5c9e686a955aca404610d89
Author: blickly <blickly@google.com>
Date:   Tue Aug 8 15:37:37 2017 -0700

    Refactor the .i.js generation pass to better represent declarations.

    The greatly simplifies the final SimplifyDeclarations traversal.

    This refactoring is hopefully also a first step toward determining
    (and pruning) unneeded requires in the .i.js output.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=164655084
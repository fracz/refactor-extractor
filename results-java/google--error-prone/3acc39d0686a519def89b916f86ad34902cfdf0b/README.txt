commit 3acc39d0686a519def89b916f86ad34902cfdf0b
Author: paulduffin <paulduffin@google.com>
Date:   Tue Sep 26 01:50:22 2017 -0700

    Avoid reorganizing the imports if there are no changes

    Adding an existing import or removing a non-existent import
    would reorganize the imports even though there was no actual
    change to the set of imports. While that is not usually an
    issue when the imports are already sorted it does cause
    unnecessary changes in code that has unsorted imports, or
    imports whose order does not match that expected by Error
    Prone. This change will only reorder the imports when the set
    of imports actually changes.

    RELNOTES: Will only reorganize the imports if the set actually changes

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=170023287
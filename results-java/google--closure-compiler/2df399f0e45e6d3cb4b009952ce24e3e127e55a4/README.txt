commit 2df399f0e45e6d3cb4b009952ce24e3e127e55a4
Author: blickly <blickly@google.com>
Date:   Tue May 30 15:25:20 2017 -0700

    Refactor ConvertToTypedInterface

    Split the RemoveCode traversal into two separate portions:
    RemoveNonDeclarations: which removes code that doesn't affect typing like function bodies and control flow structures.
    SimplifyDeclarations: which simplifies assignments by removing duplication and removing unnecessary right-hand sides and adding JSDoc if necessary.

    Also do some unrelated refactoring, like pulling the JSDoc rewriting helper methods into a static helper class.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=157520686
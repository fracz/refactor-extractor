commit 931352f95a87823e17d12adbb2aa8ba6d7bb2ba6
Author: bangert <bangert@google.com>
Date:   Mon Jun 26 15:41:38 2017 -0700

    [RefasterJS] Add support for correctly handling goog.require inside a goog.module.

    - Attempts to detect whether a file should use const foo = goog.require or just
    goog.require.
    - If using short names, rewrite the template to use aliases as appropriate.
    - Fix sorting so the goog.require statements added by SuggestedFix are handled
    better. The sorting still isn't ideal (but there's the sort imports script + g4 fix).
    - Use originalName in place of name nodes when replacing (otherwise de-sugared
    code will be emitted).
    - To accomplish all this, modify the compiler to preserve goog.module sugar when running refasterjs. At least for the given refactorings, replacements on the sugared + de-sugared source code apply cleanly to the original source.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=160205920
commit 2c3b120a26612c5ad4856c043e888468b39a5f46
Author: Michajlo Matijkiw <michajlo@google.com>
Date:   Thu Sep 3 17:49:57 2015 +0000

    Remove unused ParserInputSource method and other minor cleanups

    Trying to curb usage of the create method taking a String for efficiency
    reasons. Noticed this method was unused + a few places where we could
    easily use chars instead of string. Not a major improvement but removes
    some temptation.

    RELNOTES:

    --
    MOS_MIGRATED_REVID=102258319
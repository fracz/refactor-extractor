commit 75ba9b938716a857a601f920bde87e5ee4413645
Author: Mark Schaller <mschaller@google.com>
Date:   Wed Dec 9 22:58:04 2015 +0000

    Efficiency improvements to GroupedList

    Presize GroupedListHelper sets when initialized from collections. Use
    CompactHashSets throughout. Avoid primitive autoboxing in Precondition
    statements.

    --
    MOS_MIGRATED_REVID=109835986
commit bc1cf157e6cc4b0de1523681c9a19f8cfcfd9385
Author: tbreisacher <tbreisacher@google.com>
Date:   Thu Sep 17 14:00:34 2015 -0700

    CollapseProperties: Always inline var nodes that are executed only once.

    Avoids an annoying warning and should improve code size as well.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=103323186
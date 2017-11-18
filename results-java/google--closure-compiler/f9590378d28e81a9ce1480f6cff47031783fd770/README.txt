commit f9590378d28e81a9ce1480f6cff47031783fd770
Author: tbreisacher <tbreisacher@google.com>
Date:   Mon Sep 21 10:24:28 2015 -0700

    CollapseProperties: Always inline var nodes that are executed only once, unless they are exported by convention.

    Avoids an annoying warning and should improve code size as well.

    Rollforward of cl/103323186
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=103557916
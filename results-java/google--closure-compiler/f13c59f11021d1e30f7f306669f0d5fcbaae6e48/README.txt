commit f13c59f11021d1e30f7f306669f0d5fcbaae6e48
Author: tbreisacher <tbreisacher@google.com>
Date:   Fri Sep 18 09:43:46 2015 -0700

    Automated g4 rollback of changelist 103323186.

    *** Reason for rollback ***

    Breakages

    *** Original change description ***

    CollapseProperties: Always inline var nodes that are executed only once.

    Avoids an annoying warning and should improve code size as well.

    ***
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=103390530
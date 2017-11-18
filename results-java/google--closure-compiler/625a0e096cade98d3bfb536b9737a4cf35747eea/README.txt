commit 625a0e096cade98d3bfb536b9737a4cf35747eea
Author: tbreisacher <tbreisacher@google.com>
Date:   Tue Sep 27 13:23:22 2016 -0700

    When pretty-print is enabled, include the semicolon and newline after a var/let/const statement.

    The semicolon was already being inserted if the statement was in a SCRIPT or BLOCK, but was omitted if the code printer was printing just the statement, which happens in the refactoring tools.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=134445966
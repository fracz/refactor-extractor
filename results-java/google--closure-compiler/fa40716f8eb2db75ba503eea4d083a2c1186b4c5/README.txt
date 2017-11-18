commit fa40716f8eb2db75ba503eea4d083a2c1186b4c5
Author: bradfordcsmith <bradfordcsmith@google.com>
Date:   Wed Apr 26 17:38:45 2017 -0700

    Add an index to JSModule and use an array to store them in JSModuleGraph.

    Also removes an unused method and adds a missing @VisibleForTesting annotation.

    Could provide a tiny performance improvement, because it removes the creation
    of a set used just for precondition checking that is now handled by checks
    around setting the module index.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=154369560
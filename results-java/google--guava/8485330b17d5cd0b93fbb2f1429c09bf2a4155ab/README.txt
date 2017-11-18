commit 8485330b17d5cd0b93fbb2f1429c09bf2a4155ab
Author: Chris Povirk <cpovirk@google.com>
Date:   Sat Jan 12 18:32:51 2013 -0500

    Trim lots of unused and redundant code.  In particular,

    * refactor hash table size computations into one place
    * use Maps.safeGet and friends where appropriate
    * eliminate code duplication between the two skeleton implementations of NavigableMap.descendingMap()
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=41052812
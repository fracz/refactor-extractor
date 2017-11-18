commit 32ce76a7042b30c9f820b50012f5736358991a72
Author: Dimitris Vardoulakis <dimvar@google.com>
Date:   Fri Jan 10 16:37:39 2014 -0800

    Adds a new pass which gathers property names defined in externs and stores them on the Compiler object. Existing passes are refactored to use this information (AmbiguateProperties, InlineProperties, RemoveUnusedClassProperties, RenameProperties).
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=59377658
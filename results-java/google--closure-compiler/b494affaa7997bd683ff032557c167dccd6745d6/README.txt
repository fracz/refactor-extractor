commit b494affaa7997bd683ff032557c167dccd6745d6
Author: dcc@google.com <dcc@google.com@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Fri Aug 13 19:27:10 2010 +0000

    Speed improvements for CallGraph.
    - Avoid MultiMaps.
    - Combine Function and Callsite creation into one traversal.
    - Speeds up Maps Callgraph creation by 30%, Pinto by 50%.
    - Still too slow.

    R=acleung
    DELTA=229  (98 added, 102 deleted, 29 changed)


    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=116006


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@360 b0f006be-c8cd-11de-a2e8-8d36a3108c74
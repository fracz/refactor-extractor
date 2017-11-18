commit 31baadd1293c5ae7af05b3a06a1ba3a06403e4ed
Author: johnlenz@google.com <johnlenz@google.com@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Mon Apr 4 15:49:01 2011 +0000

    Unlike a normal call, a reference passed to JSCompiler_ObjectPropertyString must be considered a possible modification to the name itself, not a possible modification to an alias.

    This fix unblocks the optimize parameters improvement.

    R=nicksantos
    DELTA=27  (27 added, 0 deleted, 0 changed)


    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=1245


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@962 b0f006be-c8cd-11de-a2e8-8d36a3108c74
commit d7e85b0997ebb3d920c977de7a4a3f8ede5ba2d8
Author: johnlenz@google.com <johnlenz@google.com@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Thu Jul 15 21:34:16 2010 +0000

    Add basic "free call" annotation and support for issue 180.  Additional
    testing is required to validate that new CALL nodes are not
    inappropriately introduced.

    Also, refactored PrepareAst to make the different actions being taken
    easier to follow.

    R=nicksantos
    DELTA=215  (120 added, 18 deleted, 77 changed)


    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=52003


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@282 b0f006be-c8cd-11de-a2e8-8d36a3108c74
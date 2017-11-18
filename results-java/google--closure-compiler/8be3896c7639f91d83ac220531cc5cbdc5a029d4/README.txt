commit 8be3896c7639f91d83ac220531cc5cbdc5a029d4
Author: nicksantos@google.com <nicksantos@google.com@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Mon Apr 25 16:07:07 2011 +0000

    Clean up global namespacing a bit, to make way for additional
    checks.
    I'm not quite sure if the change w/r/t 'declaration' and 'refs'
    is an improvement or not--i think it will allow us to get rid
    of special cases down the line, but am not sure.

    R=acleung
    DELTA=117  (40 added, 9 deleted, 68 changed)


    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=1542


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@1031 b0f006be-c8cd-11de-a2e8-8d36a3108c74
commit f01925cbf7a39a885e51865208368e861d25a0c1
Author: Nicholas.J.Santos <Nicholas.J.Santos@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Fri Apr 2 02:32:10 2010 +0000

    Tests for deps generation (Nick)
    R=andrew

    Fix for decomposing assignment-ops. (john)
    R=alan
    DELTA=159  (127 added, 21 deleted, 11 changed)

    Remove unneeded imports. (John)
    R=robert
    DELTA=2  (0 added, 2 deleted, 0 changed)

    Automated rollback of:
    Remove now unneeded checks for label names when inspecting NAME nodes. (Robert)
    R=Alan
    DELTA=6  (4 added, 0 deleted, 2 changed)

    Automated rollback of:
    Don't use NAME for label names, introduce LABEL_NAME. This improves the correctness of the compiler as many passes don't distinguish between label names and variable names appropriately. (Robert)
    R=Alan
    DELTA=57  (12 added, 29 deleted, 16 changed)

    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=gydkaf


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@167 b0f006be-c8cd-11de-a2e8-8d36a3108c74
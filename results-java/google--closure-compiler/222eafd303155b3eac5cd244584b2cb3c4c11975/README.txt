commit 222eafd303155b3eac5cd244584b2cb3c4c11975
Author: Nicholas.J.Santos <Nicholas.J.Santos@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Mon Apr 5 19:30:23 2010 +0000

    tighten up some types. (Nick)
    R=alan
    DELTA=4  (2 added, 0 deleted, 2 changed)

    Don't use NAME for label names, introduce LABEL_NAME. This improves the correctness of the compiler as many passes don't distinguish between label names and variable names appropriately. (John)
    R=robert

    Remove now unneeded checks for label names when inspecting NAME nodes. (John)
    R=robert

    Tweak code generator to break after blocks for better pretty printing. (John)
    R=robert
    DELTA=196  (160 added, 0 deleted, 36 changed)

    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=ktmses


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@170 b0f006be-c8cd-11de-a2e8-8d36a3108c74
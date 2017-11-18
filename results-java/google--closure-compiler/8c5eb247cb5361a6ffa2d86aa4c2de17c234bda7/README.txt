commit 8c5eb247cb5361a6ffa2d86aa4c2de17c234bda7
Author: Nicholas.J.Santos <Nicholas.J.Santos@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Wed Mar 31 04:01:45 2010 +0000

    Refactor how coding conventions are set (Nick)
    R=alan

    Don't use NAME for label names, introduce LABEL_NAME. This improves
    the correctness of the compiler as many passes don't distinguish
    between label names and variable names appropriately. (John)
    R=robert
    DELTA=57  (29 added, 12 deleted, 16 changed)

    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@163 b0f006be-c8cd-11de-a2e8-8d36a3108c74
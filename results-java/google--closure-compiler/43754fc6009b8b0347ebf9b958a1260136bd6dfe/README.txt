commit 43754fc6009b8b0347ebf9b958a1260136bd6dfe
Author: tbreisacher <tbreisacher@google.com>
Date:   Tue Apr 25 13:13:17 2017 -0700

    Automated rollback of c85375949fa3442ec1f1c4c3537aae34aed9f664

    *** Reason for rollback ***

    Appears to break the external build:

    [ERROR] COMPILATION ERROR :
    [INFO] -------------------------------------------------------------
    [ERROR] /home/travis/build/google/closure-compiler/src/com/google/javascript/refactoring/CodeReplacement.java:[19,28] error: package com.google.auto.value does not exist
    [ERROR] /home/travis/build/google/closure-compiler/src/com/google/javascript/refactoring/CodeReplacement.java:[26,1] error: cannot find symbol

    *** Original change description ***

    Use AutoValue (https://github.com/google/auto/) for CodeReplacement

    ***

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=154210919
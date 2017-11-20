commit c6c8963927a2da4c18eae81c9d207ed9a4aa3d4f
Author: cushon <cushon@google.com>
Date:   Tue Apr 5 20:37:33 2016 -0700

    Remove suggested fix for TypeParameterUnusedInFormals

    The suggested fix was meant to illustrate the problem, but it isn't an
    ideal fix and often doesn't compile without additional refactoring.

    RELNOTES: N/A
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=119121903
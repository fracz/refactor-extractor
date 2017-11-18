commit 911f2b94ced447f69c52436962ba67964c8832eb
Author: Yang Yang <geraint0923@gmail.com>
Date:   Tue May 31 15:50:30 2016 -0700

    Avoid re-analyzing SymbolReferences

    Optimizers such as DesugaringOptimizer and SimplifyExpressions
    need access to expression types, which is currently re-computed
    every time. This change improves the process by short-circuiting
    the analysis for SymbolReferences.
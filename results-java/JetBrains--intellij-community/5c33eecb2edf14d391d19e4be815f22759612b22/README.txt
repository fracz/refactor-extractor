commit 5c33eecb2edf14d391d19e4be815f22759612b22
Author: Tagir Valeev <Tagir.Valeev@jetbrains.com>
Date:   Tue Jul 18 17:37:13 2017 +0700

    TrivialFunctionalExpressionUsage bugfixes & refactoring

    Fixes IDEA-176019 Trivial functional expression: do not inline if parameter produces side effect and evaluated not once
    Fixes EA-103938 (invalid PSI was used if inlining replaced the whole method)
    Fixes inlining of functional expression
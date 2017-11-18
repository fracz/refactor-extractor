commit 81e951bf9022c5e2d4d66f8165218ba842021b70
Author: Tagir Valeev <Tagir.Valeev@jetbrains.com>
Date:   Wed Dec 14 17:55:46 2016 +0700

    StreamToLoopInspection#ensureCodeBlock refactored to use PsiTreeUtil.mark/releaseMark (IDEA-CR-16755)
    RefactoringUtil#expandExpressionLambdaToCodeBlock reverted (now changes there unnecessary)
    void single-expression lambda support
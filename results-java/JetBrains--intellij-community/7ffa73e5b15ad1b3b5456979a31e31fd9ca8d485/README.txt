commit 7ffa73e5b15ad1b3b5456979a31e31fd9ca8d485
Author: anna <Anna.Kozlova@jetbrains.com>
Date:   Wed Dec 28 13:37:27 2011 +0100

    base refactoring processor: do not proceed with the refactoring if find usages throws an exception
    EA-32497 - assert: BaseRefactoringProcessor.doRun
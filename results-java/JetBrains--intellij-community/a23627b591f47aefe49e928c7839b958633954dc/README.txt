commit a23627b591f47aefe49e928c7839b958633954dc
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Thu Mar 12 19:12:08 2015 +0300

    PY-4387 Both actions extends BaseRefactoringAction

    I also moved them to com.jetbrains.python.refactoring.convert package.
    Possible errors are reported using standard error dialogs now.
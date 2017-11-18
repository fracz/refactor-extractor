commit 099fa6c11efa0855f997021270b0164a56913bc9
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Tue Apr 9 13:44:29 2013 +0400

    Minor refactoring in Function/PropertyCodegen

    Check if we need to generate code before calling generateMethod()
    There were 4 such checks, 2 of them in PropertyCodegen, one in
    FunctionCodegen.gen() and one in ClosureCodegen. Every usage except the last
    was prepended with the check
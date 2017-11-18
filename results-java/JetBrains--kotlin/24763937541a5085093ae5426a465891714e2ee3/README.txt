commit 24763937541a5085093ae5426a465891714e2ee3
Author: Alexey Sedunov <alexey.sedunov@jetbrains.com>
Date:   Mon Nov 2 14:58:16 2015 +0300

    Change Signature: Do not move lambda out of parentheses if it neither wasn't out
    before nor corresponds to the last parameter after the refactoring
     #KT-9763 Fixed
commit fdecc14f0dfd70f0716badda110e32e173048e79
Author: Christopher Berner <christopherberner@gmail.com>
Date:   Tue Jul 14 10:36:07 2015 -0700

    Fix exception in RowNumberOperator

    RowNumberOperator will fail with an exception in unpartitioned limit
    mode, when the next operator blocks, but the previous one does not. Also
    improve the operator tests to test for this case.
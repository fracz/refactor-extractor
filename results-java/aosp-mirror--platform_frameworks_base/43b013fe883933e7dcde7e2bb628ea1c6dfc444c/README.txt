commit 43b013fe883933e7dcde7e2bb628ea1c6dfc444c
Author: Felipe Leme <felipeal@google.com>
Date:   Tue Dec 22 14:09:25 2015 -0800

    Minor improvements on 'Take bug report' Action.

    Long press was returning true, which would not close the window.
    Comments were outdated.

    BUG: 26034608
    Change-Id: Ia34906efff048299c709406039f86e7e14058259
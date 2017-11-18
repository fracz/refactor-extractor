commit 9c83ff471d482adabb8058dbd2a80c1e520765c8
Author: Yohei Yukawa <yukawa@google.com>
Date:   Thu Mar 12 15:31:25 2015 +0900

    Expose isSystemImeThatHasSubtypeOf to Settings

    This is a follow up CL for a recent attempt to minimize
    the number of default enabled IMEs.
    - part1: I831502db502f4073c9c2f50ce7705a4e45e2e1e3
    - part2: Ife93d909fb8a24471c425c903e2b7048826e17a3
    - part3: I6571d464a46453934f0a8f5e79018a67a9a3c845
    - part4: I871ccda787eb0f1099ba3574356c1da4b33681f3

    In the avobe CLs, an internal (hidden) method
    InputMethodUtils.isValidSystemDefaultIme was marked as
    deprecated and we decided to migrate to
    InputMethodUtils.isSystemImeThatHasSubtypeOf.

    To finish this refactoring, this CL make the new
    method visible to the settings app.

    InputMethodUtils.isValidSystemDefaultIme remains
    to be an internal method. No behavior change is
    intended.

    Change-Id: I8cb9ca40d15af099c3d1ded46797fb57f14fb9e8
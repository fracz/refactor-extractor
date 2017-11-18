commit 6aa037887800e34bd057585106609236c950ca22
Author: Yohei Yukawa <yukawa@google.com>
Date:   Sat Feb 21 03:00:22 2015 +0900

    Remove deprecated hidden public methods from InputMethodUtils.

    This is a follow up CL for a recent attempt to minimize
    the number of default enabled IMEs.
    - part1: I831502db502f4073c9c2f50ce7705a4e45e2e1e3
    - part2: Ife93d909fb8a24471c425c903e2b7048826e17a3
    - part3: I6571d464a46453934f0a8f5e79018a67a9a3c845
    - part4: I871ccda787eb0f1099ba3574356c1da4b33681f3

    This CL removes following deprecated hidden public methods
    from InputMethodUtils as planned.
    - isSystemImeThatHasEnglishKeyboardSubtype(InputMethodInfo)
    - isValidSystemDefaultIme(boolean, InputMethodInfo, Context)
    - containsSubtypeOf(InputMethodInfo, String, String)

    This is a pure code refactoring with preserving the current
    logic. Hence no behavior change is intended.

    Change-Id: I1ff994cbbdef83e1e907a0d88aa9ae09d45263b4
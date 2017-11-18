commit b21220efae92a56ff7b4b781fa614a6e3a8a3007
Author: Yohei Yukawa <yukawa@google.com>
Date:   Sat Nov 1 21:04:30 2014 +0900

    Minimize the number of default enabled IMEs part 4

    This is a follow up CL for recent attempt to minimize
    the number of default enabled IMEs.
    - part1: I831502db502f4073c9c2f50ce7705a4e45e2e1e3
    - part2: Ife93d909fb8a24471c425c903e2b7048826e17a3
    - part3: I6571d464a46453934f0a8f5e79018a67a9a3c845

    It turned out that the changes made in part2 and part3 are
    a bit overkill, and users will see no software keyboards
    in some particular situations. The problem we missed in
    the previous CLs is the fact that
    InputMethodInfo#isDefault is indeed a locale-dependent
    value, hence it may vary depending on the system locale.
    Existing unittests also failed to abstract such
    locale-dependent nature.

    In order to addresses that regression, the selection logic
    is a bit widely reorganized in this CL.  Now the logic is
    implemented as a series of fallback rules.

    Also, unit tests are updated to be able to 1) test the
    order of the enabled IMEs, and 2) emulate the
    locale-dependent behavior of InputMethodInfo#isDefault
    to enrich test cases.

    BUG: 17347871
    BUG: 18192576
    Change-Id: I871ccda787eb0f1099ba3574356c1da4b33681f3
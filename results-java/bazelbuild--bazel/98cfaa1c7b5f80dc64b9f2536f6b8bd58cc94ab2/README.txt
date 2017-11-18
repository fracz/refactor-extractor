commit 98cfaa1c7b5f80dc64b9f2536f6b8bd58cc94ab2
Author: hlopko <hlopko@google.com>
Date:   Wed Apr 26 15:16:24 2017 +0200

    Refactor ActionTester to pass BitSet to factories

    When discussing readability review for  unknown commit we realized that
    ActionTester api is not super readable and we could improve it using BitSet.
    This cl does just that.

    RELNOTES: None.
    PiperOrigin-RevId: 154289241
commit 0cdfdd651bf986e1454e6c063f81f56a10178aa2
Author: Borek Bernard <borekb@gmail.com>
Date:   Wed Mar 2 09:27:21 2016 +0100

    [#719] Refactored MergeDriverTest. Details:

    - MergeDriverTests now use more narrative style – the sequence of steps is directly visible in the test methods
    - Two new asserts for asserting clean and conflicting merges
    - Some methods in MergeDriverTestUtils renamed / refactored
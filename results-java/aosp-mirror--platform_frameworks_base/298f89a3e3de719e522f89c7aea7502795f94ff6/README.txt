commit 298f89a3e3de719e522f89c7aea7502795f94ff6
Author: Felipe Leme <felipeal@google.com>
Date:   Mon May 8 17:23:52 2017 -0700

    Fixed autofill dumpsys and improved logging.

    Fixes: 38196286
    Bug: 37997043

    Test: adb shell dumpsys autofill -a
    Test: CtsAutoFillServiceTestCases pass

    Change-Id: Ifaae7b5c0894ecf0d16fff8a3c96e4746fe2361b
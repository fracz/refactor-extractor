commit 9a7be3a3eb166fc3cf8453a55db636935e2eb410
Author: Diego Perez <diegoperez@google.com>
Date:   Wed Mar 1 14:18:10 2017 +0000

    Rename android.os.Build so it can be dynamically generated

    This will allow the android.os.Build class to be generated dynamically
    from the Studio side.
    In order to allow the "refactoring" of classses like android.os.Build,
    Create needed to be modified.

    While adding the test, I've also done a small clean-up of the
    AsmGeneratorTest to re-use some code.

    Bug: http://b.android.com/210345
    Test: Added new test to AsmGeneratorTest
    Change-Id: Ie4e1209c1c60b7a33cb427dbd556a9741ec8f8b3
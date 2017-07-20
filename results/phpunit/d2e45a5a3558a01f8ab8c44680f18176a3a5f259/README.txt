commit d2e45a5a3558a01f8ab8c44680f18176a3a5f259
Author: Sebastian Bergmann <sb@sebastian-bergmann.de>
Date:   Wed Sep 3 17:18:00 2014 +0200

    Initial work on refactoring to use SebastianBergmann\GlobalState for
    snapshotting global state.

    An expected value in Framework_TestCaseTest::testStaticAttributesBackupPost() was changed to make this test pass after refactoring. While I really hate doing that it appears that the previous expected value was wrong, thus hiding a bug in the previous implementation.
commit 8f5521aa679f21bc106dec90ecf21cd2283747a7
Author: Jeremy Joslin <jjoslin@google.com>
Date:   Tue Dec 20 14:36:20 2016 -0800

    Clear and restore the calling ID.

    Clear and restore the calling identity in IPC methods after asserting
    the caller has the required permissions.

    Fixed 2 tests in NetworkScoreServiceTest that were failing due to a
    recent refactor.

    Test: runtest frameworks-services -c com.android.server.NetworkScoreServiceTest
    BUG: 33781319
    Change-Id: Icd79751d12dcfe4af8026980aaa1f7bd463468dc
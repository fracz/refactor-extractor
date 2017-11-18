commit 29ed4a99bf3fae207f2359de34d3b37edf15e3dc
Author: Jeremy Joslin <jjoslin@google.com>
Date:   Tue Dec 20 14:36:20 2016 -0800

    Clear and restore the calling ID.

    Clear and restore the calling identity in IPC methods after asserting
    the caller has the required permissions.

    Fixed 2 tests in NetworkScoreServiceTest that were failing due to a
    recent refactor.

    Test: runtest frameworks-services -c com.android.server.NetworkScoreServiceTest
    BUG: 33781319
    Change-Id: I562713df3d9455cdc02bf80a687940fb9daecd8f
    Merged-In: Icd79751d12dcfe4af8026980aaa1f7bd463468dc
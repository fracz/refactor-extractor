commit 0da67d661e20a3361f0ee4f10a19d1b8edf5e73d
Author: Adrian Roos <roosa@google.com>
Date:   Mon Apr 17 14:50:22 2017 -0700

    AppErrors: Add test for AppErrorDialog.onCreate

    Also refactor to make sure we always remove the TIMEOUT
    message and clear the mProc.mCrashDialog field, even when
    dismissing without the Handler.

    Bug: 37351370
    Test: runtest -c com.android.server.am.AppErrorDialogTest frameworks-services
    Change-Id: If9fb348e8ed83c6c1c0f48fa7fd27ffd33b530f2
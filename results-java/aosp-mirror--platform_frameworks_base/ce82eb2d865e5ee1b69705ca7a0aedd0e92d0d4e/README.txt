commit ce82eb2d865e5ee1b69705ca7a0aedd0e92d0d4e
Author: Yohei Yukawa <yukawa@google.com>
Date:   Sun Feb 12 23:16:47 2017 -0800

    Rely on com.android.internal.os.SomeArgs

    Before introducing new state tracking IDs to IInputConnectionWrapper,
    this CL cleans up IInputConnectionWrapper to use
    com.android.internal.os.SomeArgs instead of local-defined one in favor
    of possible performance improvement thanks to the process grobal
    object pool that com.android.internal.os.SomeArgs has.

    This is a mechanical refactoring CL. No behavior change is intended.
    Test: No new warnings in `adb logcat` from the following TAGs
          - IInputConnectionWrapper
          - InputMethodManager
          - InputMethodManagerService
          - InputMethodService
    Bug: 35079353
    Change-Id: Ic614f112f960382280acd8891b3af56d47679f08
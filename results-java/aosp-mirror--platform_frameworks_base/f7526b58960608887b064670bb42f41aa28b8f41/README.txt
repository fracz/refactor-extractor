commit f7526b58960608887b064670bb42f41aa28b8f41
Author: Yohei Yukawa <yukawa@google.com>
Date:   Sat Feb 11 20:57:10 2017 -0800

    Merge restartInput into startInput in internal IPC

    This is the 2nd attempt to merge restartInput into startInput in
    internal IPC after fixing the mistake in new parameter order in
    the previous CL [1].

    As a preparation to start tracking all the event flows that
    cause InputMethodManagerService#setImeWindowStatus(), this CL
    merges an internal IPC method IInputMethod#restartInput() into
    IInputMethod#startInput() in favor of simplicity.

    This is a refactoring CL that should have no behavior change.

     [1]: Ifda6f74ac1b1370d9e9a9fe60354b692121fdcb9
          1a5838e966eab7a9f0dca71cabbc9922babb995e

    Test: Set true to InputMethodService#DEBUG and make sure startInput()
          and restartInput() are called in the following scenario.
          1. Complete the setup wizard.
          2. adb shell am start -a android.app.action.SET_NEW_PASSWORD
          3. Proceed to "Choose your password" page
          4. Make sure startInput() gets called.
          5. Type "aaaa" then hit "CONTINUE" button.
          6. Make sure restartInput() gets called.
    Bug: 35079353
    Change-Id: I476d0cf8cbb0a0134941854f9337d9ad15e66a71
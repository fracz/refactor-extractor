commit 6cab5e823a0053c60576c65cd307c865512eac38
Author: Alice Yang <alice@google.com>
Date:   Thu May 31 15:48:51 2012 -0700

    Fix bug where existing account not pre-checked

    The fix was to call ListView.setItemChecked()
    instead of ListView.setSelection() for setting radio button status.
    Also refactored the code a bit so it's less verbose.

    Bug 6588533

    Change-Id: I8add072a0277183baec7c0d5634a28f2d3a28c5e
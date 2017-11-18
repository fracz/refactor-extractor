commit bab851c7c9dfe6f3d063a1009c4d57cfa2ff005c
Author: Felipe Leme <felipeal@google.com>
Date:   Fri Feb 3 18:45:08 2017 -0800

    Refined session management so Save can be automatically called.

    This is yet another big refactoring:

    - AutoFillManager keeps track of its current AutoFillSession.
    - Views call AFM.startSession(View) when they can trigger autofill.
      (virtual views can call it as well). At this point, the manager
      sets an AutoFillSession, gets the activity token, and passes it to
      the service.
    - Subsequent calls to AFM.start() will be ignored since the session
      is set.
    - When the Activity is gone, it calls AFM.finishSession().
    - Simlilarly, virtual views could call it as well.
    - Added getAutoFillValue() to View.
    - Removed AFM.updateAutoFillInput(childId): virtual views should now
      call startSession(childId) to start a session, and use the
      VirtualViewListener callbacks for updates.
    - Change AutoFillValue to use String (which is immutable) instead of
      CharSequence for text values.
    - Check if view is enabled before auto-filling.
    - Removed 'cmd autofill fill' since it would require the appCallback
    - Automatically dismiss the snack bar after 30s
    - Moved the "don't change autofill value when autofilling" Inception
      logic into the service side.
    - Etc...

    BUG: 34819567
    BUG: 33269702
    BUG: 31001899

    Test: manual verification
    Test: CtsAutoFillServiceTestCases passes

    Change-Id: I5fad928d4d666701302049d142026a1efa7291cd
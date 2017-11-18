commit bd00fef41ce38d2c711bd4151997c1cd8964b712
Author: Felipe Leme <felipeal@google.com>
Date:   Tue Jan 24 15:10:26 2017 -0800

    Moar AutoFill Framework refactoring...

    * Encapsulated application-level auto-fill logic on AutoFillSession.
      Currently, Activity.java directly manages the IAutoFillCallback binder
      object used to auto-fill its views, but this logic need to be
      extended so Views can use it to draw the auto-fill bar.

    * Pass auto-fill id and boundaries to requests
      So AutoFillUI can display its affordance in the right places.

    * Uses a new auto-fill id on View (instead of reusing accessibility's).
      That allows moving the logic on whether a new request should be made or
      the existing UI moved to the service side.

    * Split service methods in 2, for shell cmd and app
      And applied the right permission check on both.

    * Removed CancelationSignal from onSaveRequest()
      Since it's not really needed.

    * Etc...
      ¯\_(ツ)_/¯

    BUG: 34637800
    BUG: 31001899
    Test: CtsAutoFillServiceTestCases passes
    Test: manual verification

    Change-Id: Ibd0cb2cfff6d0f6bb6b697a423ed5e89df687b82
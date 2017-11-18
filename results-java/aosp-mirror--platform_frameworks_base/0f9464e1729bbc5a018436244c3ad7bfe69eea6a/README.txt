commit 0f9464e1729bbc5a018436244c3ad7bfe69eea6a
Author: Felipe Leme <felipeal@google.com>
Date:   Wed Mar 29 15:54:53 2017 -0700

    Refactored ViewState.

    - Added an int state.
    - Removed mValueChanged and use state.
    - Removed unused mAuth
    - Set its Fillresponse in the proper places.
    - Encapsulated private attributes.
    - ...except mId (which is now id).
    - Stored only id of current view state on Session.

    This refactoring didn't modidy any behavior - in fact, the CTS tests didn't
    change - but it will make it much easier to implement partitioning.

    BUG: 35707731
    Test: CtsAutoFillServiceTestCases pass

    Change-Id: Ib07929a4089201a0e5bb66004af91f6cba362ba4
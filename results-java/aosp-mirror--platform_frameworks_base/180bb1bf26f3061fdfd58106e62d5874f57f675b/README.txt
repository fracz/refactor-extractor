commit 180bb1bf26f3061fdfd58106e62d5874f57f675b
Author: Gilles Debunne <debunne@google.com>
Date:   Thu Mar 10 11:14:00 2011 -0800

    Refactor in TextView's HandleViews.

    HandleView had a reference on its Controller, and Controller had
    a reference to its HandleView.

    This large refactoring breaks this dependency, creating smarter
    HandleView subclasses that don't have to delegate to the controller
    anymore.

    Change-Id: I472621f747cdc78fd8b2bba84c0edc62cb2f6316
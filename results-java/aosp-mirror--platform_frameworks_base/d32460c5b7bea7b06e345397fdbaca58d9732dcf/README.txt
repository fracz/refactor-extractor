commit d32460c5b7bea7b06e345397fdbaca58d9732dcf
Author: Jeff Brown <jeffbrown@google.com>
Date:   Fri Jul 20 16:15:36 2012 -0700

    Refactor local window manager implementation.

    The objective of this refactoring is to remove the reliance on
    WindowManager wrapper objects for compatibility mode and for
    managing sub-windows.

    Removed the WindowManager.isHardwareAccelerated() method since
    it is never used.

    Change-Id: I4840a6353121859a5e0c07d5cc307a437c595d63
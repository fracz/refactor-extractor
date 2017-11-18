commit d6a1692ef340608101c376fa2fd87d7c189bb906
Author: Filip Gruszczynski <gruszczy@google.com>
Date:   Tue Sep 8 13:00:49 2015 -0700

    Refactoring of setting app starting window code.

    This improves the code in following ways:
    * move transferring of starting window into a separate method, so it can
    be easier to understand;
    * move checking of whether to display starting window from phone window
    manager to window manager service; this has three benefits:
      1) whole logic is in one place;
      2) we don't need to schedule addition of a starting window, if we
         would stop it later anyway;
      3) we don't need to wait for creation of window style to decide, if a
         window is floating.

    Change-Id: Ibfbd87b84a7080e372211f162fa1865f8c5ab973
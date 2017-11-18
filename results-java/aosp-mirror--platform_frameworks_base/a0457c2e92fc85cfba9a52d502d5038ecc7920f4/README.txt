commit a0457c2e92fc85cfba9a52d502d5038ecc7920f4
Author: John Spurlock <jspurlock@google.com>
Date:   Fri Sep 26 13:22:08 2014 -0400

    Touch-exploration improvements to volume dialog.

     - Extend the dismiss timeout when interacting with various
       subcontrols.
     - Ensure "hover" events in touch exploration extend the timeout
       in addition to touch events.
     - Introduce new helper to standardize interaction callbacks.
     - Announce zen toasts.
     - Announce zen condition selections, and when existing countdown
       conditions are modified.

    Bug:17578434
    Change-Id: I8a055b3455aa8d20ba93439bdec6cc75db97800e
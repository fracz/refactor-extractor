commit abeab723d10b56302b86b1929ea994cf4a861fd0
Author: Felipe Leme <felipeal@google.com>
Date:   Mon Apr 4 11:01:44 2016 -0700

    Minor improvements useful for debugging.

    - Better dump of received intents by displayed the relevant extras.
    - Gracefully handles the case where the bugreport file URI is invalid
      during development.

    BUG: 27996121
    Change-Id: I97a48d1e9641142a43c66c1dded2f7f322dc66aa
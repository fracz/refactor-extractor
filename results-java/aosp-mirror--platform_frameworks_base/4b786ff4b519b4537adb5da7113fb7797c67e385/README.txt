commit 4b786ff4b519b4537adb5da7113fb7797c67e385
Author: John Spurlock <jspurlock@google.com>
Date:   Tue Dec 17 15:19:16 2013 -0500

    Battery meter: remove bolt color compromise & improve pct.

     - Bolt remains opaque (white) below a threshold level, otherwise
       transparent.
     - Draw the entire shape using a path, removing the need for a
       software layer.
     - Use a similar approach to make the percentage text readable,
       but this still requires a software layer (since text is involved).

    Bug: 12131168
    Change-Id: Ifde5e99121155bf1be171f44b2c80c116b17c9e7
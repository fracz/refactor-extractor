commit 99c12e8d5f12f239c28644a837937810299e5e3f
Author: Ben Murdoch <benm@google.com>
Date:   Wed Apr 25 15:00:17 2012 +0100

    Create WebViewDatabaseClassic from WebViewDatabase.

    WebViewDatabase has a getInstance() method, so similarly
    to WebStorage, WebIconDatabase etc we refactor it into a
    proxy class, and move the current implementation into
    WebViewDatabaseClassic.

    Also clean up some JavaDoc in touched files.

    Bug: 6234236
    Change-Id: I71cbd8f78e60f396e96e8546073ad634797cce15
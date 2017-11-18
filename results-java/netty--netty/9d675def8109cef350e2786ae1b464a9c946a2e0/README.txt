commit 9d675def8109cef350e2786ae1b464a9c946a2e0
Author: Norman Maurer <norman_maurer@apple.com>
Date:   Fri May 22 13:40:01 2015 +0200

    Only call System.nanoTime() if no read batch is ongoing. Related to [#3808]

    Motivation:

    [#3808] introduced some improvements to reduce the calls to System.nanoTime() but missed one possible optimization.

    Modifications:

    Only call System.nanoTime() if no reading patch is in process.

    Result:

    Less System.nanoTime() calls.
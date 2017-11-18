commit 0c3541be65fa87519a879c053a7cf4b4526be5db
Author: Brad Ebinger <breadley@google.com>
Date:   Tue Nov 1 14:11:38 2016 -0700

    Readability improvements in Logging

    We now truncate Session method paths and IDs based on the Logging level
    to improve Session Logging readability in logcat. If another external
    session is started off of an existing external Session, the preceding
    histories are replaced with "..." so that the Session information is not
    overwhelming.

    Bug: 26571395
    Test: Unit Tests and manual tests pass
    Change-Id: I9ffda3d64f1072fa6228a82a86116a5e47d18c96
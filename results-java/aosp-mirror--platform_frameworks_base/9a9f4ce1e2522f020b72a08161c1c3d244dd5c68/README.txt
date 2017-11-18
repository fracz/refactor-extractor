commit 9a9f4ce1e2522f020b72a08161c1c3d244dd5c68
Author: Steven Ross <stross@google.com>
Date:   Tue Oct 16 13:56:37 2012 -0400

    Stopping Face Unlock immediately on detaching from window fixes 7338808

    The refactoring broke my fix for 7222226.  We need to stop Face Unlock without
    any messaging delay when the window is detached to avoid it starting with an
    invalid window, or stopping right after it restarts.

    Change-Id: Iea23989ec2ab3ad7d1a57e2d1fb85163a6396024
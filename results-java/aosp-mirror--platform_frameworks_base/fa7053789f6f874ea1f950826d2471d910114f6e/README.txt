commit fa7053789f6f874ea1f950826d2471d910114f6e
Author: Winson Chung <winsonc@google.com>
Date:   Tue Nov 8 15:45:10 2016 -0800

    Adding experiment for minimized pinned stack.

    - Also refactoring the PIP touch handling to be independent gestures

    Test: Enable the setting in SystemUI tuner, then drag the PIP slightly
          offscreen. This is only experimental behaviour, and
          android.server.cts.ActivityManagerPinnedStackTests will be updated
          accordingly if we keep this behavior.

    Change-Id: I5834971fcbbb127526339e764e7d76b5d22d4707
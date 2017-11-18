commit 51f2d606dcbfba3cc5b03dfea37c1304b91c232f
Author: John Reck <jreck@google.com>
Date:   Wed Apr 6 07:50:47 2016 -0700

    Fix some edge cases

    Bug: 27709981

    This desperately needs a refactor, but to keep
    the current (really needed & nice) behavior of
    dispatching after sync finishes would be difficult
    to handle cleanly without lots of ripping so... #yolo

    Change-Id: I831a06c6ae7412a062720d68ecbe3085190f0258
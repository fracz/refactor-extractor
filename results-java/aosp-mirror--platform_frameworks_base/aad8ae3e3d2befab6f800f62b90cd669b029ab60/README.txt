commit aad8ae3e3d2befab6f800f62b90cd669b029ab60
Author: sergeyv <sergeyv@google.com>
Date:   Tue Feb 28 18:28:27 2017 -0800

    Move graphicstests to coretests

    Two reason for merge:
    1. Currently graphicstests aren't runned as part of acpt
    2. Separation was confusing: android.graphics package existed in both apks: frameworks & coretests.

    Test: refactoring CL.
    Change-Id: I0ade3ebbc2d06074ae81a2c390475f1f434dd873
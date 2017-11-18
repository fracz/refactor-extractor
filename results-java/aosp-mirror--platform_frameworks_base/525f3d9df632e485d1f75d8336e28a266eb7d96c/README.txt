commit 525f3d9df632e485d1f75d8336e28a266eb7d96c
Author: Craig Mautner <cmautner@google.com>
Date:   Tue May 7 14:01:50 2013 -0700

    Do not launch home task if activities remain

    When a root activity is finishing and it is supposed to return to
    home make sure there are only lower activities waiting to start before
    going home.

    Fixes bug 8632206.

    Various other refactorings for efficiency.

    Change-Id: I8bbb9de78d0ea9f45a504cf4bad72c698e9cc3d8
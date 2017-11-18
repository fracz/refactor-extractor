commit 03e636f33dfdb2f9438f06997a52b43291a83e41
Author: alanv <alanv@google.com>
Date:   Fri Oct 12 11:42:37 2012 -0700

    Don't alter accessibility JS APIs unless a page is about to load.

    Previously we were adding and removing the APIs on window attach and
    detach, but a page's life cycle is not related to attach/detach. This
    patch also ensures that ChromeVox is not null before calling. Includes
    formatting fixes and extra comments in the waitForResultTimedLocked
    method to improve readability. Fixes parsing of integer resultId, which
    was being parsed using Long.

    Bug: 7328348
    Change-Id: I6b81a8e4d8209f8e99419da9b8250abe57e25048
commit cb5f57bc580d7f73b93c8f504daa4dcd74cdce72
Author: Filip Gruszczynski <gruszczy@google.com>
Date:   Mon Nov 23 17:57:03 2015 -0800

    Destroy docked divider surface when it's hidden.

    Also includes bunch of small refactorings:
    * destroying surfaces is now fully contained within
    WindowManagerServices and mDestroySurface can be privatized;
    * WMS.isDockedStackResizingLocked can be removed;
    * mScreenCaptureDisabled changes from being SparseArray<Boolean> to
    SparseBooleanArray, which not only avoids boxing but also makes code
    simpler (no need to check for null)

    Bug: 25844096
    Change-Id: I0e5462760ffbc947ce6dc52ef429fa270ffc6786
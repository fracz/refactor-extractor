commit dc6bb24a3c2c830c909523e8d7d999fb556fb77d
Author: Zhijun He <zhijunhe@google.com>
Date:   Thu Dec 3 15:34:34 2015 -0800

    media: improve ImageReader/Writer native memory management

    * Hook up the native allocation registration with ImageWriter, such that GC
    can get some hint when clean up the large memory object.
    * Close all pending images when closing ImageReader. This could avoid native
    mem leaks for some bad app practice. For example, some apps may hold images
    in background service when activity is paused/destroyed, which could cause
    huge native memory leaks even ImageReader is closed.
    * make Image close thread safe: it is possible the clients close the image
    in listener thread and the client main thread.
    * Some minor code refactor to reduce the code duplication.

    Bug: 25088440
    Change-Id: I37d22b52aeb8d2521bf9c702b0f54c05905473e0
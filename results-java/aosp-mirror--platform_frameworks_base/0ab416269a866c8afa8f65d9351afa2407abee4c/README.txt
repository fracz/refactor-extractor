commit 0ab416269a866c8afa8f65d9351afa2407abee4c
Author: Zhijun He <zhijunhe@google.com>
Date:   Thu Feb 25 16:00:38 2016 -0800

    ImageReader/Writer: refactor and cleanup

    Below changes are included:
    * Defer the buffer lock to Image#getPlanes call. This will save quite a bit
    CPU cycles associated with lock buffer if the application doesn't really
    want to access the data.
    * Refactor the code: move some common code to some utility class, and use
    one unified consumer (BufferItemConsumer) in ImageReader native implementation.
    The code refactoring will also make it easier to support non-opaque image
    attach/detach.

    Bug: 22356918
    Bug: 19962027
    Change-Id: I4fb865b0ea3deb6650afc64c32a5906f30e8ccbd
commit af9d667ccf3e24058214cf4cc0a8aa8bc5100e3c
Author: Kenny Root <kroot@google.com>
Date:   Fri Oct 8 09:21:39 2010 -0700

    OBB: rearrange to be entirely asynchronous

    Rearrange structure of MountService handling of OBBs to be entirely
    asynchronous so we don't rely on locking as much. We still need the
    locking to support dumpsys which has been improved to output all the
    data structures for OBBs.

    Added more tests to cover more of the error return codes.

    Oh and fix a logic inversion bug.

    Change-Id: I34f541192dbbb1903b24825889b8fa8f43e6e2a9
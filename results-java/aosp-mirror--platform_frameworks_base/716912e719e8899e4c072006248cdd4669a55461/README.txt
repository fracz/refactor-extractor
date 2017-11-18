commit 716912e719e8899e4c072006248cdd4669a55461
Author: Steve McKay <smckay@google.com>
Date:   Thu May 26 12:04:43 2016 -0700

    Move BandController out of MultiSelectManager.

    Improved dependency injection (elimination of propagating dependencies).
    This change cleans up MultiSelectManager ahead of other improvements.
    Also, cancel band select in response to touch events....else weird things happen.

    Change-Id: I261d928ff7eb5d8a50791d5dd9d7202b324efc54
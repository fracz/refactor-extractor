commit c4d8eb6fb7c88c5c4da38b0b113c24cc4b78c0b7
Author: Romain Guy <romainguy@google.com>
Date:   Wed Aug 18 20:48:33 2010 -0700

    Speedup TextView fades (no more layers required.)

    Also fixes a crash in the drop shadows cache and improves
    drop shadows caching.

    Change-Id: I9c0208a49467f9201d786ae0c129194b8d423923
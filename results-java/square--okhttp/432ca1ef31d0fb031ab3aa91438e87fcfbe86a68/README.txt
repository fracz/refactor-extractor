commit 432ca1ef31d0fb031ab3aa91438e87fcfbe86a68
Author: Neil Fuller <nfuller@google.com>
Date:   Fri Jan 23 17:26:51 2015 +0000

    Reduce flakiness and document reasons for flakiness

    Android has been receiving reports of some tests being flaky
    on what are probably lower-spec devices.

    This introduces delays into tests where sockets are being
    poisoned after the entire response body has been written to
    them *and* where there are follow-up requests.

    This change also improves the documentation for the problematic
    SocketPolicy values.
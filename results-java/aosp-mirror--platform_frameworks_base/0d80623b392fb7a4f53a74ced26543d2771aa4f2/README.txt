commit 0d80623b392fb7a4f53a74ced26543d2771aa4f2
Author: Primiano Tucci <primiano@google.com>
Date:   Mon Jul 28 18:51:45 2014 +0100

    Refactor ActivityManagerService and make WebViewFactory more pedantic.

    This CL adds more robustness to the logic in WebViewFactory, checking
    whether the isolated process did start at all and catching exceptions
    in its java side.
    Also, this addresses the refactor comments received in CL 509840.

    BUG:16403706
    Change-Id: Iaaea6d36142ece6d974c2438259edf421fce9f2e
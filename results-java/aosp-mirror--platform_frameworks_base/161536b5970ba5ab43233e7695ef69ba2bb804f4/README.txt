commit 161536b5970ba5ab43233e7695ef69ba2bb804f4
Author: Primiano Tucci <primiano@google.com>
Date:   Mon Jul 28 18:51:45 2014 +0100

    Cherry pick Refactor ActivityManagerService and make WebViewFactory more pedantic. DO NOT MERGE

    This CL adds more robustness to the logic in WebViewFactory, checking
    whether the isolated process did start at all and catching exceptions
    in its java side.
    Also, this addresses the refactor comments received in CL 509840.

    Original BUG:16403706
    Original Change-Id: Iaaea6d36142ece6d974c2438259edf421fce9f2e

    Bug: 16723226
    Change-Id: Id308f2ffde9b67a3eb4719c7b81b4f46421f0c2e
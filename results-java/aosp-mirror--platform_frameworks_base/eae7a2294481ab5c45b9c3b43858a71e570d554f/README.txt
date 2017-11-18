commit eae7a2294481ab5c45b9c3b43858a71e570d554f
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Tue Jul 25 11:40:56 2017 +0900

    Logging improvements when NetworkCapabilities change

    This patch improves the wtf() logging in updateCapabilities to
    better distinguish between the cases of a changed specifiers, changed
    transports, or changed capabilities. The case of NOT_METERED being added
    or removed is ignored.

    Bug: 63326103
    Test: runtest frameworks-net, runtest frameworks-wifi
    Merged-In: I05c6e78891e1eac658f1cf883223af520a9a4f8f
    Merged-In: I4f6cbc0adb461cef6610460daeba72ca38b8f10c
    Merged-In: I165a8bbe8362100f1e2bb909459fb45b1c68d5ae
    Merged-In: Iec6d92e9a3a12bab87c5adfaf17f776465077060
    Merged-In: I633d6347a7f852c27c03fc96b36ca2a60f70c73c
    Merged-In: I38739184fc0db105bfd3b4c02cce01e803739e5d
    Merged-In: Ia58b877056e2442136cc8b145ac8f4e6560cfc2c

    (cherry pick from commit 683ea489d302b494ab40c0d5dc97d352a59d8aa9)

    Change-Id: Id32ca66068c8ff549627e8e8c0e50897ef928c58
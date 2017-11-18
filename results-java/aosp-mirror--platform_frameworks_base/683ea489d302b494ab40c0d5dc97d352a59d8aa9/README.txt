commit 683ea489d302b494ab40c0d5dc97d352a59d8aa9
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Tue Jul 25 11:40:56 2017 +0900

    Logging improvements when NetworkCapabilities change

    This patch improves the wtf() logging in updateCapabilities to
    better distinguish between the cases of a changed specifiers, changed
    transports, or changed capabilities. The case of NOT_METERED being added
    or removed is ignored.

    Bug: 63326103
    Test: runtest frameworks-net, runtest frameworks-wifi
    Change-Id: I05c6e78891e1eac658f1cf883223af520a9a4f8f
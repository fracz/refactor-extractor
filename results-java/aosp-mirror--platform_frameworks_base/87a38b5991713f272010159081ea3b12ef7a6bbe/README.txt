commit 87a38b5991713f272010159081ea3b12ef7a6bbe
Author: Yasuhiro Matsuda <mazda@google.com>
Date:   Fri Jul 24 22:10:16 2015 +0900

    Do not broadcast VOLUME_STATE_CHANGED before boot has completed.

    This is for avoiding unnecessary launch of processes which receive
    VOLUME_STATE_CHANGED (e.g. com.android.externalstorage) duting boot.
    This change improves boot time by ~200 ms.

    BUG: 22163689
    Change-Id: I76d39695552d49b6188bad8fdae81c5744013396
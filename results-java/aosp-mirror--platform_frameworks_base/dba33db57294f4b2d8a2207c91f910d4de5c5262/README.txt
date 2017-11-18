commit dba33db57294f4b2d8a2207c91f910d4de5c5262
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Thu Mar 23 22:40:44 2017 +0900

    ConnectivityManager: remove obsolete callback types

    This patch also:
      - suppresses callback triggers from ConnectivityService for these
        obsolete callback types.
      - marginally refactors callCallbackForRequest().

    Test: runtest frameworks-net
    Change-Id: Idfb75640732943c599de4975f252f706d21ad594
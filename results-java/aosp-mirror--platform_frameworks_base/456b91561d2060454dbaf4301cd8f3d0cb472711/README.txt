commit 456b91561d2060454dbaf4301cd8f3d0cb472711
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Thu Apr 6 17:22:18 2017 +0900

    DO NOT MERGE ANYWHERE ConnectivityService: move reportNetworkConnectivity to handler

    This patch moves reportNetworkConnectivity onto the handler of
    ConnectivityService.

    This allows:
      - to inspect NetworkAgentInfo on the ConnectivityService handler,
        which is always more correct than doing so on a Binder thread.
      - to improve locking policies around NetworkAgentInfo.

    Test: $ runtest frameworks-net
    Bug: 37119619, 36902662
    Change-Id: I49a765826e65c29a1995242290e5e7544112c94e
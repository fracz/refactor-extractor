commit b577d65825e623a9868664486482ed137b98b504
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Tue Jun 27 15:13:20 2017 +0900

    Nat464Xlat: clat management cleanup

    This patch does some minor refactoring of clat starting/stopping code:
     - remove unused LinkProperties arguments in updateClat
     - remove unused Context argument in Nat464Xlat ctor
     - introduce ensureClatIsStarted and ensureClatIsStopped methods and
       simplify updateClat
     - add clatd to NetworkAgentInfo toString() method
     - clarify some comments

    This changes prepare for moving BaseNetworkObserver callbacks to
    ConnectivityService.

    Bug: 62997041
    Bug: 64571917
    Test: runtest frameworks-net
          manually connected to IPv6 only network and went to test-ipv6.com
    Change-Id: Idb204784614cfe700f73255a7a7b78c5e9ee6eca
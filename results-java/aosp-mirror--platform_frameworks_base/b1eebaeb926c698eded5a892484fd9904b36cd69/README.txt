commit b1eebaeb926c698eded5a892484fd9904b36cd69
Author: Erik Kline <ek@google.com>
Date:   Thu May 21 16:17:12 2015 +0900

    Notify only on loss of provisioning.

    Lots of code refactoring, include:
        - no longer watch for on-link proxies (only routers and DNS servers)
        - keep track of NUD state of neighbors of interest

    Bug: 18581716
    Change-Id: Ia7dbef0690daf54f69ffecefc14e1224fd402397
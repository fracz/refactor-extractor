commit b57578ca4f0fa64406ffad09ad9250391e3bbfa5
Author: Lorenzo Colitti <lorenzo@google.com>
Date:   Fri Jul 1 01:53:25 2016 +0900

    Rewrite lingering.

    The two major changes here are:

    - Move lingering out of NetworkMonitor. The fact that lingering
      is currently its own state in NetworkMonitor complicates the
      logic there: while a network is lingering it cannot be in any
      other state, we have to take care not to leave LingeringState
      for the wrong reason, etc.
    - Instead of keeping a single per-network boolean to indicate
      whether a network is lingered or not, keep a linger timer for
      every request. This allows us to fix various corner-case bugs
      in lingering.

    The changes in behaviour compared to the current code can be seen
    in the unit test changes. Specifically:

    1. Bug fix: when a network is lingered, and a request is added
       and removed to it, the existing code tears the network down
       immediately. The new code just sends another CALLBACK_LOSING
       and resumes lingering with the original timeout.
    2. Bug fix: if cell is unvalidated and wifi comes up and
       validates before cell does (as might happen on boot), the
       existing code immediately tears down cell. The new code
       lingers cell, which is correct because unvalidated cell was
       the default network, so an app might have been using it.
    3. Correctness improvement: always send CALLBACK_AVAILABLE for
       the new network before sending CALLBACK_LOSING. This was not
       really an issue in practice, because the usual flow is:
        - Network A is the default.
        - Network B connects, CALLBACK_AVAILABLE.
        - Network B validates, CALLBACK_LOSING.

    Bug: 23113288
    Change-Id: I2f1e779ff6eb869e62921a95aa9d356f380cf30a
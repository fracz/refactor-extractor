commit a5f39aed845a7d13b929c9004936e60859362f1d
Author: Nikos Michalakis <nikos@netflix.com>
Date:   Wed Jul 13 16:34:37 2016 -0700

    Improve test coverage for PeerAwareInstanceRegistry.

    In preparation for refactoring the getOverriddenInstanceStatus()
    method to remove duplicate code and enable composition of different
    "status overriding rules".
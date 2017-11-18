commit 669f02323f9c7298fdef79b44e1a512549d1411a
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Thu Jun 29 22:58:39 2017 +0900

    Networking unit tests: fix some flaky tests

     - less strict regex for SharedLogTest: the subsecond part of the
       timestamp can have 0, 1, 2 or 3 digits.
     - refactor NetworkStatsServiceTest and NetworkStatsObserversTest to use
       waitForIdleHandler facility of ConnectivityServiceTest.
       NetworkStatsServiceTest was using a flaky custom version of
       waitForIdleHandler.

    Bug: 62918393
    Bug: 32561414
    Test: runtest frameworks-net
    Change-Id: I634acfb5f4fe1bd5267e3f14b9f645edc32d5d12
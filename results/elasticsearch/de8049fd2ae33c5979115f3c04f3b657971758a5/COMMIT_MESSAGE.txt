commit de8049fd2ae33c5979115f3c04f3b657971758a5
Author: Ryan Ernst <ryan@iernst.net>
Date:   Wed Feb 22 20:51:35 2017 -0800

    Tests: Ensure multi node integ tests wait on first node

    When a rest integ test has multiple nodes, each node is supposed to not
    start configuring itself until the first node has been started, so that
    the unicast host information can be written. However, this was never
    explicitly setup to occur, and we were just very lucky with the current
    gradle version and stability of the code always produced a task graph
    that had node0 starting first. With the recent refactorings to integ
    tests, the order has changed. This commit fixes the ordering by adding
    an explicit dependency between the first node and the other nodes.
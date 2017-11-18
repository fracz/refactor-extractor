commit 0e39827d68f533833049b42eda691082b735fcb2
Author: David Phillips <david@acz.org>
Date:   Mon Oct 28 15:44:44 2013 -0700

    Only announce coordinator if configured as one

    This fixes a bug introduced in the CoordinatorModule refactoring
    that causes every node to announce itself as a coordinator.
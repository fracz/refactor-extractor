commit 94dcb3c3fd84a23bbcca75eb24c8ee9aa6ffa9b1
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Tue Oct 4 11:24:12 2016 +0900

    DO NOT MERGE: Do not synchronize boolean reads/writes

    This patch removes the synchronization around the private variable
    mRunning inside of IpReachabilityMonitor and instead qualifeis the field
    as volatile.

    Synchronization is not needed for reads/writes on native fields or
    object references because they are already guaranteed to be atomic.

    Synchronization here was used for enforcing memory visibility across
    concurrent threads indirectly through monitor acquire/release.
    The volatile keyword achieves this in a more explicit way.

    Also, this patch changes the way that probeAll() copies the
    IpReachabilityMonitor's mIpWatchList by temporary holding mIpWatchList
    keys into an ArrayList instead of a more expensive HashSet. Since Java
    HashSet are just degenerated HashMaps, and that key iteration order is
    based on key hash, the iteration order over this temporary collection
    will be consistent for the same mIpWatchList.

    Test: refactoring CL. Existing unit tests still pass.

    (cherry picked from commit b0f1186c034c4df9eb54ed29944d16ce6d7ade56)

    Change-Id: I48d2b4d837a459150cd431b400ec01b87b48c014
commit c3318aa3dd1fe2a117248e2191d68643dee243f0
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Tue Sep 5 13:25:07 2017 +0900

    ConnectivityService: improve wakelock logging

    This patch adds the following wakelock related counters to connectivity
    service dumps included in bug reports:
     - total number of wakelok acquisitions and releases
     - total cumulative wakelock duration
     - longest time the lock was held

    Bug: 65085354
    Test: runtest frameworks-net, also manually dumped connectivity service
          and check new logging

    Merged-In: I8f67750c2eea73abf3d44f7f6df484427a8ea3f9
    Merged-In: I93c0eb7c8add966378647400e11e33765d952345
    Merged-In: Iabe99993001e069b8a8077533bca1fa7fb2f59ba

    (cherry picked from commit 26bcfa19d01758c86a8f43a5b39673cd5866d2f3)

    Change-Id: I4d6bb43110916b440819813b478523546ac5570e
commit 2a6578b7cf3cb73b675d7de4fe9a141ad11b6875
Author: tball <tball@google.com>
Date:   Mon Jan 4 07:20:26 2016 -0800

    Don't test for unresolved ports, because OS X 10.11 now returns all ports as resolved to improve
    socket performance. Previously, iOS had this feature, so our test only ran on OS X.
            Change on 2016/01/04 by tball <tball@google.com>
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=111319490
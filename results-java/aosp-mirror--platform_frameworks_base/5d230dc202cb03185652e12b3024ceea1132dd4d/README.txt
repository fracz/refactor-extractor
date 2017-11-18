commit 5d230dc202cb03185652e12b3024ceea1132dd4d
Author: Daniel Nishi <dhnishi@google.com>
Date:   Mon Apr 10 11:21:26 2017 -0700

    Use the StorageStatsManager in FileCollector.

    This should vastly improve the speed of the FileCollector and
    resolves the null context issue from the previous variant.

    Change-Id: I16a70cd0376511b095b1d7fe1c25e8df95263bc1
    Fixes: 35807386
    Test: Existing tests continue to pass.
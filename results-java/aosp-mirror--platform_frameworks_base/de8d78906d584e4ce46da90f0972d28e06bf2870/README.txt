commit de8d78906d584e4ce46da90f0972d28e06bf2870
Author: Yigit Boyar <yboyar@google.com>
Date:   Mon Sep 22 15:22:45 2014 -0700

    Remove unnecessary waits in TouchUtil's drag

    TouchUtil's drag method tries to sync after sending
    each event which is not necessary. Sync are slow so
    removing them greatly improves test running time.

    Bug: 17323559
    Change-Id: Ia4ed02b2af44da0d821d93d28f963005d9d7ea79
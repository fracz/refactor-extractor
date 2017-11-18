commit 179d6b3f0ccc506b20ae432c84fe5c3d09c10bbf
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Sun Mar 12 17:27:47 2017 -0600

    Deprecate storage "low" and "ok" broadcasts.

    These broadcasts resulted in a terrible user experience where dozens
    of apps would wake up and try deleting everything they possibly can,
    meaning that we'd thrash between showing/hiding the low space
    notification to users.

    Instead, if apps have data that they're okay being purged when the
    system is chronically low on space, we want to strongly encourage
    them to rely on the much-improved getCacheDir() behaviors in OC.

    Test: builds, boots
    Bug: 35406598
    Change-Id: I74abfba1b8d3948363b79f8b66ca0ad60faac756
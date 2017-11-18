commit 75e4a027d874266b302b84c5507ca148c5be7e52
Author: Stig Rohde Døssing <srdo@apache.org>
Date:   Mon Nov 6 18:01:52 2017 +0100

    STORM:2803: Fix leaking threads from Nimbus/TimeCacheMap, slightly refactor Time to use more final fields, replaced uses of deprecated classes/methods and added a few tests.
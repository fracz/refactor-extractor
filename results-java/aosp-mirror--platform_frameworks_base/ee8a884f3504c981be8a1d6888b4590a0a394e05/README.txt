commit ee8a884f3504c981be8a1d6888b4590a0a394e05
Author: Hung-ying Tyan <tyanh@google.com>
Date:   Wed Oct 6 08:33:47 2010 +0800

    SIP: Fix busy authentication loop.

    Add a retry count and give up after two attempts.
    Also stop auto registration when server is unreachable.
    And rename onError() to restartLater() for better readability.

    http://b/issue?id=3066573

    Change-Id: Icfa65c58546a1e2bf8e59e29584a3926c53c479b
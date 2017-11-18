commit a2a29b0c8ed3874753ea2bc6a69545f39a469aa9
Author: Hung-ying Tyan <tyanh@google.com>
Date:   Tue Aug 11 18:09:57 2009 +0800

    Fix order of setting/saving state in VpnService.

    and also refactor code making sure a thread won't grab two locks (which
    may cause deadlocks in some corner cases).
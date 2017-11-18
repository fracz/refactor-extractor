commit 66a6be3a4c955e52ea61e0d35f338983f632abf8
Author: Jack Yu <jackyu@google.com>
Date:   Wed Mar 30 11:14:39 2016 -0700

    Excluded certain APNs (e.g. IMS) from mobile data usage.

    Added not_metered capability to a mobile network if none
    of its associated APN types are metered. Also used not_metered
    capability to determine if a network should be accounted for
    data usage or not instead of using network type, which is
    always MOBILE after refactoring. Will add VT usage support
    in next phase.

    bug: 20888836
    Change-Id: Id692cb856be9a47d0e918371112630128965b1bb
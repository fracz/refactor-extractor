commit 60ec50a850ac7265b662df3c872583b6ef581ef8
Author: Laurent Tu <laurentt@google.com>
Date:   Thu Oct 4 17:00:10 2012 -0700

    Last position improvements for GeofenceManager

    Use LocationManager.getLastPosition() in GeofenceManager instead of
    keeping track of it manually. Keeping track of it in GeofenceManager
    doesn't handle the case where we install a fence, and cross it just
    after that based on the last position before we installed the fence.

    Also shuffle around some code in LocationManagerService to remember the
    last position even if there are no UpdateRecords. This is useful in the
    GeofenceManager for example.

    Bug: 7047435
    Change-Id: Ia8acc32e357ecc2e1bd689432a5beb1ea7dcd1c7
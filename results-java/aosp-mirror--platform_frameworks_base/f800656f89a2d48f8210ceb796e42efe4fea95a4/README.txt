commit f800656f89a2d48f8210ceb796e42efe4fea95a4
Author: Mike J. Chen <mjchen@google.com>
Date:   Fri Sep 30 18:05:44 2011 -0700

    Fix disconnect from wired ethernet issues.

    When a cable was unplugged, we were telling the driver to release the
    ip address so if a cable on a different network was plugged in, it would
    still try to use it's old ip address on the new network, which probably
    didn't work.

    Also, we didn't notify ConnectivityService about the state change in
    the unplug case.  Some of this was done in the interface removed case,
    but we never remove the interface in Tungsten, just unplug.  So refactor
    the common disconnect code into a disconnect() function that's called
    by both the link status change (unplug) and interface removal (only applies
    to things like USB ethernet dongles) cases.

    Change-Id: Ia16648d576187de21b667f026ed3454ac4116bb6
    Signed-off-by: Mike J. Chen <mjchen@google.com>
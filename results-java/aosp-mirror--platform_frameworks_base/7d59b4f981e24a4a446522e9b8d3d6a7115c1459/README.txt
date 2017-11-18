commit 7d59b4f981e24a4a446522e9b8d3d6a7115c1459
Author: Adrian Roos <roosa@google.com>
Date:   Tue May 27 20:01:31 2014 +0200

    Add and improve logged TrustAgent connection events

    Adds events for when a TrustAgentService gets connected
    or is stopped. Also explicitly revokes trust when a
    trust agent gets disconnected, such that it shows up in
    dumpsys.

    Bug: 15281644
    Change-Id: I5875a34da923345683279c1f755d43454ff6318d
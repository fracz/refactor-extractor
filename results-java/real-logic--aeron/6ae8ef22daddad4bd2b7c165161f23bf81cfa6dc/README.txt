commit 6ae8ef22daddad4bd2b7c165161f23bf81cfa6dc
Author: Todd L. Montgomery <todd.montgomery@kaazing.com>
Date:   Thu Aug 7 18:40:11 2014 -0700

    [Java]: refactored client keepalives to 1 per client conductor. Added clientId to correlated messages for use in keepalives.
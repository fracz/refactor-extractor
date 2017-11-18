commit 600f0031331314faacedbdb4d8c3e7e7dee3895a
Author: Jeff Brown <jeffbrown@google.com>
Date:   Thu May 22 17:06:00 2014 -0700

    Improve window orientation listener.

    Take into account whether the device appeared to be overhead facing
    down.  If so, suppress orientation changes until the device is tilted
    back upright again.  This should improve behavior in situations such
    as reading in bed and rolling over to one side.

    Change-Id: I683515e1867626dacc71d90eaacb32e75ab41827
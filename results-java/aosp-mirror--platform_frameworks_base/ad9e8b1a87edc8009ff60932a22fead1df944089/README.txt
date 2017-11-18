commit ad9e8b1a87edc8009ff60932a22fead1df944089
Author: Christopher Tate <ctate@google.com>
Date:   Wed Oct 5 17:49:26 2011 -0700

    Disable db_sample logging

    db_sample logging is thrashing the event log hard, and is currently
    neither maintained nor heeded by anybody on the current release.  It
    can be re-enabled simply by throwing the appropriate static boolean
    to 'true', but for now this should greatly improve the utility of our
    event logs.  (We were seeing a rollover period of 20 minutes or less;
    ideally we want to see the event log run at least half a day before
    rolling.)

    Bug 5419627
    Bug 5104300

    Change-Id: I2125544130aae142974102dbad3b557e49fcd494
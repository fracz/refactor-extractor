commit fc8fa638617efb5695a1f89ea75375faebbe2a40
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Aug 17 16:20:47 2011 -0700

    Fix issue #5128639: SystemUI grows by 10MB after taking a screenshot

    We now do the screenshot in a separate process.

    Also change the recents panel to not use hardware acceleration
    on lower-end devices.  And improve how it gets shown to not
    load all data up-front which results in a long delay when you have
    lots of recents.

    Change-Id: Ia309a90f9939e5405758621b3f7114597bd0c02a
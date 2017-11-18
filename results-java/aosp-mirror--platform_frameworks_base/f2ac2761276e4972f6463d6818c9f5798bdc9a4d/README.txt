commit f2ac2761276e4972f6463d6818c9f5798bdc9a4d
Author: Dianne Hackborn <hackbod@google.com>
Date:   Sat Aug 16 11:44:40 2014 -0700

    Fix issue #17082301: replacePreferredActivity is ignoring userId

    It was being given the argument and just...  ignoring it.

    But the bulk of this change is to make replacePreferredActivity
    better about replacing -- it now detects if the request will not
    make a change and, in that case, just do nothing.

    The reason for this?

    It turns out that each time you install an app, the telephony
    system is calling this function over 20 times to set the default
    SMS app.  This is almost always doing nothing, but before this
    change it means we would re-write packages.xml over 20 times...!

    There are definitely more improvements that can be made here (delaying
    write of packages.xml to allow them to batch together, reducing
    the amount of calls being made), but until then this is a big
    improvement.

    Change-Id: I02c4235b8ecd5c13ef53e65d13c7dc2223719cec
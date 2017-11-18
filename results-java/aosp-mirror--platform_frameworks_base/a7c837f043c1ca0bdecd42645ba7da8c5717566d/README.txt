commit a7c837f043c1ca0bdecd42645ba7da8c5717566d
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Jan 15 16:20:44 2014 -0800

    Add battery power use reporting to batterystats service.

    Move the BatteryStatsHelper class (which computes power use based
    on the raw battery stats) out of the settings app and in to the
    framework.  It is now used by batterystats dump output to print
    the computed power information from its current stats.

    This involved a lot of refactoring of the BatteryStatsHelper code
    to remove all of the UI dependencies.  I also did a bunch of cleanup
    in it, such as making all power computations be in terms of mAh.

    Change-Id: I8ccf2c9789dc9ad34904917ef57050371a59dc28
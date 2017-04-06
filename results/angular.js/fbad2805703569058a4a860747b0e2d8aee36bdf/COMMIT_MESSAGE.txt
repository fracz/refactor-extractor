commit fbad2805703569058a4a860747b0e2d8aee36bdf
Author: Jason Bedard <jason+github@jbedard.ca>
Date:   Sat Oct 4 17:09:22 2014 -0700

    refactor($parse): separate tokenizing vs parsing more
    Fixes `a.b` and `a .b` generating different getterFns
    Fixes `{'': ...}` turning into `{"''": ...}`
    Fixes `{.: ...}` parsing as `{'.': ...}` instead of throwing
    Fixes #9131
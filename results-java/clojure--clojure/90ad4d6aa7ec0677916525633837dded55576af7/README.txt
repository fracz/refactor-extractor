commit 90ad4d6aa7ec0677916525633837dded55576af7
Author: Alex Miller <alex.miller@cognitect.com>
Date:   Fri Mar 10 08:47:36 2017 -0600

    CLJ-1860 Make -0.0 hash consistent with 0.0

    0.0 and -0.0 compared equal but hashed differently. The patch
    makes -0.0 (double and float) hash to 0, same as 0.0. Also,
    the patch restructures hasheq to cover just long and double and
    moves the rest to a helper function to improve inlining.

    Signed-off-by: Stuart Halloway <stu@cognitect.com>
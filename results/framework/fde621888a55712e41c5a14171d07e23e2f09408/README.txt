commit fde621888a55712e41c5a14171d07e23e2f09408
Author: Andrew <browner12@gmail.com>
Date:   Mon Jul 3 14:34:19 2017 -0500

    do not reuse variable name in foreach loop (#19883)

    due the scoping of PHP foreach loops, this works fine. however it can be a little confusing from a readability standpoint.

    I just rename the value of the first loop so there is no question which variable we are referring to.

    also, if we were to ever use the `$aliases` variable after this loop, it would not contain what we expected.
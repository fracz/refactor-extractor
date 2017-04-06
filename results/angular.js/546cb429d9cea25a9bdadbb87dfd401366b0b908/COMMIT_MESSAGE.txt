commit 546cb429d9cea25a9bdadbb87dfd401366b0b908
Author: Igor Minar <igor@angularjs.org>
Date:   Fri Apr 18 16:20:02 2014 -0700

    perf($interpolate): speed up interpolation by recreating watchGroup approach

    This change undoes the use of watchGroup by code that uses $interpolate, by
    moving the optimizations into the $interpolate itself. While this is not ideal,
    it means that we are making the existing api faster rather than require people
    to use $interpolate differently in order to benefit from the speed improvements.
commit 50b1ba0083115c4fb578f13e0b94c6d80b5b1841
Author: Rajeev Sharma <rdsharma@google.com>
Date:   Wed Aug 15 15:16:52 2012 -0700

    Fisheye filter:  approx version, general performance improvements

    Lightly refactor original filter and rework math for performance.  Add approx
    version which uses approx_rsqrt, approx_sqrt, and the new approx_atan function.

    Change-Id: I796d50da05c1684167696ea4da37d7d75fc78626
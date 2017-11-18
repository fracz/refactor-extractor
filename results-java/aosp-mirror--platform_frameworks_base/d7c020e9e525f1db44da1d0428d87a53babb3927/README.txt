commit d7c020e9e525f1db44da1d0428d87a53babb3927
Author: Raph Levien <raph@google.com>
Date:   Wed Feb 4 20:09:03 2015 -0800

    Interval tree for SpannableStringBuilder

    This CL greatly improves the speed of SpannableStringBuilder by storing
    the spans as an interval tree with binary search.

    Bug: 7404182
    Change-Id: I2540b2cfe8aef128337e63829479660ba51e62b5
commit aa67f684ff43c81e3280c846245ec6ebe907787e
Author: John Reck <jreck@google.com>
Date:   Tue Sep 20 14:24:21 2016 -0700

    Fix a bunch of repeated reads of a ro.* property

    SystemProperties.get() is not particularly fast,
    especially if a string is returned. Since ro.* values
    are unable to be changed, there's no need to
    continously re-query them. Cache the value at
    static init time to trivially fix this.

    Test: refactoring CL.
    Change-Id: Iccb021d3cb2ba3a4a1d0048ddec6811bb7409eec
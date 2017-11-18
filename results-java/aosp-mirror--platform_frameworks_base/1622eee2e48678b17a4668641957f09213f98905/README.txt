commit 1622eee2e48678b17a4668641957f09213f98905
Author: Mattias Petersson <mattias.petersson@sonyericsson.com>
Date:   Tue Dec 21 10:15:11 2010 +0100

    Improve performance of WindowState.toString()

    This fix improves the performance by caching the string that should
    be returned, and reuse it next time if possible.
    This will make it faster to switch between activities, approximately
    half the time to create the new view when changing from landscape to
    portrait. Also, the time for starting a new application is be reduced
    as WindowState.toString is being called thousands of times in this
    case.

    Change-Id: I2b8b9bc1e251d1af43b6c85f049c01452f2573a2
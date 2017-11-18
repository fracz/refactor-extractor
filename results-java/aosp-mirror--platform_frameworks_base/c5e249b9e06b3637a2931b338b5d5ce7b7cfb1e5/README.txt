commit c5e249b9e06b3637a2931b338b5d5ce7b7cfb1e5
Author: songjinshi <songjinshi@xiaomi.com>
Date:   Wed Jul 27 20:36:46 2016 +0800

    Fixes the system server crash issue caused by uncatched exception.

    The restat method of the StatFs may throw an IllegalArgumentException,
    so we must to catch it and throw an IOException for the caller
    of the trimToFit(),this fix can improve system stability.

    https://code.google.com/p/android/issues/detail?id=218359

    Change-Id: I54a2f569eea67d3ab628944e3586ca918ec70283
    Signed-off-by: songjinshi <songjinshi@xiaomi.com>
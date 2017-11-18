commit 6217e37d30042fd78d17a8d5145f578279d60808
Author: Tobias Thierer <tobiast@google.com>
Date:   Tue Oct 17 20:26:20 2017 +0100

    Framework: Prefer android.system.Os over libcore.io.Libcore.os

    This is a pure refactoring with no a behavior change other than
    that these calls now go through android.system.Os, which immediately
    delegates to Libcore.os.

    This is a first step towards separating framework (via
    android.system.Os) vs. libcore (via Libcore.os) clients of these
    low level APIs. Separating these is a prerequisite towards moving
    the API parts of android.system into framework, and moving the
    rest into a different package in libcore.

    Test: Treehugger
    Bug: 67901714

    Change-Id: Ifd8349ec5416e5693f40dba48fdf2bef651b7d81
    Merged-In: Ifd8349ec5416e5693f40dba48fdf2bef651b7d81
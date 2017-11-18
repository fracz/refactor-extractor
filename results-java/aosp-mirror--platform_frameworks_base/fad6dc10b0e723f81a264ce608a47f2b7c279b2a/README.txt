commit fad6dc10b0e723f81a264ce608a47f2b7c279b2a
Author: Calin Juravle <calin@google.com>
Date:   Thu Aug 3 19:48:37 2017 -0700

    Use PackageUseInfo in DexOptimizer

    Pass the PackageUseInfo directly to DexOptimizer and use it to detect if a
    package is used by other apps. Move the usage checks closer to dexopt so
    that they can be easily adapted when we add usage info for each of the
    app's code paths separately.

    This is a refactoring CLs to reduce the size and complexity of the
    upcoming CLs which record the usage info for each of the application
    splits.

    (cherry picked from commit 3b74c41776da66562a68b12a0fed8d20b6952868)

    Bug: 64124380
    Test: runtest -x
    services/tests/servicestests/src/com/android/server/pm/dex/*

    Merged-In: I8031590cdaff81ab1792ca19baddb6cb36dc021d
    Change-Id: I8031590cdaff81ab1792ca19baddb6cb36dc021d
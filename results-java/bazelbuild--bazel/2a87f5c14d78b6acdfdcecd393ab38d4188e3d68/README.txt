commit 2a87f5c14d78b6acdfdcecd393ab38d4188e3d68
Author: Liam Miller-Cushon <cushon@google.com>
Date:   Sat Aug 6 01:36:08 2016 +0000

    Fix handling of jdeps files in android_* rules

    If header compilation is enabled, the compile-time dependency information is
    produced by the header compilation action. The android rules were incorrectly
    using the dependency information from the regular compilation, which defeated
    the critical path improvement from header compilation.

    --
    MOS_MIGRATED_REVID=129505189
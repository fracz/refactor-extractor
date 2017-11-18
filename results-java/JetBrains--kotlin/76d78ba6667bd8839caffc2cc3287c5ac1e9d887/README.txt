commit 76d78ba6667bd8839caffc2cc3287c5ac1e9d887
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Thu May 19 21:07:49 2016 +0300

    Remove LazyResolveTestUtil, refactor usages to use JvmResolveUtil

    LazyResolveTestUtil.resolve by now had almost exactly the same code as
    JvmResolveUtil.analyze.

    LazyResolveTestUtil.resolveLazily with 3 parameters was only used in
    LoadBuiltinsTest, inline it there.

    Move LazyResolveTestUtil.getTopLevelPackagesFromFileList to
    AbstractDiagnosticsTest, the only place where it was used
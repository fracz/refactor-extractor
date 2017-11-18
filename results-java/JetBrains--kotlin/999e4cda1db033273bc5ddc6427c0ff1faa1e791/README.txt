commit 999e4cda1db033273bc5ddc6427c0ff1faa1e791
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Fri Jun 16 19:25:57 2017 +0300

    Compute module mappings eagerly in JvmPackagePartProvider, refactor

    Previously we traversed all notLoadedRoots on each request for package
    parts with the given package FQ name. Since notLoadedRoots might contain
    a lot of roots (which never transition into "loadedModules" because e.g.
    they are not Kotlin libraries, but just Java libraries or SDK roots with
    the META-INF directory), this was potentially hurting performance. It
    seems it's more optimal to compute everything eagerly once
    JvmPackagePartProvider is constructed.

    Another problem with the previous version of JvmPackagePartProvider was
    that it did not support "updateable classpath" which is used by REPL and
    kapt2, it only used the initial roots provided in the
    CompilerConfiguration. In REPL specifically, we would thus fail to
    resolve top-level callables from libraries which were dynamically added
    to the execution classpath (via some kind of a @DependsOn annotation).
    In the new code, JvmPackagePartProvider no longer depends on
    CompilerConfiguration to avoid this sort of confusion, but rather relies
    on the object that constructed it (KotlinCoreEnvironment in this case)
    to provide the correct roots. This is also beneficial because the
    computation of actual VirtualFile-based roots from the ones in the
    CompilerConfiguration might get trickier with modular Java 9 roots
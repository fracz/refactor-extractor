commit d65af8f951c5ffacdb32ff70ecc4dc42361fa1dd
Author: Denis Zharkov <denis.zharkov@jetbrains.com>
Date:   Mon Apr 17 12:12:00 2017 +0300

    Introduce custom Java model implementation for class-files

    It's only used for CLI compiler, and it should improve performance
    of loading Java descriptors from class-files

    For IntelliJ, it leads to 10-15% percent speedup of Kotlin Builder

    Before this change, we were using a Java model based on Java PSI that
    also read class files, but do it less effectively since it performs
    some extra work, that we don't need, e.g. eagerly reading all
    the inner classes
commit 4f440fcbb9f92723ac8ae227534bbde51d5be2d9
Author: Phillip Webb <pwebb@gopivotal.com>
Date:   Tue Jul 29 14:12:52 2014 -0700

    Java 7 ClassLoader performance improvements

    Use Java 7 `registerAsParallelCapable` and `getClassLoadingLock` methods
    when possible. This should improve performance when running on JDK 7+
    whilst still remaining JDK 6 compatible.

    Closes gh-1284
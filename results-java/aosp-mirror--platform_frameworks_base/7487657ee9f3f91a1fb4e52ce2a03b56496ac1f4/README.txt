commit 7487657ee9f3f91a1fb4e52ce2a03b56496ac1f4
Author: Fyodor Kupolov <fkupolov@google.com>
Date:   Mon Feb 23 17:14:45 2015 -0800

    Extracted a separate class to run dexopt on packages

    performDexOptLibsLI and related methods were extracted to PackageDexOptimizer
    class. Minor refactoring of PackageManagerService.

    This is a non-functional change. It should simplify further work to allow
    storing OAT files inside package dir.

    (cherry picked from commit eeea67b8c3678d882d3774edc41242c63daa60fa)

    Change-Id: I3494a2da70605362bb6fb4625ffbee1cbe1cd457
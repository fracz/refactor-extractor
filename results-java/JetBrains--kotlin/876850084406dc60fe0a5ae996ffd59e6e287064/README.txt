commit 876850084406dc60fe0a5ae996ffd59e6e287064
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed Jun 28 01:08:05 2017 +0300

    Fix incorrect behavior and refactor JvmModuleAccessibilityChecker

    Previously we assumed that a symbol is accessible if its containing
    package is exported by module-info.java. Which was obviously wrong and
    could lead to a situation where a symbol would be incorrectly accessible
    if a usage module has a dependency on the symbol's module in IDEA
    project terms, but does not require it in its module-info.java

     #KT-18598 In Progress
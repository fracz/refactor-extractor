commit 05fe2f39158b5b7b2d93449e0643807706b0c8c6
Author: Konstantin Kolosovsky <konstantin.kolosovsky@jetbrains.com>
Date:   Mon Mar 13 18:41:47 2017 +0300

    Optimize "ModuleDefaultVcsRootPolicy.getDefaultVcsRoots()" performance

    Use "Set" for uniqueness checking (instead of "ArrayList.contains()")
    which improves performance in case of large modules/content roots number
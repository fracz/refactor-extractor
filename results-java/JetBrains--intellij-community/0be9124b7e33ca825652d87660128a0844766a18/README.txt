commit 0be9124b7e33ca825652d87660128a0844766a18
Author: Sergey Simonchik <sergey.simonchik@jetbrains.com>
Date:   Tue May 30 20:17:28 2017 +0300

    improve DirectoriesScope.contains performance: avoid creating&populating SmartHashSet for each method invocation (IDEA-CR-21452)
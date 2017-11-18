commit b080694d1042ff52429944735afd39699fa8caba
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Tue Nov 24 14:20:02 2015 +0300

    [hg]:  improve branch heads information structural type to support heads order

    * heads in branch file store in the priority order: tha last head related to main head and related to "branch name" reference when update, pull, etc command performed.
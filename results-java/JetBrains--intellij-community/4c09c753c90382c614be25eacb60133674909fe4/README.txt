commit 4c09c753c90382c614be25eacb60133674909fe4
Author: Konstantin Kolosovsky <konstantin.kolosovsky@jetbrains.com>
Date:   Mon Mar 6 19:17:26 2017 +0300

    svn: Walk through unversioned svn file subtrees using "VirtualFile" api

    Using "VirtualFile" api (instead of "java.io.File" api) improves
    performance of processing large unversioned file subtrees while
    detecting svn statuses

    IDEA-140038
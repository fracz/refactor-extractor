commit 73f0ee24136d28a5fc525c0706700f49f36a3eb4
Author: Sébastien Lavoie <github@lavoie.sl>
Date:   Tue Jun 10 13:36:33 2014 -0400

    [DI][Routing] recursive directory loading

    Issue #11045

    For now, the Routing DirectoryLoader requires the type `directory`
    to be specified so it does not conflict with `AnnotationDirectoryLoader`.
    However, this could be refactored.
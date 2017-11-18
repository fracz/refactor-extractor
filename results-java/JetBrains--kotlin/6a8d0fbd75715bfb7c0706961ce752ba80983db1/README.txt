commit 6a8d0fbd75715bfb7c0706961ce752ba80983db1
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Mon Sep 21 18:22:14 2015 +0300

    Introduce infrastructure to separate string table from metadata on JVM

    Nothing especially helpful happens here, this is only a big refactoring
    introducing a separate string array for the string table, which is currently
    always empty, but will contain actual strings soon
commit f5b730723a4da8ba04aedc796728fd1a2b08cff5
Author: Maxim.Mossienko <Maxim.Mossienko@jetbrains.com>
Date:   Fri Aug 4 13:23:34 2017 +0200

    refactoring: introduce ResourceHandle, superclass of Handle

    ResourceHandle counts refs and disposes resource when count reaches zero
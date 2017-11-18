commit fd2840cfad6dd75f4dabbb6f560193fbad5339ee
Author: Alexander Koshevoy <Alexander.Koshevoy@jetbrains.com>
Date:   Sat Sep 5 16:20:08 2015 +0300

    PY-15476 Multiple changes to refactor Docker python remote run support

    Multiple changes:
     - classes that implement interaction with Docker Remote API via docker-java library moved to python-docker module;
     - "remoteProcessStarterManager" extension point added with Docker and SSH implementations;
     - Docker SSH credentials elimination continued.
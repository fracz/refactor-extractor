commit 825c88b08b4cb676126cb67c9857bd4335b61913
Author: Michael Golubev <michael.golubev@jetbrains.com>
Date:   Wed Jul 19 01:25:08 2017 +0200

    IDEA-176049 - Docker Tab: Freeze with 100+ containers/images

    - ServerConnectionImpl
    - - getDeployments results are cached
    - - still called N times for N nodes
    - - but at least not leading to N*N*logN comparator calls
    - + refactoring: code related to deployments extracted
    - DeploymentImpl: avoid running arbitrary code under 2 nested (!) locks
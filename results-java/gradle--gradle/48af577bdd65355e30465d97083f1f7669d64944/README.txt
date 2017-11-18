commit 48af577bdd65355e30465d97083f1f7669d64944
Author: Daz DeBoer <darrell.deboer@gradleware.com>
Date:   Wed Jan 21 21:27:14 2015 -0700

    Don't use Maven Ant tasks for deploying to Maven repositories

    - Inlined core task classes and refactored for simplicity
    - Merged custom Ant tasks into inlined tasks
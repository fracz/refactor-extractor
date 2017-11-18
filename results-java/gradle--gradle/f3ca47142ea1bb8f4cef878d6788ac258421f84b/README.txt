commit f3ca47142ea1bb8f4cef878d6788ac258421f84b
Author: Daz DeBoer <daz@gradle.com>
Date:   Sat Sep 3 22:16:56 2016 -0600

    Prevent concurrent execution of an included build

    Previously, our dumb cycle detection was preventing concurrent execution
    of included builds. Now that this is improved, we need to block build
    requests for a build that is current executing.

    Combined with the improved cycle detection, this fixes a bug where
    execution with `--parallel` could result in a cyclic dependency
    failure, when no such cycle exists.
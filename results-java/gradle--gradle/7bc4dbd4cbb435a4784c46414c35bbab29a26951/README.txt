commit 7bc4dbd4cbb435a4784c46414c35bbab29a26951
Author: Daz DeBoer <daz@gradle.com>
Date:   Tue Apr 26 20:17:45 2016 -0600

    Remove use of synthetic task for wiring composite task dependencies

    When an external dependency is replaced with a 'project' dependency in a composite,
    the tasks that build the required artifacts in the producer build must be executed
    prior to the use of the artifact files. Previously, this was done by way of a
    synthetic task added to the task execution graph, in a similar way to task
    added to the graph for regular project dependencies in a multiproject build.

    Beside producing ugly and confusing task output, this mechanism was not conducive
    to further improvements to resolution of project dependencies in a composite (e.g. parallelization).
    Execution of tasks to build dependency artifacts is now wired directly into the
    `CompositeProjectDependencyResolver`.
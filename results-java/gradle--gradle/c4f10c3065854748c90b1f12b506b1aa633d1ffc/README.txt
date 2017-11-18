commit c4f10c3065854748c90b1f12b506b1aa633d1ffc
Author: Daz DeBoer <daz@gradle.com>
Date:   Mon May 8 07:06:51 2017 -0600

    Create a task reference for building included build artifacts

    Instead of executing included builds on-demand when resolving the
    artifacts, we now create an `IncludedBuildTaskReference` for each of
    these artifacts and add it to the task execution graph.

    This improves behaviour by executing tasks for an included build
    as a separate task dependency of the tasks that require the artifacts,
    rather then performing this execution when resolving the inputs to the
    task requiring the artifacts.

    The `ProjectArtifactBuilder` will still attempt to build included-build
    artifacts on-demand: this behaviour is required to support plugin
    development using a composite build, by building plugin artifacts when
    resolving the buildscript classpath. This could eventually be replaced
    by a separate task execution graph for constructing the buildscript
    classpath, building on the current execution of the `buildSrc` build.
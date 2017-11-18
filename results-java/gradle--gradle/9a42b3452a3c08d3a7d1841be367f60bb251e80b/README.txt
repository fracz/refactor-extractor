commit 9a42b3452a3c08d3a7d1841be367f60bb251e80b
Author: Daz DeBoer <daz@gradle.com>
Date:   Fri Aug 26 18:45:13 2016 -0600

    Introduce a separate `BuildIdentifier` for composites

    ProjectComponentIdentifier and ProjectComponentSelector now each have
    a BuildIdentifier instance to identify the build that contains the
    project. You can create a project identifier for the 'current' build
    or from an included build.

    At this stage, any ProjectComponentIdentifier for the 'current'
    build has a 'current' BuildIdentifier, with name = `null`.
    This will be improved to be a true build identifier.
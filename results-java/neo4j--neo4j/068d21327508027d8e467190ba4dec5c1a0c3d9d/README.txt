commit 068d21327508027d8e467190ba4dec5c1a0c3d9d
Author: fickludd <johan.teleman@neotechnology.com>
Date:   Thu Oct 19 13:00:02 2017 +0200

    Move interpreted runtime into it's own module

    This is a large refactoring with the main purpose of reducing
    dependencies between the interpreted runtime and the rest of
    cypher. In doing this two other modules had to be created:

      neo4j-runtime-util
        Holds common runtime classes and interfaces which are not
        tied to the interpreted or any other runtime

      neo4j-cypher-planner-spi-3.4
        Holds planner SPI interfaces. Some of these are also used by
        runtime classes, and this design limits the dependencies
        between planner and runtime implementations.

    Not that non-versioned modules are free to depend on versioned modules,
    but the reverse would break cypher versioning.

    Following this refactoring, it should be possible to stop all
    runtimes from depending of neo4j-cypher. That has not been attempted
    here though.
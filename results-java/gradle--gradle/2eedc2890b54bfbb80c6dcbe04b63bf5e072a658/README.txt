commit 2eedc2890b54bfbb80c6dcbe04b63bf5e072a658
Author: Adam Murdoch <adam@gradle.com>
Date:   Mon Oct 23 11:03:08 2017 +1100

    Changed the dependency resolution engine to understand to some degree the connections between the various modules of a component that is published across multiple modules, such as a C++ library or executable.

    The publishing plugins no longer insert an artificial dependency between the main module and the other modules. The module metadata parser does this instead when it reads the metadata. This implementation is intentionally dumb and can be improved later without requiring changes to the metadata.
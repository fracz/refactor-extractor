commit 9046dcdff0c7cd863cfd950a5dc61c21b508335a
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Fri Apr 12 12:56:26 2013 +0200

    improvements to Groovy class path inference
    - fail early rather than returning empty configuration
    - don't wrap `groovy` configuration in LazilyInitializedFileCollection
    - removed some tests that are now covered elsewhere
commit 2ce8446a0dc3af249106f84dfeb9dc40e8785c3b
Author: lryan <lryan@google.com>
Date:   Mon Jun 2 14:43:36 2014 -0700

    Large refactor to:
    - Introduce 'Channel' & 'Call' interfaces
    - Unify the surfaces for the prototype generated stubs
    - Lighten dependency on MessageLite outside of generated code (see Marshaller)
    - Bridge Channel to Session pending Transport interface rewrite
    - Update all tests to new interfaces

    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=68407406
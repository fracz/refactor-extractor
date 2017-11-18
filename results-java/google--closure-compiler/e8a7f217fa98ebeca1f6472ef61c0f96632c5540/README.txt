commit e8a7f217fa98ebeca1f6472ef61c0f96632c5540
Author: mknichel <mknichel@google.com>
Date:   Thu Jan 29 14:00:54 2015 -0800

    Add tests for the MovePropertiesToConstructor refactoring, and fix the bugs in the refactoring to make the tests assert the right behavior. This refactoring is now in good enough shape to be used by people to make code @struct compatible.

    The code still isn't perfect and could still be cleaned up, but I will do that in a future CL.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=85099030
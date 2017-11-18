commit 97be5e92b907bf452731a01306c40193e2374173
Author: mthvedt <mthvedt@google.com>
Date:   Tue Mar 3 16:20:11 2015 -0800

    Fork annotation processing into its own step.

    This is part of a refactor to gather all the GenerationUnits before processing begins.

    The commented out lines in J2ObjCTest.java succeed on Mac, but mysteriously the annotation processing fails on my PC and in []. The same annotation processing (with precisely the same args) succeeds when run with Javac on PC. The same result happens when only this test is patched against head. Per tball, proceeding and circling back later since nothing in annotation processing is broken now that wasn't broken before.
            Change on 2015/03/03 by mthvedt <mthvedt@google.com>
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=87660001
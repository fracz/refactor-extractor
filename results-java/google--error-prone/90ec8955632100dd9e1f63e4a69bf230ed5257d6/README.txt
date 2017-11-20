commit 90ec8955632100dd9e1f63e4a69bf230ed5257d6
Author: bangert <bangert@google.com>
Date:   Thu Feb 11 09:55:24 2016 -0800

    Compile all affected files at once in BugCheckerRefactoringTestHelper

    Allows testing refactorings that look at other compilation units (i.e.
    imported class names).

    Add a expectUnchanged function to make including helper files less
    verbose.

    RELNOTES: none
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=114447929
commit 3943a1d0210a9493da3844940a3b976cb3b0f6e4
Author: mlourenco <mlourenco@google.com>
Date:   Thu Mar 10 09:39:50 2016 -0800

    Set CompilerOptions.setTrustedStrings to true, thus preventing characters (like = and %) in strings from getting JS-escaped on refactorings.

    When the compiler is run from the command line trusted strings is set to true, but the default is false.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=116877582
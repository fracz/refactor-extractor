commit 05a0236e0737fc21abec8b05afd86b260cd1f9ed
Author: cpovirk <cpovirk@google.com>
Date:   Thu Sep 4 07:47:36 2014 -0700

    Smooth over some rough edges in the test:

    - Don't put the method name in the error message. This not only encourages less verbose expectations but also eliminates the confusing errors that resulting from accidentally writing "triggerNullnessChecker" instead of "triggerNullnessCheckerOnInt."
    - Force callers who pass primitive to explicitly choose whether to autobox. In the process, discover that a few of our tests weren't testing what I thought, and improve the analysis to get most of them right.
    - Create a third file of tests. I thought I might be hitting the javac error limit, though I may be wrong about that. Ultimately the tests should be reorganized further, but this is a start in reducing the number of errors to wade through during debugging.

    (Also, simplify visitNode, and increase the symmetry between visitEqualTo and visitNotEqual.)
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=74776547
commit 4e2d298fe7a3be0728cedf7aa3d175d4c94815cf
Author: JDGrimes <jdg@codesymphony.co>
Date:   Tue Jan 10 10:35:01 2017 -0500

    Improve performance of function restriction sniffs

    This abstract sniff is extended by quite a few of our sniffs, and so it has a major impact on the performance of the ruleset as a whole.

    Here we make two improvements:

    1. We use `isset()` instead of `in_array()`.
    2. We don't call `findPrevious()` a second time unless we actually need the information. We only need to check if the namespace is top-level if the function is prefixed with `\\`, the namespace separator.

    This seems to improve the performance of these sniffs by as much as 25%.
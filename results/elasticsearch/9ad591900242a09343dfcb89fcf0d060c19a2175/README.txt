commit 9ad591900242a09343dfcb89fcf0d060c19a2175
Author: Jason Tedor <jason@tedor.me>
Date:   Sat Feb 27 22:30:29 2016 -0500

    Refactor bootstrap checks

    This commit refactors the bootstrap checks into a dedicated class. The
    refactoring provides a model for different limits per operating system,
    and provides a model for unit tests for individual checks.

    Closes #16844
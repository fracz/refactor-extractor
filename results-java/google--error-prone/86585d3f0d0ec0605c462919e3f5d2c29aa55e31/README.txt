commit 86585d3f0d0ec0605c462919e3f5d2c29aa55e31
Author: eaftan <eaftan@google.com>
Date:   Mon Jul 6 15:13:28 2015 -0700

    Refactor documentation generator.

    This is in preparation for extracting examples from code inlined into
    tests. It creates a value type to store information about the example
    and refactors the existing documentation generator to use that instead
    of accessing the example file directly.

    It also separates positive and negative examples, and prints the positive
    ones first.

    RELNOTES: None
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=97617255
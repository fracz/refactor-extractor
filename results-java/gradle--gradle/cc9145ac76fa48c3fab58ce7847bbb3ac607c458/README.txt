commit cc9145ac76fa48c3fab58ce7847bbb3ac607c458
Author: Alexander Landau <alandau@palantir.com>
Date:   Wed Sep 16 14:48:42 2015 -0700

    GRADLE-3112 Fix JUnit test filtering interaction with @Parameterized.

    - JUnit filtering as applied by the ParentRunner checks each
      Description in the test tree; if any Description fails to pass
      the test, its children are ignored.
      - Our MethodNameFilter did not take this into account.
      - @Parameterized happens to be a case that exposes this; it
        includes Descriptions with names like "[0]" that don't match
        any reasonable test method name filter.
      - The fix is to have the filter allow non-leaf Descriptions to
        pass if any child passes. Filtering will still happen properly
        at the lowest level.
        - JUnit's CategoryFilter takes the same approach.
    - Minor improvements to related TestSelectionMatcher code.
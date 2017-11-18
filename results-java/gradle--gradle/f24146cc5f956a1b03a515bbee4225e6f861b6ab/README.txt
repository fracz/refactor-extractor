commit f24146cc5f956a1b03a515bbee4225e6f861b6ab
Author: Chris Beams <chris@gradle.com>
Date:   Wed Feb 17 12:41:52 2016 +0100

    Add @Override where missing in production software model sources

    Prior to this change, the affected submodules had 2044 occurrences of
    the @Override annotation. With this commit, there are now 3492
    occurrences. This suggests some divergence in IDE settings, either
    across developers, across time, or both. At the moment, it appears that
    IDEA (15 CE) is configured correctly to add @Override automatically.

    This same refactoring should probably be done globally acrosse the
    Gradle codebase, but has been constrained here to software model-related
    submodules (a) because it is what the author is responsible for and (b)
    because significant refactoring of type hierarchies is underway there
    right now--the kind of work most likely to benefit from the compiler
    checks that proper use of @Override affords.

    Should this same refactoring be applied globally, it would be worth
    looking into enforcing consistent use of @Override via checkstyle or
    similar at the same time.
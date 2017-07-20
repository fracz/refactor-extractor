commit 113c5684716f117650f75bd3f996931c3d7460f2
Author: Mark Story <mark@mark-story.com>
Date:   Sun May 31 09:29:25 2015 -0400

    Fix backwards compatibility for typehints.

    In refactoring the rules checker trait I broke typehints around
    buildRules(). This adds a compatible implementation that won't trigger
    strict errors.
commit 4d104048dc5f6233f7d1b11b3e2cb34baae5fa6f
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Thu Nov 28 15:29:54 2013 +0100

    Refactors state machine contexts into interfaces

    This commit is a needed refactoring to make state machines more testable.
     It removes all dependencies from all state machine contexts that before
     had them interconnected and all tangled up, requiring multiple levels
     of mocking. Instead, now they are all implemented by MultiPaxosContext
     which takes care to factor out common needed functionality. While the
     diff is large, it does not represent too big of a change.
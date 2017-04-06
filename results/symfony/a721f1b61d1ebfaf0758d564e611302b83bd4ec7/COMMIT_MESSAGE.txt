commit a721f1b61d1ebfaf0758d564e611302b83bd4ec7
Merge: 0bfeda6 b7c2d3d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Apr 18 09:22:19 2012 +0200

    merged branch willdurand/propel-type-guesser (PR #3953)

    Commits
    -------

    b7c2d3d [Propel1] Added tests for guessType() method
    897a389 [Propel1] Added tests for the PropelTypeGuesser
    cffcdc9 Improve the TypeGuesser to match the latest Sf2.1

    Discussion
    ----------

    [Form] Propel type guesser

    Thanks to @vicb, the PropelTypeGuesser has been updated. I've added unit tests to prove his improvement, and everything is ok from my point of view.

    ---------------------------------------------------------------------------

    by willdurand at 2012-04-15T16:38:09Z

    Well, I made the changes, but I really don't care about fixing these comments.

    To write `assertNull` doesn't improve readabilty, as I expect to read the returned value in the test. And, it's better to read `null` than to read the assertion method. Moreover, that makes the test suite inconsistent, as you are not able to read each tests the same way :)

    ---------------------------------------------------------------------------

    by vicb at 2012-04-15T17:20:01Z

    Great ! thanks @willdurand
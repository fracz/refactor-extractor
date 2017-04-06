commit 839dd2aaa4b6209e3916935481c6df5c873bb3b6
Merge: 3026287 541888d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Dec 5 10:22:47 2011 +0100

    merged branch drak/patch-1 (PR #2784)

    Commits
    -------

    541888d [EventDispatcher] Added missing object destruction in test tearDown() and removed duplicated tests.

    Discussion
    ----------

    [EventDispatcher] Merge two test cases into one.

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -

    Ref https://github.com/symfony/symfony/commit/667c24d73da03b6076609c8d616e982ecf493cdd#commitcomment-766966

    ---------------------------------------------------------------------------

    by stof at 2011/12/04 19:43:32 -0800

    ``testGetName`` could also be removed as it is already tested by ``getSetName``, and same for the event dispatcher.

    Basically, you cannot test the setter without testing the associated getter so no need for 2 tests for them as it is forced to be duplicate for part of them (and no need to use so long names IMO)

    ---------------------------------------------------------------------------

    by drak at 2011/12/05 00:49:22 -0800

    I've refactored the test to remove duplication, I pushed as a rebase so the new commit is 541888d referencing the same discussion.
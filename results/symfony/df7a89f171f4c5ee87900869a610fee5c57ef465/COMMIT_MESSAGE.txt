commit df7a89f171f4c5ee87900869a610fee5c57ef465
Merge: 8a1f0a0 5ff741d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Sep 28 08:54:42 2015 +0200

    minor #15942 [Security] Improve AbstractVoter tests (WouterJ)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [Security] Improve AbstractVoter tests

    Applying the improved tests from https://github.com/symfony/symfony/pull/15932 into the oldest possible branch.

    Merge conflicts from 2.7 into 2.8 caused by this PR do not need to be done carefully, I'll create a new PR for 2.8 updating the tests as soon as these changes are merged up.

    | Q             | A
    | ------------- | ---
    | Fixed tickets | -
    | License       | MIT

    Commits
    -------

    5ff741d Readd the correct tests
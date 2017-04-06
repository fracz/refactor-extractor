commit 854015265c24a7524d95d4ee57c59f2836d60dc1
Merge: 89f7b5e 77fd70b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Oct 11 15:44:22 2012 +0200

    merged branch bamarni/console (PR #5689)

    This PR was merged into the 2.1 branch.

    Commits
    -------

    86503db [Console] added a unit test
    3b2eeb6 [Console] fixed #5384

    Discussion
    ----------

    [Console] Simplified find method

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: #5384

    It simplifies the way command or command suggestions are found, also fixing issue #5384.

    It's a WIP I had, my plan was to also remove the original methods findAlternatives, etc... but some other methods are relying on them, so I'm sending it as it instead of running into more refactoring.

    ---------------------------------------------------------------------------

    by bamarni at 2012-10-06T20:35:59Z

    I've refactored some code, as you can see in the tests I've changed, the exception messages have slightly changed.

    ---------------------------------------------------------------------------

    by fabpot at 2012-10-08T13:04:36Z

    I don't see the point fo this big refactoring, especially if it also changes the behavior. I think a better approach would be to do only one thing at a time. But as is, this is not mergeable.

    ---------------------------------------------------------------------------

    by bamarni at 2012-10-08T14:01:41Z

    I'll try to fix this PR to keep the same behavior and error messages as it is currently.

    ---------------------------------------------------------------------------

    by bamarni at 2012-10-08T14:07:43Z

    Well if in fact there is only one bug, it would be better to patch the current code instead of doing this refactoring, even though I think the current code which finds commands is overcomplicated.

    ---------------------------------------------------------------------------

    by stof at 2012-10-08T14:09:18Z

    Well, in this case, please submit only a bug report for the 2.1 branch (or 2.0 if it is also affected), and then you can refactor the logic in master to simplify it if needed. But a refactoring should not happen in a maintenance branch

    ---------------------------------------------------------------------------

    by bamarni at 2012-10-09T07:32:47Z

    I've patched the code.

    ---------------------------------------------------------------------------

    by fabpot at 2012-10-09T07:37:53Z

    Can you add a test for the bug fix?

    ---------------------------------------------------------------------------

    by bamarni at 2012-10-09T21:30:21Z

    here you go
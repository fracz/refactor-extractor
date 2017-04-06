commit f11d7a0a0c08b5ef3b93988392c615124c00afec
Merge: 5cc43f9 7b63428
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Aug 28 07:36:52 2012 +0200

    merged branch Seldaek/sigchild-workaround (PR #5353)

    Commits
    -------

    7b63428 [Process] Add workaround for PHP's internal sigchild failing to return proper exit codes

    Discussion
    ----------

    [Process] Add workaround for PHP's internal sigchild failing to return proper exit codes

    PHP compiled with --enable-sigchild basically fails to return exit codes, and randomly returns -1 instead most of the time (see https://bugs.php.net/bug.php?id=29123).

    This works around it by having the exit code going through another pipe. It's enabled by default for linux because the new pipe trick won't work on windows I think, but that's unlikely to be an issue because most people don't compile their own php there.

    I could have it enabled only when sigchild is enabled using the code below, but obviously that adds some overhead, so I'm not sure what's worst.

    ```php
    ob_start();
    phpinfo(INFO_GENERAL);
    $sigchild = false !== strpos(ob_get_clean(), '--enable-sigchild');
    ```

    That said, this renders composer unusable (because we do check exit codes) for people having sigchild enabled, and it's not so easy to workaround outside of the Process class itself, so I hope this is an acceptable fix.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-08-26T13:41:18Z

    How about prepending commands with ``exec`` to avoid spawning child processes altogether?

    see #5030

    ---------------------------------------------------------------------------

    by Seldaek at 2012-08-26T13:51:40Z

    @schmittjoh I'm not sure how that's related to this issue? The problem here is that $exitcode is -1 when it should be 0, I don't see how the additional level of exec would help but maybe I'm missing your point.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-08-26T13:59:15Z

    I haven't looked in detail at this, but exec removes the child wrapper that PHP adds normally.

    ---------------------------------------------------------------------------

    by fabpot at 2012-08-26T16:10:24Z

    What about doing the fix in 2.0?

    ---------------------------------------------------------------------------

    by stof at 2012-08-26T16:13:04Z

    Can it be applied to 2.0 without too much work ? The Process component has been refactored for 2.1

    ---------------------------------------------------------------------------

    by Seldaek at 2012-08-26T16:16:06Z

    Just tried to rebase and it's not so trivial.. I can try to rebuild the
    patch from scratch for 2.0 if it's important.

    ---------------------------------------------------------------------------

    by fabpot at 2012-08-26T16:24:38Z

    ok, let's only do the fix for master for now.

    ---------------------------------------------------------------------------

    by Seldaek at 2012-08-26T20:49:39Z

    @fabpot ok so the question remains whether there should be a ctor check for the configure flag to enable this or if we just always do it and hope it doesn't cause issues.
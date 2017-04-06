commit d12011724ef292d5f2680f9a7e80a613e82fc8d2
Merge: a04d5d6 917f473
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jan 18 09:14:58 2013 +0100

    merged branch gnugat/refactoring/console-get-help (PR #6789)

    This PR was squashed before being merged into the master branch (closes #6789).

    Commits
    -------

    917f473 [Console] Removing unnecessary sprintf in Application->getHelp

    Discussion
    ----------

    [Console] Removing unnecessary sprintf in Application->getHelp

    Minor change of the `Symfony\Component\Console\Application->getHelp()`  method.

    I have spotted:
    1. an unnecessary `sprintf` call (no `args` arguments);
    2. two ways of adding a new line in the help (an empty string as new entry of the array and a `\n` at the end of the string).

    It seems to be there since the begining and it looks like a forgoten change to me, so I fixed them by removing the `sprintf` call and using a new array entry (empty string) instead of the `\n`.

    | Q             | A
    | ------------- | ---
    | License       | MIT
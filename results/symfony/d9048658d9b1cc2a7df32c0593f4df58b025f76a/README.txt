commit d9048658d9b1cc2a7df32c0593f4df58b025f76a
Merge: 52543e7 d37ff15
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Jul 16 18:30:54 2011 +0200

    merged branch schmittjoh/lazyParameterBag (PR #1712)

    Commits
    -------

    d37ff15 removed unused code
    2d3051f tabs -> spaces
    2c224ce improves the exception message, and removes unnecessary constraint to only allow strings inside strings
    d0b056c fixes a bug where getParameterBag() always returns null

    Discussion
    ----------

    Fixes a bug in PHPDumper, and in parameter resolving